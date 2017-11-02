package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.api.Person.passwordStrengthType;
import cn.zhijian.passport.api.Registration;
import cn.zhijian.passport.commands.CancelJoinCorporateCommand;
import cn.zhijian.passport.commands.CorporateInviteAduitCommand;
import cn.zhijian.passport.commands.EmailBindingCommand;
import cn.zhijian.passport.commands.ModifyPersonAvatarCommand;
import cn.zhijian.passport.commands.ModifyPersonCommand;
import cn.zhijian.passport.commands.PasswordResetCommand;
import cn.zhijian.passport.commands.PersonCorporateApplyStatusCommand;
import cn.zhijian.passport.commands.RegistrationCommand;
import cn.zhijian.passport.commands.SendEmailBindingCommand;
import cn.zhijian.passport.commands.ValidatePasswordResetInfoCommand;
import cn.zhijian.passport.commands.ValidateSMSCodeCommand;
import cn.zhijian.passport.commands.builders.SendEmailCommandBuilder;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.InvitationDAO;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.MessageRelationMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.SendBindingEmailMapper;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.InvitationRow;
import cn.zhijian.passport.db.row.JoinCorporateApplyView;
import cn.zhijian.passport.db.row.MessageRelationRow;
import cn.zhijian.passport.db.row.PersonCorporateView;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.SendBindingEmailRow;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.domain.crypto.PasswordEncrypter;
import cn.zhijian.passport.domain.events.PersonRegisterEvent;
import cn.zhijian.passport.domain.exceptions.RegistrationInvalidException;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.passport.statustype.MessageSourceType;
import cn.zhijian.passport.template.TemplateService;
import cn.zhijian.pay.commands.CreateWalletCommand;
import liquibase.util.StringUtils;
import lombok.Data;

