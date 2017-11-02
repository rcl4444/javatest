package cn.zhijian.passport.repos;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.api.Staff;
import cn.zhijian.passport.api.team.Team;
import cn.zhijian.passport.converters.CorporateConverter;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.CorporateStaffView;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.RoleStaffRow;
import cn.zhijian.passport.db.row.StaffInvitationView;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.db.row.TeamRow;
import cn.zhijian.passport.session.SessionStore;

public class CorporateRepository {

	final static Logger logger = LoggerFactory.getLogger(CorporateRepository.class);

	final CorporateMapper dao;
	final CorporateStaffMapper staffMapper;
	final TeamDAO teamDao;
	final SessionStore sessionStore;
	final CorporateRoleMapper corporateRoleMapper;
	final PersonDAO personDAO;
	final ApplicationDao applicationDao;

	public CorporateRepository(SessionStore sessionStore, CorporateMapper dao, CorporateStaffMapper staffMapper,
			TeamDAO teamDao,CorporateRoleMapper corporateRoleMapper,PersonDAO personDAO,ApplicationDao applicationDao) {
		this.dao = dao;
		this.staffMapper = staffMapper;
		this.teamDao = teamDao;
		this.sessionStore = sessionStore;
		this.corporateRoleMapper = corporateRoleMapper;
		this.personDAO = personDAO;
		this.applicationDao = applicationDao;
	}

	/**
	 * The logged in person want to load the corporate information. Access control
	 * is ensured.
	 * 
	 * @param sessionId
	 * @param corporateId
	 * @return
	 */
	public Corporate load(String sessionId, long corporateId) {
		return sessionStore.doInSession(sessionId,
				ctx -> CorporateConverter.convertRow(dao.loadCorporateByStaff(corporateId, ctx.getPerson().getId())),
				null);
	}

	public PagedResult<CorporateStaffView> loadStaff(long personId, long corpId, Integer state, Integer roleid, String query, int pageNo, int pageSize) {

		int offset = (pageNo - 1) * pageSize;
		int totalRows = staffMapper.countStaffByQueryCorporate(query, state, roleid, corpId, personId);
		return new PagedResult<>(staffMapper.loadStaffByQueryCorporate(query, state, roleid, corpId, personId, offset, pageSize),
				totalRows, pageNo, pageSize);
	}
	
	public PagedResult<StaffInvitationView> loadJoinStaff(long personId, long corpId,Integer state, String query, int pageNo, int pageSize) {
	
		int offset = (pageNo - 1) * pageSize;
		int totalRows = staffMapper.countInvitationStaffQuery(query, state, 0, corpId, personId);
		return new PagedResult<>(staffMapper.loadInvitationStaffQuery(query, state, 0, corpId, personId, offset, pageSize),
				totalRows, pageNo, pageSize);
	}
	
	public PagedResult<StaffInvitationView> loadInviteStaff(long personId, long corpId,Integer state, String query, int pageNo, int pageSize) {
		
		int offset = (pageNo - 1) * pageSize;
		int totalRows = staffMapper.countInvitationStaffQuery(query, state, 1, corpId, personId);
		return new PagedResult<>(staffMapper.loadInvitationStaffQuery(query, state, 1, corpId, personId, offset, pageSize),
				totalRows, pageNo, pageSize);
	}

	public PagedResult<Team> loadTeam(String sessionId, long corpId, String query, Integer pageNo, Integer pageSize) {
		PagedResult<Team> rows = sessionStore.doInSession(sessionId, ctx -> {
			if (query != null && !query.trim().isEmpty()) {
				String q = "%" + query.trim() + "%";
				int totalRows = teamDao.countByQueryCorporate(q, corpId);
				int p = 1;
				int s = totalRows;
				if (pageNo != null) {
					p = pageNo;
				}
				if (pageSize != null) {
					s = pageSize;
				}
				int offset = (p - 1) * s;
				return new PagedResult<>(teamDao.loadByQueryCorporate(q, corpId, offset, s), totalRows, p, s)
						.map(this::convert);

			} else {
				int totalRows = teamDao.countByCorporate(corpId);
				int p = 1;
				int s = totalRows;
				if (pageNo != null) {
					p = pageNo;
				}
				if (pageSize != null) {
					s = pageSize;
				}
				int offset = (p - 1) * s;
				return new PagedResult<>(teamDao.loadByCorporate(corpId, offset, s), totalRows, p, s)
						.map(this::convert);
			}
		}, null);
		logger.debug("Teams: {}", rows);
		return rows;
	}
	
	public PagedResult<CorporateRow> filterCorporate(PagingQuery query){
		
		int totalRows = this.dao.getExamineCount(query);
		int s = query.getPageSize() == null? totalRows : query.getPageSize();
		return new PagedResult<>(this.dao.getExaminePaging(query),totalRows,query.getPageNo(),query.getPageSize());
	}

	public Staff getStaffById(Long id){
		
		StaffRow row = this.staffMapper.loadStaff(id);
		if(row == null){
			return null;
		}
		else{
			PersonRow person = this.personDAO.load(row.getPersonId());
			return new Staff(row.getId(),
				person.getUsername(),
				row.getPersonname(),
				row.getJobnum(),
				row.getResidenceaddress(),
				row.getEmail(),
				row.getMobile(),
				row.getSchoolrecord(),
				row.getQualificationrecord(),
				row.getAdvantage(),
				null);
		}
	}
	
	public Corporate load(long corporateId) {
		return CorporateConverter.convertRow(dao.load(corporateId));
	}
	
	public List<ApplicationRow> CorpOnApplication(long corpId)
	{
		return applicationDao.findApplicationOnCorp(corpId);
	}
	
	public List<ApplicationRow> CorpNotApplication(long corpId)
	{
		return applicationDao.findApplicationNotCorp(corpId);
	}
	
	public RoleStaffRow findRoleStaffbyStaffId(long corporateId, long staffId)
	{
		return dao.findRoleStaffbyStaffId(corporateId, staffId);
	}
	
	public List<StaffRow> findStaffInnerJoinbyId(long corpId, long teamId) {
		return 	teamDao.findStaffInnerJoinbyId(corpId, teamId);
	}
	
	public List<TeamRow> findTeambyId(long corpId){
		return teamDao.findTeambyId(corpId);
	}
	
	public List<CorporateRoleRow> findCorporateRolebyCorpId(long corpId) {
		return 	corporateRoleMapper.findCorporateRolebyCorpId(corpId);
	}
	
	public int countCorporateStaff(long corpId)
	{
		return staffMapper.countCorporateStaff(corpId);
	}
	
	private Team convert(TeamRow s) {
		return new Team(s.getId(), s.getName(),s.getDescription());
	}
}
