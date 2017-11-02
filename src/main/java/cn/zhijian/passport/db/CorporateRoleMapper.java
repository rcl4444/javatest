package cn.zhijian.passport.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.CorporateModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;
import cn.zhijian.passport.db.row.PersonAppOperationRow;
import cn.zhijian.passport.db.row.RoleOperationRow;
import cn.zhijian.passport.db.row.StaffRow;

public interface CorporateRoleMapper {

	@Insert("insert into corporaterole(corporateid,rolename,description,createdBy,createdAt)"
			+ " values (#{corporateid},#{rolename},#{description},#{createdBy},#{createdAt})")
	@Options(useGeneratedKeys = true)
	int insert(CorporateRoleRow row);
	
	@Update("update corporaterole set rolename=#{c.rolename},description=#{c.description},updatedBy=#{c.updatedBy},updatedAt=#{c.updatedAt}"
			+ " where id = #{id} and corporateid=#{c.corporateid}")
	int update(@Param("c") CorporateRoleRow row, @Param("id") Long id);
	
	@Select("select * from corporaterole where rolename=#{rolename} and corporateid=#{corporateid}")
	List<CorporateRoleRow> findByRoleName(@Param("rolename") String rolename,@Param("corporateid")Long corporateid);
	
	@Select("select * from corporaterole where rolename=#{rolename} and corporateid=#{corporateid} and id <> #{id}")
	List<CorporateRoleRow> findByRoleNameAndId(@Param("rolename") String rolename,@Param("corporateid")Long corporateid,@Param("id") long id);
	
	@Select("<script>select count(*) from corporaterole where corporateId = #{corp_id} "
			+ "<if test=\"query !=null\"> and rolename like #{query}</if></script>")
	int countByCorporateRole(@Param("query") String query,@Param("corp_id") long corpId);
	
	@Select("<script>select * from corporaterole where corporateId = #{corp_id} "
			+ "<if test=\"query !=null\"> and rolename like #{query}</if> order by id " //
			+ "limit #{limit} offset #{offset} </script>")
	List<CorporateRoleRow> loadByCorporateRole(@Param("query") String query,@Param("corp_id") long corpId, @Param("offset") int offset,
			@Param("limit") int limit);
	
	@Delete("delete from corporaterole where id = #{id} and corporateId=#{corp_id}")
	int delete(@Param("id") long id,@Param("corp_id") long corp_id);
	
	@Select("select s.* from staff s inner join roleStaff t on s.id = t.staffId"
			+ " where t.corporateId = #{corpId} and t.roleId = #{roleId} and s.role!='OWNER'")
	List<StaffRow> findStaffInnerJoinbyId(@Param("corpId") long corpId, @Param("roleId") long roleId);
	
	@Select("<script>"
			+ "select * from applicationmodule "
			+ "<if test=\"appids!=null and appids.size()>0\">"
			+ "where applicationid in "
			+ "<foreach collection=\"appids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</if>"
			+ "</script>")
	List<ApplicationModuleRow> getAppModuleByApp(@Param("appids")List<Long> appids);
	
	@Select("<script>"
			+ "select * from moduleoperation "
			+ "<if test=\"moduleids!=null and moduleids.size()>0\">"
			+ "where moduleid in "
			+ "<foreach collection=\"moduleids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</if>"
			+ "</script>")
	List<ModuleOperationRow> getAppModuleOperationByModule(@Param("moduleids")List<Long> moduleids);
	
	@Select("<script>"
			+ "select * from moduleoperation "
			+ "<if test=\"operationids!=null and operationids.size()>0\">"
			+ "where id in "
			+ "<foreach collection=\"operationids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</if>"
			+ "</script>")
	List<ModuleOperationRow> getAppModuleOperationById(@Param("operationids")List<Long> operationids);
	
	@Select("select * from roleoperation where roleid=#{roleid}")
	List<RoleOperationRow> getRolePower(Long roleid);
	
	@Select("select * from corporaterole where id=#{id}")
	CorporateRoleRow load(Long id);
	
	@Insert("insert into roleoperation(roleid,voucherid,applicationid,moduleid,operationid,createdate) values "
			+ "(#{roleid},#{voucherid},#{applicationid},#{moduleid},#{operationid},#{createdate})")
	@Options(useGeneratedKeys = true)
	int insertRoleOperation(RoleOperationRow row);
	