public class PersonCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(PersonCommandHandler.class);

	final PersonDAO dao;
	final InvitationDAO inviteDao;
	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final TemplateService templateService;
	final String siteUrl;
	final SendBindingEmailMapper sendBindingEmailDao;
	final EventBus eventBus;
	final SqlSessionManager sqlSessionManager;
	final CorporateStaffMapper staffMapper;
	final CorporateMapper corporateMapper;
	final CorporateEnums corporateEnums;

	public PersonCommandHandler(SessionStore sessionStore, PersonDAO dao, InvitationDAO inviteDao, CommandGateway cmdGw,
			TemplateService temps, String siteUrl, SendBindingEmailMapper sendBindingEmailDa, EventBus eventBus,
			SqlSessionManager sqlSessionManager, CorporateStaffMapper staffMapper, CorporateMapper corporateMapper,
			CorporateEnums corporateEnums) {
		this.sessionStore = sessionStore;
		this.dao = dao;
		this.inviteDao = inviteDao;
		this.cmdGw = cmdGw;
		this.siteUrl = siteUrl;
		this.templateService = temps;
		this.sendBindingEmailDao = sendBindingEmailDa;
		this.eventBus = eventBus;
		this.sqlSessionManager = sqlSessionManager;
		this.staffMapper = staffMapper;
		this.corporateMapper = corporateMapper;
		this.corporateEnums = corporateEnums;
	}

	/**
	 * Return validation code
	 * 
	 * @param cmd
	 * @return
	 * @throws RegistrationInvalidException
	 */
	@CommandHandler
	public Pair<Boolean, String> register(RegistrationCommand cmd) throws RegistrationInvalidException {
		if (dao.findPersonByUsername(cmd.getData().getUsername()).size() > 0) {
			return Pair.of(false, "用户名重复");
		}

		if (dao.finPersonByMobile(cmd.getData().getMobile()).size() > 0) {
			return Pair.of(false, "手机号重复");
		}
		
		if (StringUtils.isEmpty(cmd.getData().getInvitationCode())) {
			return simpleRegister(cmd.getData());
		}
		return null;
	}

	@CommandHandler
	public boolean changeAvatar(ModifyPersonAvatarCommand cmd) {
		return sessionStore.doInSession(cmd.getSessionId(), ctx -> {
			logger.info("Changing Avatar for Person {}: {}", ctx.getPerson().getId(), cmd.getData().getResourceId());
			PersonRow row = dao.load(ctx.getPerson().getId());
			Person p = new Person(null, null, null, null, null, cmd.getData().getResourceId(), null, null, null, null,
					null, null, null, null, null, null);
			String InfoCompletion = IsInfoCompletion(row);
			int updateCnt = dao.changeAvatar(ctx.getPerson().getId(), cmd.getData().getResourceId(), InfoCompletion);
			return (updateCnt == 1);
		}, () -> false);
	}

	private Pair<Boolean, String> simpleRegister(Registration reg) throws RegistrationInvalidException {

		if (ValidateSMSCode(reg.getMobile(), reg.getCode())) {
			PersonRow person = insertPersonFromRegistration(reg);
			String sessionId = UUID.randomUUID().toString();
			LoginContext ctx = SetLoginContext(reg, sessionId, person.getId());
			sessionStore.put(sessionId, ctx);
			this.eventBus.publish(new GenericEventMessage<>(new PersonRegisterEvent(person.getId(), new Date(),person.getWalletId())));
			return Pair.of(true, "注册成功");
		} else {
			return Pair.of(false, "验证码不正确");
		}
	}

	private PersonRow insertPersonFromRegistration(Registration reg) throws RegistrationInvalidException {
		// move the whole record to person
		Date now = new Date();
		PersonRow person = convert(reg);
		person.setPasswordStrength(isStrengthType(reg.getPassword()));
		person.setInfoCompletion("20");
		person.setCreatedAt(now);
		person.setPassword(PasswordEncrypter.encrypt(reg.getPassword()));

		String Walletid = cmdGw.sendAndWait(new CreateWalletCommand());
		logger.debug("person Walletid : " + Walletid);
		person.setWalletId(Walletid);
		dao.insertPerson(person);

		return person;
	}

	@CommandHandler
	public boolean modifyPerson(ModifyPersonCommand cmd) throws Exception {
		boolean IsSuccess = sessionStore.doInSession(cmd.getSessionId(), ctx -> {
			PersonRow row = convertRow(cmd.getPerson());
			row.setUpdatedAt(new Date());
			row.setUpdatedBy(ctx.getName());
			row.setInfoCompletion(IsInfoCompletion(row));
			if (dao.updatePerson(row) > 0) {
				return true;
			}
			return false;
		}, null);
		return IsSuccess;
	}

	@CommandHandler
	public Pair<Boolean, String> ValidatePasswordResetInfo(ValidatePasswordResetInfoCommand cmd) throws Exception {
		
		if (dao.findPersonByUsername(cmd.getData().getUsername()).size() == 0) {
			return Pair.of(false, "用户不存在");
		}
		
		if (dao.findPersonByUsernameAndMobile(cmd.getData().getUsername(),cmd.getData().getMobile()).size() == 0) {
			return Pair.of(false, "用户不存在");
		}
		
		if (ValidateSMSCode(cmd.getData().getMobile(), cmd.getData().getCode())) {
			if (dao.findPersonByUsernameAndMobile(cmd.getData().getUsername(), cmd.getData().getMobile()).size() > 0) {
				return Pair.of(true, "发送成功");
			}
		}
		else {
			return Pair.of(false, "验证码不正确");
		}
		return null;
	}

	@CommandHandler
	public Pair<Boolean, String> passwordReset(PasswordResetCommand cmd) throws Exception {
		
		if (dao.findPersonByUsernameAndMobile(cmd.getData().getUsername(),cmd.getData().getMobile()).size() < 0) {
			return Pair.of(false, "用户不存在");
		}
		
		if (dao.PasswordReset(PasswordEncrypter.encrypt(cmd.getData().getPassword()), cmd.getData().getUsername(),
				cmd.getData().getMobile(), isStrengthType(cmd.getData().getPassword())) > 0) {
			return Pair.of(true, "密码修改成功");
		}
		return Pair.of(false, "密码修改失败");
	}

	@CommandHandler
	public boolean sendEmailBinding(SendEmailBindingCommand cmd) throws Exception {
		boolean IsSuccess = sessionStore.doInSession(cmd.getSessionId(), ctx -> {
			try {

				String validationCode = RandomStringUtils.randomAlphabetic(20);

				SendBindingEmailRow row = convertRow(ctx.getPerson().getId(), validationCode, cmd.getEmail());
				row.setCreatedAt(new Date());
				row.setCreatedBy(ctx.getPerson().getName());
				if (sendBindingEmailDao.insert(row) < 1) {
					return false;
				}

				Map<String, Object> vars = Maps.newHashMap();
				vars.put("link", composeValidationLink(validationCode));
				vars.put("time", 24);

				// use template engine for email content
				String html = templateService.getContent("bindingEmail/html.ftl", vars);
				String txt = templateService.getContent("bindingEmail/txt.ftl", vars);

				// send email
				cmdGw.send( //
						SendEmailCommandBuilder.builder() //
								.addTo(ctx.getPerson().getName(), cmd.getEmail()) //
								.setSubject("邮箱绑定Email") //
								.setText(txt) //
								.setTextHtml(html) //
								.build() //
				);
				return true;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}, null);
		return IsSuccess;
	}

	@CommandHandler
	public boolean emailBinding(EmailBindingCommand cmd) throws Exception {
		try {
			SendBindingEmailRow row = sendBindingEmailDao.findSendBindingEmailbyCode(cmd.getBindingCode());
			if (row != null) {
				if (dao.updateIsBindingEmail(1, row.getEmail(), row.getPersonId()) > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.debug("error : ", e);
		}

		return false;
	}

	private String composeValidationLink(String validationCode) {
		return siteUrl + "#/maivaliresult/" + validationCode;
	}

	private boolean ValidateSMSCode(String mobile, String code) {
		return cmdGw.sendAndWait((new ValidateSMSCodeCommand(mobile, code)));
	}

	private LoginContext SetLoginContext(Registration row, String sessionId, Long id) {
		if (row != null) {
			LoginContext ctx = new LoginContext(sessionId, convertRow(id, row), null, null, null, null);
			return ctx;
		}
		return null;
	}

	private PersonRow convert(Registration reg) {
		return new PersonRow(null, reg.getUsername(), reg.getPassword(), null, null, reg.getMobile(), null, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null);
	}

	private Person convertRow(Long id, Registration row) {
		return new Person(id, row.getUsername(), null, null, row.getMobile(), null, null, null, null, null, null, null,
				null, null, null, null);
	}

	private PersonRow convertRow(Person prs) {
		return new PersonRow(prs.getId(), prs.getUsername(), null, prs.getName(), prs.getEmail(), prs.getMobile(),
				prs.getAvatar(), prs.getRealName(), prs.getSex(), prs.getBirthday(), prs.getSchool(), prs.getQq(),
				prs.getWx(), null, null, prs.getRealName(), new Date(), prs.getIsBindingEmail(),
				prs.getPasswordStrength(), prs.getInfoCompletion(), prs.getWalletId());
	}

	private SendBindingEmailRow convertRow(long personId, String validationCode, String email) {
		return new SendBindingEmailRow(null, personId, validationCode, email, null, null);
	}

	
	private passwordStrengthType isStrengthType(String value) {

		int count = 0;
		if (Pattern.compile("\\d*").matcher(value).find()) {
			count++;
		}
		if (Pattern.compile("[a-z]").matcher(value).find()) {
			count++;
		}
		if (Pattern.compile("[A-Z]").matcher(value).find()) {
			count++;
		}
		if (Pattern.compile("\\W").matcher(value).find()) {
			count++;
		}

		switch (count) {
		case 1:
			return passwordStrengthType.WEAK;
		case 2:
			return passwordStrengthType.MEDIUM;
		case 3:
		case 4:
			return passwordStrengthType.STRONG;
		default:
			break;
		}
		return null;
	}

	private String IsInfoCompletion(PersonRow person) {
		int count = 0;

		if (person.getMobile() != null && person.getMobile() != "") {
			count = count + 10;
		}

		if (person.getUsername() != null && person.getUsername() != "") {
			count = count + 10;
		}

		if (person.getName() != null && person.getName() != "") {
			count = count + 10;
		}

		if (person.getAvatarResourceId() != null && person.getAvatarResourceId() != "") {
			count = count + 10;
		}

		if (person.getRealName() != null && person.getRealName() != "") {
			count = count + 10;
		}

		if (person.getBirthday() != null) {
			count = count + 10;
		}

		if (person.getSchool() != null && person.getSchool() != "") {
			count = count + 10;
		}

		if (person.getQq() != null && person.getQq() != "") {
			count = count + 10;
		}

		if (person.getWx() != null && person.getWx() != "") {
			count = count + 10;
		}

		if (person.getIsBindingEmail() != null) {
			count = count + 10;
		}

		return String.valueOf(count);
	}

	@CommandHandler
	public Pair<Boolean, String> corporateInviteAduit(CorporateInviteAduitCommand cmd) {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				InvitationDAO sessionInvitationDAO = session.getMapper(InvitationDAO.class);
				CorporateStaffMapper sessionStaffMapper = session.getMapper(CorporateStaffMapper.class);
				CorporateMapper sessionCorporateMapper = session.getMapper(CorporateMapper.class);
				InvitationRow row = sessionInvitationDAO.load(cmd.getInvitationid());
				if (row == null || row.getIscancel()) {
					return Pair.of(false, "加入信息不存在");
				}
				if (cmd.isIspass()) {
					if (sessionStaffMapper.judgePersonBelong(row.getPersonId(), corporateEnums.getCorporateEnums())
							.size() > 0) {
						return Pair.of(false, "您已隶属公司,不能加入公司操作");
					}
					CorporateRow corporate = sessionCorporateMapper.load(row.getInviterCorpId());
					if (corporate == null) {
						return Pair.of(false, "公司信息不存在");
					}
					StaffRow staff = new StaffRow();
					staff.setPersonId(row.getPersonId());
					staff.setCorporateId(row.getInviterCorpId());
					staff.setPersonname(row.getPersonname());
					staff.setJobnum(row.getJobnum());
					staff.setResidenceaddress(row.getResidenceaddress());
					staff.setEmail(row.getEmail());
					staff.setMobile(row.getMobile());
					staff.setSchoolrecord(row.getSchoolrecord());
					staff.setQualificationrecord(row.getQualificationrecord());
					staff.setAdvantage(row.getAdvantage());
					staff.setRole("STAFF");
					staff.setCreatedAt(new Date());
					staff.setCreatedBy(cmd.getOperateusername());
					staff.setBlocked(false);
					sessionStaffMapper.insertStaff(staff);
				}
				row.setAccepted(cmd.isIspass());
				sessionInvitationDAO.update(row);
				session.commit();
				return Pair.of(true, "审核完毕");
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@CommandHandler
	public List<Object> getCorporateApplyStatus(PersonCorporateApplyStatusCommand cmd) {

		// 有对应企业
		List<PersonCorporateView> passCorporates = this.dao.getCorporateInviteList(cmd.getPersonid(),
				corporateEnums.getCorporateEnums());
		if (passCorporates.size() > 0) {
			return passCorporates.stream().collect(Collectors.toList());
		} else {
			// 无对应企业
			List<CorporateApplyStatus> result = new ArrayList<>();
			// 获取公司创建
			List<CorporateRow> createApplys = this.corporateMapper.findCorporateCreateApply(cmd.getPersonid(),
					Arrays.asList(CorporateEnum.Audit_Apply.getCode(), CorporateEnum.Audit_Reject.getCode()));
			createApplys.stream().map(o -> {
				String ispending = "";
				switch (o.getIsPending()) {
				case Audit_Apply:
					ispending = "pending";
					break;
				case Audit_Reject:
					ispending = "refuse";
					break;
				case Audit_Pass:
					ispending = "pass";
					break;
				default:
					break;
				}
				return new CorporateApplyStatus(o.getId(), "create", o.getName(), ispending, o.getHsCode(),
						o.getCorporateMark(), o.getCreditCode(), o.getCreatedAt());
			}).forEach(o -> result.add(o));
			// 获取申请加入公司
			List<JoinCorporateApplyView> joinApplys = this.inviteDao.findJoinCorporateApply(cmd.getPersonid());
			joinApplys.stream().map(o -> {
				if (o.getAccepted() != null && o.getAccepted() == true) {
					return new CorporateApplyStatus(o.getCorporateid(), "join", o.getCorporatename(), "pass",
							o.getHscode(), o.getCorporatemark(), o.getCreditcode(), o.getApplydate());
				} else {
					return new CorporateApplyStatus(o.getCorporateid(), "join", o.getCorporatename(),
							o.getAccepted() == null ? "pending" : "refuse", o.getHscode(), o.getCorporatemark(),
							o.getCreditcode(), o.getApplydate());
				}
			}).forEach(o -> result.add(o));
			Collections.sort(result);
			return result.stream().map(o -> {
				Map<String, Object> op = new HashMap<>();
				op.put("corporateid", o.getCorporateid());
				op.put("companyType", o.getCompanyType());
				op.put("name", o.getName());
				op.put("isPending", o.getIsPending());
				op.put("hsCode", o.getHsCode());
				op.put("corporateMark", o.getCorporateMark());
				op.put("creditCode", o.getCreditCode());
				return op;
			}).collect(Collectors.toList());
		}
	}

	@CommandHandler
	public Pair<Boolean, String> cancelJoinApply(CancelJoinCorporateCommand cmd) {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				InvitationDAO sessionInvitationDAO = session.getMapper(InvitationDAO.class);
				MessageMapper sessionMessageMapper = session.getMapper(MessageMapper.class);
				MessageRelationMapper sessionmrMapper = session.getMapper(MessageRelationMapper.class);
				InvitationRow ir = sessionInvitationDAO.findLastJoinCorporateApply(cmd.getPersonid());
				if (ir == null) {
					return Pair.of(false, "无申请信息");
				}
				if (ir.getAccepted() != null && ir.getAccepted()) {
					return Pair.of(true, "您已通过审批,不能取消");
				}
				ir.setIscancel(true);
				sessionInvitationDAO.update(ir);
				MessageRelationRow mrr = sessionmrMapper.findBySourceId(ir.getId(),
						MessageSourceType.JoinCorporateApply);
				if (mrr != null) {
					sessionMessageMapper.deleteMessage(Arrays.asList(mrr.getMessageid()));
				}
				session.commit();
				return Pair.of(true, "取消成功");
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@Data
	private class CorporateApplyStatus implements Comparable {

		final Long corporateid;
		final String companyType;// 'join/create',
		final String name;// "公司名",
		final String isPending;// "pending/pass 待审核/通过"
		final String hsCode;// "海关代码",
		final String corporateMark;// '公司ID',
		final String creditCode;
		final Date applydate;

		@Override
		public int compareTo(Object o) {
			return (int) (((CorporateApplyStatus) o).applydate.getTime() - this.applydate.getTime());
		}
	}
}
