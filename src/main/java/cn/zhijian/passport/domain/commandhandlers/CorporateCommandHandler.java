package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.db.AdminCorporateMapper;
import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.api.Staff;
import cn.zhijian.passport.api.team.Team;
import cn.zhijian.passport.api.team.TeamData;
import cn.zhijian.passport.api.team.TeamStaffResponse;
import cn.zhijian.passport.commands.CorporateCancelCreateCommand;
import cn.zhijian.passport.commands.CorporateCertificationCommand;
import cn.zhijian.passport.commands.CreateCorporateCommand;
import cn.zhijian.passport.commands.CreateTeamCommand;
import cn.zhijian.passport.commands.DeleteTeamCommand;
import cn.zhijian.passport.commands.InviteCorporateStaffCommand;
import cn.zhijian.passport.commands.JoinCorporateAduitCommand;
import cn.zhijian.passport.commands.JoinCorporateStaffCommand;
import cn.zhijian.passport.commands.ModifyCorporateAvatarCommand;
import cn.zhijian.passport.commands.ModifyCorporateCommand;
import cn.zhijian.passport.commands.ModifyStaffCommand;
import cn.zhijian.passport.commands.ModityTeamCommand;
import cn.zhijian.passport.commands.RoleAddStaffCommand;
import cn.zhijian.passport.commands.TeamAddStaffCommand;
import cn.zhijian.passport.commands.UnbindStaffCommand;
import cn.zhijian.passport.commands.getTeamStaffCommand;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.InvitationDAO;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.InvitationRow;
import cn.zhijian.passport.db.row.JoinCorporateApplyView;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.RoleStaffRow;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.db.row.TeamMemberRow;
import cn.zhijian.passport.db.row.TeamRow;
import cn.zhijian.passport.domain.events.CorporateInvitedPersonEvent;
import cn.zhijian.passport.domain.events.PersonJoinCorporateEvent;
import cn.zhijian.passport.domain.exceptions.NoSessionException;
import cn.zhijian.passport.domain.exceptions.RecordNotUpdatedException;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.pay.commands.CreateWalletCommand;

