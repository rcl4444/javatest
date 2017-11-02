package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.db.row.TeamMemberRow;
import cn.zhijian.passport.db.row.TeamRow;

public interface TeamDAO {

	@Insert("insert into team (corporateId, name, description, createdBy, createdAt) values "
			+ "(#{corporateId}, #{name}, #{description}, #{createdBy}, #{createdAt})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	int insert(TeamRow row);
	
	@Update("update team set name=#{name},description=#{description} where id=#{id} and corporateId=#{corporateId}")
	int update(@Param("name") String name,@Param("description") String description,@Param("id") long id, @Param("corporateId") long corporateId);
	
	@Select("select * from team where id = #{id}")
	TeamRow load(@Param("id") long id);

	@Select("select count(*) from team where corporateId = #{corpId} and name like #{query}")
	int countByQueryCorporate(@Param("query") String q, @Param("corpId") long corpId);

	@Select("select * from team where corporateId = #{corpId} and name like #{query} order by createdAt "
			+ "offset #{offset} limit #{limit}")
	List<TeamRow> loadByQueryCorporate(@Param("query") String q, @Param("corpId") long corpId,
			@Param("offset") int offset, @Param("limit") int limit);

	@Select("select count(*) from team where corporateId = #{corpId}")
	int countByCorporate(@Param("corpId") long corpId);

	@Select("select * from team where corporateId = #{corpId} order by createdAt " //
			+ "offset #{offset} limit #{limit}")
	List<TeamRow> loadByCorporate(@Param("corpId") long corpId, @Param("offset") int offset,
			@Param("limit") int limit);

	@Insert("insert into teamMember (corporateId, teamId, staffId, role, createdBy, createdAt) values "
			+ "(#{corporateId}, #{teamId}, #{staffId}, #{role}, #{createdBy}, #{createdAt})")
	long insertMember(TeamMemberRow member);
	
	@Select("select * from team where corporateId = #{corpId}")
	List<TeamRow> findTeambyId(@Param("corpId") long corpId);
	
	@Select("select s.* from staff s inner join teamMember t on s.id = t.staffId"
			+ " where t.corporateId = #{corpId} and t.teamId = #{teamId} and s.role!='OWNER'")
	List<StaffRow> findStaffInnerJoinbyId(@Param("corpId") long corpId, @Param("teamId") long teamId);
	
	@Select("<script>select * from staff where role!='OWNER' and corporateId = #{corpId} and  id not in (select staffId from teamMember where corporateId = #{corpId} "
			+"<if test=\" teamIds.size() !=0\">"
			+ "and teamId in "
			+ "<foreach item=\"teamId\" collection=\"teamIds\" separator=\",\" open=\"(\" close=\")\">#{teamId}</foreach>)"
			+ "</if>"
			+ "</script>")
	List<StaffRow> findStaffNotTeamMemberbyId(@Param("corpId") long corpId, @Param("teamIds") List<String> teamIds);
	
	@Select("select * from staff where corporateId = #{corpId} and role!='OWNER'")
	List<StaffRow> findStaffbyCorpId(@Param("corpId") long corpId);
	
	@Delete("delete from team where id = #{id}")
	int deleteTeam(@Param("id") long id);
	
	@Delete("delete from teamMember where teamId = #{teamId} and corporateId = #{corporateId}")
	int deleteTeamMembers(@Param("teamId") long teamId,@Param("corporateId") long corporateId);
	
	@Select("select * from team t inner join teamMember tm on t.id = tm.teamid where tm.staffId=#{staffId}")
	List<TeamRow> findTeamByStaffId(@Param("staffId")Long staffId);
}
