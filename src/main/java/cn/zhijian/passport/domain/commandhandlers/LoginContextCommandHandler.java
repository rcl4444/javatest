package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.OAuthCodeRequest;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.api.Person.passwordStrengthType;
import cn.zhijian.passport.api.StaffInfo;
import cn.zhijian.passport.commands.LoginCommand;
import cn.zhijian.passport.commands.RefreshLoginContextCommand;
import cn.zhijian.passport.commands.SwitchCorporateCommand;
import cn.zhijian.passport.converters.CorporateConverter;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.db.row.CorporateModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.RoleStaffRow;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.db.row.TeamRow;
import cn.zhijian.passport.domain.crypto.PasswordEncrypter;
import cn.zhijian.passport.domain.exceptions.NoSessionException;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.CorporateEnum;

public class LoginContextCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(LoginContextCommandHandler.class);

	final PersonDAO dao;
	final CorporateMapper corpDao;
	final SessionStore sessionStore;
	final TeamDAO teamDAO;
	final CorporateRoleMapper roleMapper;
	final CorporateStaffMapper staffMapper;
	final CorporateEnums corporateEnums;

	public LoginContextCommandHandler(SessionStore sessionStore, PersonDAO dao, CorporateMapper corpDao,
			TeamDAO teamDAO, CorporateRoleMapper roleMapper, CorporateStaffMapper staffMapper,
			CorporateEnums corporateEnums) {
		this.dao = dao;
		this.corpDao = corpDao;
		this.sessionStore = sessionStore;
		this.teamDAO = teamDAO;
		this.roleMapper = roleMapper;
		this.staffMapper = staffMapper;
		this.corporateEnums = corporateEnums;
	}

	/**
	 * Login Command Processed, Updated Backend Session Store
	 * 
	 * @param cmd
	 * @return
	 */
	@CommandHandler
	public LoginContext login(LoginCommand cmd) {

		String hashedPassword = PasswordEncrypter.encrypt(cmd.getData().getPassword());
		PersonRow row = dao.findPersonByLogin(cmd.getData().getUsername(), hashedPassword);
		if (row != null) {
			logger.debug("User: " + cmd.getData().getUsername() + " logged in");
			LoginContext loginctx = sessionStore.get(cmd.getSessionId());
			if (loginctx != null && loginctx.getOauth() != null) {
				OAuthCodeRequest or = loginctx.getOauth();
				loginctx = loadLoginContext(cmd.getSessionId(), row.getId());
				loginctx.setOauth(or);
			} else {
				loginctx = loadLoginContext(UUID.randomUUID().toString(), row.getId());
			}
			sessionStore.put(loginctx.getSessionId(), loginctx);
			return loginctx;
		}
		logger.error("User: " + cmd.getData().getUsername() + " logged FAILED");
		return null;
	}

	@CommandHandler
	public LoginContext refresh(RefreshLoginContextCommand cmd) throws NoSessionException {

		String sessionId = cmd.getSessionId();
		if (sessionId == null) {
			throw new NoSessionException();
		}
		LoginContext oldctx = sessionStore.get(sessionId);
		if (oldctx == null) {
			throw new NoSessionException();
		}
		long personId = oldctx.getPerson().getId();

		LoginContext ctx = loadLoginContext(sessionId, personId);
		sessionStore.put(sessionId, ctx);
		return ctx;
	}

	@CommandHandler
	public LoginContext switchCorporate(SwitchCorporateCommand cmd) throws NoSessionException {

		String sessionId = cmd.getSessionId();
		if (sessionId == null) {
			throw new NoSessionException();
		}
		LoginContext oldctx = sessionStore.get(sessionId);
		if (oldctx == null) {
			throw new NoSessionException();
		}

		LoginContext ctx = switchCorporate(oldctx, cmd.getSwitchTo().getId());
		sessionStore.put(sessionId, ctx);
		return ctx;
	}

	private LoginContext switchCorporate(LoginContext ctx, long corpId) {
		Optional<Corporate> selected = ctx.getCorporates().stream().filter(c -> c.getId().longValue() == corpId)
				.findFirst();
		if (selected.isPresent()) {
			StaffRow staff = this.staffMapper.findStaffByPersonId(ctx.getPerson().getId(), corpId);
			List<TeamRow> team = this.teamDAO.findTeamByStaffId(staff.getId());
			List<CorporateRoleRow> roles = this.roleMapper.findRoleByStaffId(staff.getId());
			StaffInfo si = new StaffInfo(staff.getPersonname(),
					team == null ? null : team.stream().map(o -> o.getName()).collect(Collectors.joining(",")),
					roles.stream().map(o -> o.getRolename()).collect(Collectors.toList()));
			List<String> modules = getModules(selected.get(), roles);
			return new LoginContext(ctx.getSessionId(), ctx.getPerson(), selected.get(), ctx.getCorporates(), si,
					modules);
		}
		logger.error("Switch Corporate not found, not switched");
		return ctx;
	}

	private LoginContext loadLoginContext(String sessionId, long personId) {
		PersonRow row = dao.load(personId);
		if (row != null) {
			List<Corporate> corporates = corpDao
					.findCorporatesByPersonId(row.getId(), corporateEnums.getCorporateEnums()).stream()
					.map(r -> convertCorpRow(r)).collect(Collectors.toList());
			Corporate current = corporates.size() > 0 ? corporates.get(0) : null;
			StaffInfo si = null;
			List<String> modules = new ArrayList<>();
			if (current != null) {
				StaffRow staff = this.staffMapper.findStaffByPersonId(personId, current.getId());
				if (staff.getRole().equals("OWNER")) {
					si = new StaffInfo("法人代表", "企业法人", Arrays.asList("超级管理员"));
					modules = roleMapper.findAllModule().stream().map(o -> o.getModuleName())
							.collect(Collectors.toList());
				} else {
					List<TeamRow> team = this.teamDAO.findTeamByStaffId(staff.getId());
					List<CorporateRoleRow> roles = this.roleMapper.findRoleByStaffId(staff.getId());
					si = new StaffInfo(staff.getPersonname(),
							team == null ? null : team.stream().map(o -> o.getName()).collect(Collectors.joining(",")),
							roles.stream().map(o -> o.getRolename()).collect(Collectors.toList()));
					modules = getModules(current, roles);
				}
			}
			LoginContext ctx = new LoginContext(sessionId, convertRow(row), current, corporates, si, modules);
			return ctx;
		}
		return null;
	}

	private List<String> getModules(Corporate corporate, List<CorporateRoleRow> roles) {
		if (roles.size() == 0) {
			return new ArrayList<>();
		}
		List<CorporateModuleRow> corporateModuleRows = roleMapper.findCorporateModulebyRoleid(
				roles.stream().map(o -> o.getId()).collect(Collectors.toList()), corporate.getId());
		return corporateModuleRows.stream().map(o -> o.getModuleName()).collect(Collectors.toList());
	}

	private Person convertRow(PersonRow row) {
		return new Person(row.getId(), row.getUsername(), row.getName(), row.getEmail(), row.getMobile(),
				row.getAvatarResourceId(), row.getRealName(), row.getSex(), row.getBirthday(), row.getSchool(),
				row.getQq(), row.getWx(), row.getIsBindingEmail(), row.getPasswordStrength(), row.getInfoCompletion(),
				row.getWalletId());
	}

	private Corporate convertCorpRow(CorporateRow row) {
		return CorporateConverter.convertRow(row);
	}

}
