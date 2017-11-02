package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateApplicationRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.RoleStaffRow;
import cn.zhijian.passport.statustype.CorporateEnum;

/**
 * 有关个人和注册相关DAO
 * 
 * @author kmtong
 *
 */
public interface CorporateMapper {

	/**
	 * 插入企业数据
	 * 
	 * @param row
	 * @return
	 */
	@Insert("insert into corporate (name, website, address, hsCode, creditCode, "
			+ "customArea, contactsName, contactsSex, contactsDuties, contactsMobile, "
			+ "contactsTel, corporateMark, createdBy, createdAt, IsPending,contactsEmail,creditLevel,logoResourceId,walletId,isUpgrade,useNum) "
			+ "values (#{name}, #{website}, #{address}, #{hsCode}, #{creditCode}, "
			+ "#{customArea}, #{contactsName}, #{contactsSex}, #{contactsDuties}, "
			+ "#{contactsMobile}, #{contactsTel}, #{corporateMark}, #{createdBy}, #{createdAt},${isPending.getCode()},#{contactsEmail},"
			+ "#{creditLevel},#{logoResourceId},#{walletId},#{isUpgrade},#{useNum})")
	@Options(useGeneratedKeys = true)
	long insert(CorporateRow row);

	@Select("<script>select * from corporate where id in (select distinct corporateId from staff where personId = #{personId} and blocked = 0) and isPending in "
			+ "<foreach item=\"em\" collection=\"status\" separator=\",\" open=\"(\" close=\")\">${em.getCode()}</foreach></script>")
	List<CorporateRow> findCorporatesByPersonId(@Param("personId") long personId, @Param("status") List<CorporateEnum> CorporateEnums);

	@Select("select * from corporate where id in (select corporateId from staff where personId = #{personId} and blocked = 0 and corporateId = #{corp_id})")
	CorporateRow loadCorporateByStaff(@Param("corp_id") long corporateId, @Param("personId") long personId);

	@Update("update corporate set name = #{name}, website = #{website}, address = #{address}, "
			+ "hsCode = #{hsCode}, creditCode = #{creditCode}, " 
			+ "customArea = #{customArea}, contactsName = #{contactsName}, contactsSex = #{contactsSex}, contactsDuties = #{contactsDuties}, " 
			+ "contactsMobile = #{contactsMobile},contactsTel = #{contactsTel}, corporateMark = #{corporateMark}, updatedBy = #{updatedBy}, "
			+ "updatedAt = #{updatedAt} ,isPending=${isPending.getCode()},contactsEmail=#{contactsEmail},creditLevel=#{creditLevel},logoResourceId=#{logoResourceId} " 
			+ "where id = #{id}")
	int update(CorporateRow row);

	@Select("select * from corporate where id = #{id}")
	CorporateRow load(@Param("id") long corporateId);

	@Select("select * from corporate where id in " //
			+ "(select distinct corporateId from staff s inner join person p on s.personId = p.id " //
			+ "where p.email = #{email} and s.blocked = 0)")
	List<CorporateRow> findCorporatesByEmail(@Param("email") String email);
	
	@Select("select * from corporate where name = #{name} and corporateMark=#{corporateMark}")
	CorporateRow findCorporatesByNameAndMark(@Param("name") String name, @Param("corporateMark") String corporateMark);
	
	@Select("<script>select count(*) from corporate where creditCode = #{creditCode}"
			+"<if test=\"debarIds!=null and debarIds.size() > 0 \"> and id not in "
			+ "<foreach item=\"em\" collection=\"debarIds\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if>"
			+ "</script>")
	int countByCreditCode(@Param("creditCode") String creditCode, @Param("debarIds") List<Long> debarIds);
	
	@Select("select * from application where id in (select applicationId from corporateapplication where corporateId=#{corporateId})")
	List<ApplicationRow> findCorporateApplication(Long corporateId);
	
	@Select("select * from application where id in (select applicationId from personapplication where personId=#{personId})")
	List<ApplicationRow> findPersonApplication(Long personId);
	
