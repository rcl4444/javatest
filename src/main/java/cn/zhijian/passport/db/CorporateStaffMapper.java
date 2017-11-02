package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.CorporateStaffView;
import cn.zhijian.passport.db.row.StaffInvitationView;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.statustype.CorporateEnum;

/**
 * 有关个人和注册相关DAO
 * 
 * @author kmtong
 *
 */
public interface CorporateStaffMapper {

	/**
	 * 插入员工数据
	 * 
	 * @param row
	 * @return
	 */
	@Insert("insert into staff (personId,corporateId,personname,jobnum,residenceaddress,email,mobile,schoolrecord,qualificationrecord,"
			+ "advantage,role,createdBy,createdAt,updatedBy,updatedAt,blocked,sex) "
			+ "values (#{personId},#{corporateId},#{personname},#{jobnum},#{residenceaddress},#{email},#{mobile},#{schoolrecord},#{qualificationrecord},"
			+ "#{advantage},#{role},#{createdBy},#{createdAt},#{updatedBy},#{updatedAt},#{blocked},#{sex})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	long insertStaff(StaffRow row);
	
	@Update("update staff set personId=#{personId},corporateId=#{corporateId},personname=#{personname},jobnum=#{jobnum},residenceaddress=#{residenceaddress},"
			+ "email=#{email},mobile=#{mobile},schoolrecord=#{schoolrecord},qualificationrecord=#{qualificationrecord},advantage=#{advantage},role=#{role},"
			+ "createdBy=#{createdBy},createdAt=#{createdAt},updatedBy=#{updatedBy},updatedAt={updatedAt},blocked=#{blocked},sex=#{sex} "
			+ "where id=#{id}")
	long updateStaff(StaffRow row);

	@Select("<script>select s.id as staffid,s.personname as staffpersonname,s.sex,s.mobile,s.email,case when s.blocked then 0 else 1 end as blocked,"
			+ "group_concat(distinct t.name) as teamname,group_concat(distinct r.rolename) as rolename "
			+ "from staff s left join teamMember tm on s.id = tm.staffid "
			+ "left join team t on tm.teamid = t.id "
			+ "left join rolestaff rs on s.id = rs.staffid "
			+ "left join corporaterole r on rs.roleid = r.id "
			+ "where s.role!='OWNER' and "
			+ "s.corporateId in (select corporateId from staff where personId = #{personId} and blocked = 0 and corporateId = #{corp_id}) "
			+ "<if test=\"query!=null and query.length()>0\">"
			+ "and (s.personname like '%#{query}%' or s.email like '%#{query}%' or s.mobile like '%#{query}%') "
			+ "</if>"
			+ "<if test=\"state==1\">"
			+ "and s.blocked=0 "
			+ "</if>"
			+ "<if test=\"state==0\">"
			+ "and s.blocked=1 "
			+ "</if>"
			+ "group by s.id,s.personname,s.sex,s.mobile,s.email,s.blocked order by s.createdAt limit #{limit} offset #{offset}</script>")
	List<CorporateStaffView> loadStaffByQueryCorporate(@Param("query") String query,
			@Param("state") Integer state,@Param("roleid") Integer roleid,
			@Param("corp_id") long corporateId,
			@Param("personId") long personId,
			@Param("offset") int offset,
			@Param("limit") int limit);

	@Select("<script>"
			+ "select count(*) from staff s "
			+ "where s.role!='OWNER' and "
			+ "s.corporateId in (select corporateId from staff where personId = #{personId} and blocked = 0 and corporateId = #{corp_id}) "
			+ "<if test=\"query!=null and query.length()>0\">"
			+ "and ( s.personname like '%#{query}%' or s.email like '%#{query}%' or s.mobile like '%#{query}%')"
			+ "</if>"
			+ "<if test=\"state==1\">"
			+ "and s.blocked=0"
			+ "</if>"
			+ "<if test=\"state==0\">"
			+ "and s.blocked=1"
			+ "</if>"
			+ "</script>")
	int countStaffByQueryCorporate(@Param("query") String query, @Param("state") Integer state,@Param("roleid") Integer roleid,
			@Param("corp_id") long corporateId,@Param("personId") long personId);

	@Select("select * from staff where id = #{id}")
	StaffRow loadStaff(@Param("id") long id);
	
	@Select("<script>select s.id as invitationid,p.username,s.personname,s.mobile,s.remark,"
			+ "case when s.accepted is null then 2 when s.accepted then 1 else 0 end as status "
			+ "from invitation s inner join person p on s.personId = p.id "
			+ "where s.inviterCorpId in (select corporateId from staff where personId = #{personId} and blocked = 0 and corporateId = #{corp_id}) "
			+ "and s.invitationType=#{type} and s.iscancel=0"
			+ "<if test=\"query!=null and query.length()>0\">"
			+ "and ( s.realname like '%#{query}%' or s.email like '%#{query}%' or s.mobile like '%#{query}%')"
			+ "</if>"
			+ "<if test=\"state==2\">"
			+ "and s.accepted is null "
			+ "</if>"
			+ "<if test=\"state==1\">"
			+ "and s.accepted=1"
			+ "</if>"
			+ "<if test=\"state==0\">"
			+ "and s.accepted=0"
			+ "</if>"
			+ " order by s.createdAt limit #{limit} offset #{offset}</script>")
	List<StaffInvitationView> loadInvitationStaffQuery(@Param("query") String query,
			@Param("state") Integer state,
			@Param("type") Integer type,
			@Param("corp_id") long corporateId,
			@Param("personId") long personId,
			@Param("offset") int offset,
			@Param("limit") int limit);

	@Select("<script>"
			+ "select count(*) from invitation s inner join person p on s.personId = p.id "
			+ "where s.inviterCorpId in (select corporateId from staff where personId = #{personId} and blocked = 0 and corporateId = #{corp_id}) "
			+ "and s.invitationType=#{type} and s.iscancel=0"
			+ "<if test=\"query!=null and query.length()>0\">"
			+ "and ( s.realname like '%#{query}%' or s.email like '%#{query}%' or s.mobile like '%#{query}%')"
			+ "</if>"
			+ "<if test=\"state==2\">"
			+ "and s.accepted is null "
			+ "</if>"
			+ "<if test=\"state==1\">"
			+ "and s.accepted=1"
			+ "</if>"
			+ "<if test=\"state==0\">"
			+ "and s.accepted=0"
			+ "</if>"
			+ "</script>")
	int countInvitationStaffQuery(@Param("query") String query, @Param("state") Integer state, @Param("type") Integer type,
			@Param("corp_id") long corporateId,@Param("personId") long personId);
	
	@Select("<script>select s.* from staff s inner join corporate c on s.corporateid = c.id "
			+ "where personid=#{personid} and blocked=0 "
			+ "<if test=\"ispendings!=null and ispendings.size()>0\">and c.ispending in "
			+ "<foreach item=\"em\" collection=\"ispendings\" separator=\",\" open=\"(\" close=\")\">${em.getCode()}</foreach>"
			+ "</if>"
			+ "</script>")
	List<StaffRow> judgePersonBelong(@Param("personid")Long personid,@Param("ispendings") List<CorporateEnum> ispending);
	
	@Select("select * from staff where personid=#{personid} and corporateId=#{corporateId} and blocked=0;")
	StaffRow findStaffByPersonId(@Param("personid")Long personid,@Param("corporateId")Long corporateId);
	
	@Select("select count(*) from staff where corporateid=#{corporateid} and role='STAFF' and blocked=0;")
	int countCorporateStaff(@Param("corporateid")Long corporateid);
	
	@Delete("delete staff where corporateid=#{0}")
	int deleteByCorporateId(long corporateid);
}