public class CorporateCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(CorporateCommandHandler.class);

	final CommandGateway cmdGw;
	final CorporateMapper corpMapper;
	final CorporateStaffMapper staffMapper;
	final InvitationDAO inviteDao;
	final TeamDAO teamDao;
	final PersonDAO personDao;
	final SessionStore sessionStore;
	final String siteUrl;
	final SqlSessionManager sqlSessionManager;
	final EventBus eventBus;
	final AdminCorporateMapper adminCorporateMapper;
	final CorporateEnums corporateEnums;

	public CorporateCommandHandler(SessionStore sessionStore, CommandGateway cmdGw, CorporateMapper corpMapper,
			CorporateStaffMapper staffMapper, InvitationDAO inviteDao, PersonDAO personDao, TeamDAO teamDao,
			String siteUrl, SqlSessionManager sqlSessionManager, EventBus eventBus,AdminCorporateMapper adminCorporateMapper,CorporateEnums corporateEnums) {
		this.cmdGw = cmdGw;
		this.corpMapper = corpMapper;
		this.staffMapper = staffMapper;
		this.inviteDao = inviteDao;
		this.personDao = personDao;
		this.teamDao = teamDao;
		this.siteUrl = siteUrl;
		this.sessionStore = sessionStore;
		this.sqlSessionManager = sqlSessionManager;
		this.eventBus = eventBus;
		this.adminCorporateMapper = adminCorporateMapper;
		this.corporateEnums = corporateEnums;
	}

	@CommandHandler
	public Pair<String, Long> createCorporate(CreateCorporateCommand cmd) throws NoSessionException {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CorporateMapper sessionCorpMapper = session.getMapper(CorporateMapper.class);
				CorporateStaffMapper sessionstaffMapper = session.getMapper(CorporateStaffMapper.class);
				CorporateRoleMapper sessionCorporateRoleDao = session.getMapper(CorporateRoleMapper.class);
				//已经发送其他未取消的申请
				List<JoinCorporateApplyView> joinApplys = this.inviteDao.findJoinCorporateApply(cmd.getApplyPersonId());
				if(joinApplys.size() > 0){
					return Pair.of("您已经添加了申请加入公司,请取消后重新申请",null);
				}
				if(sessionstaffMapper.judgePersonBelong(cmd.getApplyPersonId(),null).size() > 0){
					return Pair.of("您已经隶属公司,不能进行创建", null);
				}
				if (sessionCorpMapper.countByCreditCode(cmd.getData().getCreditCode(), null) > 0) {
					return Pair.of("18位信息编号已被注册", null);
				}
				Date currDate = new Date();
				
				String walletid = cmdGw.sendAndWait(new CreateWalletCommand());
				logger.debug("person Walletid : "+walletid);
				
				CorporateRow corporateRow = convert(cmd.getData());
				corporateRow.setCreatedAt(currDate);
				corporateRow.setCreatedBy(cmd.getApplyUserName());
				corporateRow.setIsPending(CorporateEnum.Audit_Apply);
				corporateRow.setWalletId(walletid);
				corporateRow.setIsUpgrade(0); 
				corporateRow.setUseNum(0);
				sessionCorpMapper.insert(corporateRow);
				StaffRow staff = new StaffRow(cmd.getApplyPersonId(), corporateRow.getId(), "OWNER", cmd.getApplyUserName());
				sessionstaffMapper.insertStaff(staff);
				String[] roles = new String[] { "管理员", "总经理", "报关员", "财务" };
				for (String item : roles) {
					CorporateRoleRow crRow = new CorporateRoleRow();
					crRow.setCorporateid(corporateRow.getId());
					crRow.setRolename(item);
					crRow.setCreatedAt(currDate);
					crRow.setCreatedBy(cmd.getApplyUserName());
					sessionCorporateRoleDao.insert(crRow);
				}
				session.commit();
				logger.debug("Created Corporate: {} by {} (CorpID: {}, StaffID: {})", cmd.getData().getName(),
						cmd.getApplyUserName(), corporateRow.getId(), staff.getId());
				return Pair.of(null, corporateRow.getId());
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	private CorporateRow convert(Corporate reg) {
		return new CorporateRow(reg.getId(), reg.getName(), reg.getWebsite(), reg.getAddress(), reg.getLogo(),
				reg.getHsCode(), reg.getCreditCode(), reg.getCustomArea(), reg.getContactsName(), reg.getContactsSex(),
				reg.getContactsDuties(), reg.getContactsMobile(), reg.getContactsTel(), reg.getCorporateMark(),
				reg.getContactsEmail(), reg.getCreditLevel(), null, null, null, null, null, null, reg.getWalletId(),
				null, null, null, null, null, null, null, null, null, null);
	}

	@CommandHandler
	public Pair<String, Long> modifyCorporate(ModifyCorporateCommand cmd) throws RecordNotUpdatedException {

		if (this.corpMapper.countByCreditCode(cmd.getData().getCreditCode(), Arrays.asList(cmd.getData().getId())) > 0) {
			return Pair.of("18位信息编号已被注册", null);
		}
		CorporateRow corporate = this.corpMapper.load(cmd.getData().getId());
//		if(corporate.getIsPending() != CorporateEnum.Audit_Reject){
//			return Pair.of("只有否决状态才可修改", null);
//		}
		corporate.setName(cmd.getData().getName());
		corporate.setWebsite(cmd.getData().getWebsite());
		corporate.setAddress(cmd.getData().getAddress());
		corporate.setLogoResourceId(cmd.getData().getLogo());
		corporate.setHsCode(cmd.getData().getHsCode());
		corporate.setCreditCode(cmd.getData().getCreditCode());
		corporate.setCustomArea(cmd.getData().getCustomArea());
		corporate.setContactsName(cmd.getData().getContactsName());
		corporate.setContactsSex(cmd.getData().getContactsSex());
		corporate.setContactsDuties(cmd.getData().getContactsDuties());
		corporate.setContactsMobile(cmd.getData().getContactsMobile());
		corporate.setContactsTel(cmd.getData().getContactsTel());
		corporate.setCorporateMark(cmd.getData().getCorporateMark());
		corporate.setContactsEmail(cmd.getData().getContactsEmail()); 
		corporate.setCreditLevel(cmd.getData().getCreditLevel());
		corporate.setWalletId(cmd.getData().getWalletId());
		corporate.setUpdatedAt(new Date());
		corporate.setUpdatedBy(cmd.getApplyUserName());
//		corporate.setIsPending(CorporateEnum.Audit_Apply);
		if (corpMapper.update(corporate) != 1) {
			throw new RecordNotUpdatedException();
		}
		logger.debug("Updated Corporate: {} by {}", cmd.getData().getName(), cmd.getApplyUserName());
		return Pair.of(null, cmd.getData().getId());
	}

	@CommandHandler
	public Pair<Boolean, String> inviteCorporateStaff(InviteCorporateStaffCommand cmd) {

		CorporateRow corporateRow = this.corpMapper.load(cmd.getCorporateId());
		
		if (corporateRow != null) {
			PersonRow person = this.personDao.findPersonByAdminUsername(cmd.getStaffinfo().getAccountNo());
			if(person == null){
				return Pair.of(false, "账号\""+cmd.getStaffinfo().getAccountNo()+"\"的会员不存在");
			}
			if (this.staffMapper.judgePersonBelong(person.getId(),corporateEnums.getCorporateEnums()).size() > 0) {
				return Pair.of(false, "账号\"" + cmd.getStaffinfo().getAccountNo() + "\"已经隶属公司");
			}
			InvitationRow row = new InvitationRow();
			row.setInviterCorpId(cmd.getCorporateId());
			row.setInviterId(cmd.getInvitationpersonid());
			row.setPersonId(person.getId());
			row.setUsername(cmd.getStaffinfo().getAccountNo());
			row.setPersonname(cmd.getStaffinfo().getRelName());
			row.setJobnum(cmd.getStaffinfo().getWorkNo());
			row.setResidenceaddress(cmd.getStaffinfo().getBirthOrigin());
			row.setEmail(cmd.getStaffinfo().getEmail());
			row.setMobile(cmd.getStaffinfo().getPhone());
			row.setSchoolrecord(cmd.getStaffinfo().getEduBg());
			row.setQualificationrecord(cmd.getStaffinfo().getQualifi());
			row.setAdvantage(cmd.getStaffinfo().getStrongPoint());
			row.setCreatedAt(new Date());
			row.setCreatedBy(cmd.getInvitationuserName());
			row.setInvitationType(1);
			row.setRemark(cmd.getStaffinfo().getMark());
			row.setIscancel(false);
			inviteDao.insert(row);
			this.eventBus.publish(new GenericEventMessage<>(new CorporateInvitedPersonEvent(cmd.getInvitationpersonid(),
					cmd.getCorporateId(), corporateRow.getName(), cmd.getInvitationpersonName(), new Date(),
					person.getId(), row.getId())));
			return Pair.of(true, "提交申请中");
		} else {
			return Pair.of(false, "公司信息不存在");
		}
	}

	@CommandHandler
	public Pair<Boolean, String> modifyCorporateStaff(ModifyStaffCommand cmd) {

		StaffRow row = this.staffMapper.loadStaff(cmd.getStaffid());
		if (row == null) {
			return Pair.of(false, "该员工信息不存在");
		}
		row.setPersonname(cmd.getPersonname());
		row.setJobnum(cmd.getJobnum());
		row.setResidenceaddress(cmd.getResidenceaddress());
		row.setEmail(cmd.getEmail());
		row.setMobile(cmd.getMobile());
		row.setSchoolrecord(cmd.getSchoolrecord());
		row.setQualificationrecord(cmd.getQualificationrecord());
		row.setAdvantage(cmd.getAdvantage());
		row.setUpdatedAt(new Date());
		row.setUpdatedBy(cmd.getUpdateusername());
		this.staffMapper.updateStaff(row);
		return Pair.of(true, "修改完毕");
	}

	@CommandHandler
	public Pair<Boolean, String> staffUnbind(UnbindStaffCommand cmd) {

		StaffRow staff = this.staffMapper.loadStaff(cmd.getStaffid());
		if (staff == null) {
			return Pair.of(false, "员工信息不存在");
		}
		if (staff.isBlocked()) {
			return Pair.of(false, "员工已解绑");
		}
		staff.setBlocked(true);
		this.staffMapper.updateStaff(staff);
		return Pair.of(true, "解绑完毕");
	}

	@CommandHandler
	public Long createTeam(CreateTeamCommand cmd) {
		return sessionStore.doInSession(cmd.getSessionId(), ctx -> {
			// XXX access control
			TeamRow row = new TeamRow(null, cmd.getCorporateId(), cmd.getTeam().getName(),
					cmd.getTeam().getDescription(), ctx.getPerson().getUsername(), new Date(), null, null);

			if (teamDao.insert(row) != 1) {
				throw new RuntimeException("Insert Failed");
			}
			long teamId = row.getId();
			// insert admin as the first member
			// StaffRow staff = staffMapper.loadStaff(cmd.getAdminStaffId());
			// TeamMemberRow member = new TeamMemberRow(null, cmd.getCorporateId(), teamId,
			// staff.getId(), "ADMIN",
			// ctx.getPerson().getUsername(), new Date(), null, null);
			// teamDao.insertMember(member);
			return teamId;
		});
	}

	@CommandHandler
	public int modityTeam(ModityTeamCommand cmd) {
		if (cmd.getTeam() == null) {
			throw new RuntimeException("Team is null");
		}
		int isMotity = teamDao.update(cmd.getTeam().getName(), cmd.getTeam().getDescription(), cmd.getTeam().getId(),
				cmd.getCorpId());
		if (isMotity != 1) {
			throw new RuntimeException("Update Failed");
		}
		return isMotity;
	}

	@CommandHandler
	public Pair<Boolean,String> joinCorporate(JoinCorporateStaffCommand cmd) {
		
		CorporateRow corporateRow = corpMapper.findCorporatesByNameAndMark(cmd.getCorporatename(),cmd.getCorporatemark());
		
		if (corporateRow != null) {
			//创建公司
			CorporateRow cr = this.corpMapper.getOwnerCorporate(cmd.getPerson().getId());
			if(cr != null){
				return Pair.of(false, "您已经创建公司,不能申请加入公司");
			}
			//已经发送其他未取消的申请
			List<JoinCorporateApplyView> joinApplys = this.inviteDao.findJoinCorporateApply(cmd.getPerson().getId());
			if(joinApplys.size() > 0){
				return Pair.of(false, "您已经添加了申请加入公司,请取消后重新申请");
			}
			//已经隶属公司
			if(this.staffMapper.judgePersonBelong(cmd.getPerson().getId(),corporateEnums.getCorporateEnums()).size()>0){
				return Pair.of(false, "您已经隶属公司,不能申请加入公司");
			}
			InvitationRow row = new InvitationRow();
			row.setInviterCorpId(corporateRow.getId());
			row.setInviterId(cmd.getPerson().getId());
			row.setPersonId(cmd.getPerson().getId());
			row.setUsername(cmd.getPerson().getUsername());
			row.setPersonname(cmd.getRelName());
			row.setEmail(cmd.getPerson().getEmail());
			row.setMobile(cmd.getPerson().getMobile());
			row.setCreatedAt(new Date());
			row.setCreatedBy(cmd.getPerson().getUsername());
			row.setInvitationType(0);
			row.setRemark(cmd.getRemark());
			row.setIscancel(false);
			inviteDao.insert(row);
			this.eventBus.publish(new GenericEventMessage<>(new PersonJoinCorporateEvent(row.getId(),cmd.getPerson().getId(),
					corporateRow.getId(),cmd.getPerson().getMobile(),cmd.getRelName(),new Date())));
			return Pair.of(true, "提交申请中");
		} else {
			return Pair.of(false, "公司信息不存在");
		}
	}

	@CommandHandler
	public Pair<Boolean, String> joinCorporateAduit(JoinCorporateAduitCommand cmd) {

		try (SqlSession session = this.sqlSessionManager.openSession()) {
			try {
				InvitationDAO sessionInvitationDAO = session.getMapper(InvitationDAO.class);
				CorporateStaffMapper sessionStaffMapper = session.getMapper(CorporateStaffMapper.class);
				CorporateMapper sessionCorporateMapper = session.getMapper(CorporateMapper.class);
				InvitationRow row = sessionInvitationDAO.load(cmd.getInvitationid());
				if (row == null || row.getIscancel()) {
					return Pair.of(false, "加入信息不存在");
				}
				if (row.getAccepted() != null) {
					return Pair.of(false, "审核完毕不可重复操作");
				}
				if (cmd.isIspass()) {
					CorporateRow corporate = sessionCorporateMapper.load(row.getInviterCorpId());
					if(corporate == null){
						return Pair.of(false, "公司信息不存在");
					}
					if (sessionStaffMapper.judgePersonBelong(row.getPersonId(),corporateEnums.getCorporateEnums()).size() > 0) {
						return Pair.of(false, "该会员已隶属公司");
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
	public Pair<Boolean, String> modifyCorporateAvatar(ModifyCorporateAvatarCommand cmd) {

		CorporateRow corporateRow = corpMapper.load(cmd.getCorporateId());
		if (corporateRow == null) {
			return Pair.of(false, "公司信息不存在");
		}
		corporateRow.setLogoResourceId(cmd.getResourceId());
		corpMapper.update(corporateRow);
		return Pair.of(true, null);
	}

	@CommandHandler
	public TeamStaffResponse getTeamStaff(getTeamStaffCommand cmd) {
		long corpId = cmd.getCorpId();

		List<TeamRow> teamRow = teamDao.findTeambyId(corpId);
		List<TeamData> TeamDatas = new ArrayList<>();
		List<String> teamIds = new ArrayList<>();
		for (TeamRow row : teamRow) {
			TeamData teamData = new TeamData();
			Team team = convert(row);
			List<StaffRow> staffRowList = teamDao.findStaffInnerJoinbyId(corpId, row.getId());
			// List<Staff> staffs = staffRowList.stream().map(_row -> new
			// Staff(_row.getId(),null,personDao.load(_row.getId()).getName(),
			// null,null,null,null,null,null,null,null)).collect(Collectors.toList());
			List<Staff> staffs = new ArrayList<>();
			for (StaffRow staffRow : staffRowList) {
				String name = staffRow.getPersonname();
				Staff s = new Staff(staffRow.getId(), null, name, null, null, null, null, null, null, null, null);
				staffs.add(s);
			}
			teamData.setTeam(team);
			teamData.setStaffs(staffs);
			teamIds.add(String.valueOf(team.getId()));
			TeamDatas.add(teamData);
		}

		if (teamIds.size() != 0) {
			// Other
			List<StaffRow> staffRowList = teamDao.findStaffNotTeamMemberbyId(corpId, teamIds);
			// List<Staff> staffs = staffRowList.stream().map(_row -> new
			// Staff(_row.getId(),null,personDao.load(_row.getId()).getName(),
			// null,null,null,null,null,null,null,null)).collect(Collectors.toList());
			List<Staff> staffs = new ArrayList<>();
			for (StaffRow staffRow : staffRowList) {
				String name = staffRow.getPersonname();
				Staff s = new Staff(staffRow.getId(), null, name, null, null, null, null, null, null, null, null);
				staffs.add(s);
			}
			return new TeamStaffResponse(TeamDatas, staffs);
		} else {
			List<StaffRow> staffRowList = teamDao.findStaffbyCorpId(corpId);
			// List<Staff> staffs = staffRowList.stream().map(_row -> new
			// Staff(_row.getId(),null,personDao.load(_row.getId()).getName(),
			// null,null,null,null,null,null,null,null)).collect(Collectors.toList());
			List<Staff> staffs = new ArrayList<>();
			for (StaffRow staffRow : staffRowList) {
				String name = staffRow.getPersonname();
				Staff s = new Staff(staffRow.getId(), null, name, null, null, null, null, null, null, null, null);
				staffs.add(s);
			}
			return new TeamStaffResponse(TeamDatas, staffs);
		}
	}

	@CommandHandler
	public long TeamAddStaff(TeamAddStaffCommand cmd) {
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				teamDao.deleteTeamMembers(cmd.getTeamid(), cmd.getCorpId());

				for (long id : cmd.getStaffids().getStaffIds()) {
					TeamMemberRow member = new TeamMemberRow(null, cmd.getCorpId(), cmd.getTeamid(), id, null,
							cmd.getUsername(), new Date(), null, null);
					teamDao.insertMember(member);
				}
			} catch (Exception e) {
				session.rollback();
			}
			session.commit();
		}
		return 1;
	}

	@CommandHandler
	public Integer DeleteTeam(DeleteTeamCommand cmd) {
		if (teamDao.findStaffInnerJoinbyId(cmd.getCorpId(), cmd.getTeam().getId()).size() > 0) {
			return 0;
		}
		if (teamDao.deleteTeam(cmd.getTeam().getId()) > 0) {
			return 1;
		}
		return null;
	}

	@CommandHandler
	public int roleAddStaff(RoleAddStaffCommand cmd) {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				corpMapper.deleteRoleStaff(cmd.getRoleId(), cmd.getCorpId());

				for (long id : cmd.getStaffList().getStaffIds()) {
					RoleStaffRow row = convert(cmd.getRoleId(), id, cmd.getCorpId());
					row.setCreatedAt(new Date());
					row.setCreatedBy(cmd.getUsername());
					corpMapper.insertRoleStaff(row);
				}
				session.commit();
			} catch (Exception e) {
				// TODO: handle exception
				session.rollback();
			}
		}
		return 1;
	}

	@CommandHandler
	public Pair<Boolean, String> CorporateCertification(CorporateCertificationCommand cmd) {
		CorporateRow row = corpMapper.load(cmd.getCorporate().getId());
		if (row == null) {
			return Pair.of(false, "该数据不存在");
		}
		if (row.getIsPending() == CorporateEnum.Audit_Pass || row.getIsPending() == CorporateEnum.Authentication_Reject) {
			row.setAddress(cmd.getCorporate().getAddress());
			row.setCreditLevel(cmd.getCorporate().getCreditLevel());
			row.setIndustryType(cmd.getCorporate().getIndustryType());
			row.setIndustry(cmd.getCorporate().getIndustry());
			row.setNature(cmd.getCorporate().getNature());
			row.setProvince(cmd.getCorporate().getProvince());
			row.setCity(cmd.getCorporate().getCity());
			row.setBusinessLicense(cmd.getCorporate().getBusinessLicense());
			row.setIsPending(CorporateEnum.Authentication_Apply);
			if (corpMapper.updateCert(row) > 0) {
				return Pair.of(true, "更新成功");
			} else {
				return Pair.of(false, "更新操作失败");
			}
		}
		else {
			return Pair.of(false, "已审核、认证失败状态才可修改");
		}
	}

	public Team convert(TeamRow row) {
		return new Team(row.getId(), row.getName(), row.getDescription());
	}

	public RoleStaffRow convert(long roleId, long staffId, long corporateId) {
		return new RoleStaffRow(null, roleId, staffId, corporateId, null, null, null, null);
	}
	
	@CommandHandler
	public Pair<Boolean, String> cancelCorporateCreate(CorporateCancelCreateCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CorporateMapper sessionCorporateMapper = session.getMapper(CorporateMapper.class);
				CorporateRoleMapper sessionRoleMapper = session.getMapper(CorporateRoleMapper.class);
				CorporateStaffMapper sessionStaffMapper = session.getMapper(CorporateStaffMapper.class);
				CorporateRow cr = sessionCorporateMapper.load(cmd.getCorporateid());
				if(cr == null){
					return Pair.of(false, "公司信息不存在");
				}
				if(cr.getIsPending() == CorporateEnum.Audit_Pass || cr.getIsPending().getCode() >= CorporateEnum.Authentication_Not.getCode()){
					return Pair.of(false, "公司已经过审核,不能取消创建");
				}
				sessionStaffMapper.deleteByCorporateId(cmd.getCorporateid());
				sessionRoleMapper.deleteByCorporateId(cmd.getCorporateid());
				sessionCorporateMapper.deleteCorporateApplication(cmd.getCorporateid());
				sessionCorporateMapper.deleteCorporate(cmd.getCorporateid());
				session.commit();
				return Pair.of(true, null);
			} catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}
}