	@Delete("<script>"
			+ "delete from roleoperation where "
			+ "id in "
			+ "<foreach collection=\"ids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</script>")
	int deleteRoleOperationById(@Param("ids")List<Long> roleOperationIds);
	
	@Delete("delete from roleoperation where roleid=#{roleid} ")
	int deleteRoleOperationByRoleId(@Param("roleid") Long roleid);
	
	@Select("select count(*) from rolestaff where roleid=#{roleid}")
	int roleStaffCount(Long roleid);
	
	@Select("select distinct am.modulename,mo.operationname "
			+ "from application a inner join applicationmodule am on a.id = am.applicationid "
			+ "inner join moduleoperation mo on am.id = mo.moduleid "
			+ "inner join roleoperation ro on ro.operationid = mo.id "
			+ "inner join voucher v on v.id = ro.voucherid "
			+ "inner join corporaterole r on ro.roleid = r.id "
			+ "inner join rolestaff rs on r.id = rs.roleid "
			+ "inner join staff s on rs.staffid = s.id and s.role='STAFF' "
			+ "where s.personid=#{personid} and s.corporateid=#{corporateid} and a.id=#{appid} and v.endtime>#{daybreak} "
			+ "union "
			+ "select distinct am.modulename,mo.operationname "
			+ "from corporate c inner join voucher v on c.walletid = v.walletid "
			+ "inner join snapshotapplication sa on v.snapshotid = sa.snapshotid "
			+ "inner join snapshotapplicationmodule sam on sam.applicationid = sa.applicationid "
			+ "inner join applicationmodule am on sam.moduleid = am.id "
			+ "inner join moduleoperation mo on am.id = mo.moduleid "
			+ "where exists(select 1 from staff where personid=#{personid} and corporateid=#{corporateid} and role='OWNER') "
			+ "and sa.applicationid = #{appid} and v.endtime>#{daybreak}")
	List<PersonAppOperationRow> getPersonAppOperation(@Param("personid")Long personid, @Param("corporateid")Long corporateid, 
			@Param("appid")Long appid, @Param("daybreak")Date daybreak);
	
	@Select("select cr.* from rolestaff rs inner join corporaterole cr on rs.roleid = cr.id where rs.staffid=#{staffid}")
	List<CorporateRoleRow> findRoleByStaffId(@Param("staffid")Long staffid);
	
	@Select("select * from module")
	List<CorporateModuleRow> findAllModule();
	
	@Select("<script>select * from module where id in "
			+ "<foreach collection=\"ids\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
			+ "</script>")
	List<CorporateModuleRow> findAllModulebyId(@Param("ids") List<Long> ids);
	
	@Select("select * from roleModule where corporateId = #{corporateId} and roleId = #{roleId}")
	List<CorporateRoleModuleRow> findRoleModulebyCorpId(@Param("corporateId") long corporateId,@Param("roleId") long roleId);
	
	@Select("select * from roleModule where roleId = #{roleId}")
	List<CorporateRoleModuleRow> findRoleModulebyRoleId(@Param("roleId") long roleId);

	@Delete("delete from roleModule where roleId = #{roleId} and corporateId = #{corporateId}")
	int deleteRoleModule(@Param("roleId") long roleId, @Param("corporateId") long corporateId);
	
	@Insert("insert into roleModule (roleId, moduleId, corporateId, createdBy, createdAt ) values (#{roleId}, #{moduleId}, #{corporateId}, #{createdBy}, #{createdAt})")
	int insertRoleModule(CorporateRoleModuleRow row);
	
	@Select("<script>select * from module where id in (select moduleId from roleModule where "
			+ "<if test=\"roleIds!=null and roleIds.size()>0 \">roleId in "
			+ "<foreach collection=\"roleIds\" item=\"roleId\" separator=\",\" open=\"(\" close=\")\">#{roleId}</foreach> "
			+ "and</if> corporateId = #{corporateId})</script>")
	List<CorporateModuleRow> findCorporateModulebyRoleid(@Param("roleIds") List<Long> roleIds, @Param("corporateId") long corporateId);
	
	@Select("select * from module")
	List<CorporateModuleRow> findCorporateModule();
	
	@Select("select * from corporaterole where corporateid = #{corporateid}")
	List<CorporateRoleRow> findCorporateRolebyCorpId(@Param("corporateid") long corporateid);
	
	@Delete("delete from corporaterole where corporateId=#{corporateid}")
	int deleteByCorporateId(@Param("corporateid") long corporateid);
}