	@Select("<script>"
			+ "select * from corporate where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
			+ "<foreach collection=\"q.query\" index=\"index\" item=\"item\" separator=\" and\">"
			+ "<if test=\"item.getFilterRange()!=null and item.getFilterRange()!=''\">"
			+ "<choose>"
			+ "<when test=\"item.getOperat()=='like'\">"
			+ " ${item.getColumn()} like '${item.getFilterRange()}%' "
			+ "</when>"
			+ "<otherwise>"
			+ " ${item.getColumn()}<![CDATA[${item.getOperat()}]]>'${item.getFilterRange()}' "
			+ "</otherwise>"
			+ "</choose>"
			+ "</if>"
			+ "</foreach>"
		    + "</if>"
			+ "<if test=\"q.sort!=null and q.sort.keySet().size() > 0\">"
		    + " order by"
		    + "<foreach collection=\"q.sort.keySet()\" item=\"item\" separator=\",\">"
			+ "${item} ${q.sort.get(item)}"
		    + "</foreach>"
			+ "</if>"
			+ " limit ${q.pageSize} offset ${q.pageSize} * (${q.pageNo}-1)"
			+ "</script>")
	List<CorporateRow> getExaminePaging(@Param("q") PagingQuery query);
	
	@Select("<script>"
			+ "select count(*) from corporate where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
			+ "<foreach collection=\"q.query\" index=\"index\" item=\"item\" separator=\" and\">"
			+ "<if test=\"item.getFilterRange()!=null and item.getFilterRange()!=''\">"
			+ "<choose>"
			+ "<when test=\"item.getOperat()=='like'\">"
			+ " ${item.getColumn()} like '${item.getFilterRange()}%'"
			+ "</when>"
			+ "<otherwise>"
			+ " ${item.getColumn()}<![CDATA[${item.getOperat()}]]>'${item.getFilterRange()}'"
			+ "</otherwise>"
			+ "</choose>"
			+ "</if>"
			+ "</foreach>"
		    + "</if>"
			+ "</script>")
	int getExamineCount(@Param("q") PagingQuery query);
	
	@Select("select ispending from corporate where id=#{corporateId}")
	CorporateEnum getCorporateStatus(Long corporateId);
	
	@Insert("insert into roleStaff (roleId,staffId,corporateId,createdBy,createdAt)"
			+ " values(#{roleId}, #{staffId},#{corporateId},#{createdBy},#{createdAt})")
	int insertRoleStaff(RoleStaffRow row);
	
	@Delete("delete from roleStaff where roleId = #{roleId} and corporateId = #{corporateId}")
	int deleteRoleStaff(@Param("roleId") long roleId,@Param("corporateId") long corporateId);
	
	@Select(("select * from roleStaff where corporateId = #{corporateId} and staffId = #{staffId}"))
	RoleStaffRow findRoleStaffbyStaffId(@Param("corporateId") long corporateId,@Param("staffId") long staffId);
	
	@Select("select * from person where id in (select personid from staff where corporateid=#{corporateid} and role='OWNER') limit 1")
	PersonRow findManager(Long corporateid);
	
	@Insert("insert into corporateapplication (corporateid,applicationid,isFree,useStart,useEnd) "
			+ "values(#{corporateid},#{applicationid},#{isFree},#{useStart},#{useEnd})")
	int createCorporateApplication(CorporateApplicationRow row);
	
	@Select("select * from corporate where walletId = #{walletId}")
	CorporateRow findCorporateByWalletId(@Param("walletId") String walletId);
	
	@Select("select * from corporate where id = #{id}")
	CorporateRow findCorporateByCId(@Param("id") long id);
	
	@Update("update corporate set industryType = #{industryType}, industry =#{industry}, "
			+ "nature = #{nature}, province = #{province}, city = #{city}, businessLicense = #{businessLicense}, "
			+ "isPending = ${isPending.getCode()}, address = #{address}, creditLevel = #{creditLevel} "
			+ "where id = #{id}")
	int updateCert(CorporateRow row);
	
	@Select("<script>select c.* from corporate c inner join staff s on c.id=s.corporateid "
			+ "where s.personid=#{personid} and s.role='OWNER' "
			+ "<if test=\"ispendings!=null and ispendings.size()>0\">and c.ispending in"
			+ "<foreach item=\"em\" collection=\"ispendings\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if></script>")
	List<CorporateRow> findCorporateCreateApply(@Param("personid")Long personid,@Param("ispendings")List<Integer> ispendings);

	@Select("select s.* from staff s inner join corporate c on s.corporateid = c.id where s.personid=#{personid} and s.role='OWNER'")
	CorporateRow getOwnerCorporate(@Param("personid") long personid);
	
	@Delete("delete corporate where id = #{0}")
	int deleteCorporate(long corporateid);
	
	@Delete("delete corporateapplication where corporateid=#{0}")
	int deleteCorporateApplication(Long corporateid);
}
