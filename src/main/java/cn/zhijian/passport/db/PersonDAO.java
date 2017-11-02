package cn.zhijian.passport.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.Person.passwordStrengthType;
import cn.zhijian.passport.db.row.CorporateApplicationRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonApplicationRow;
import cn.zhijian.passport.db.row.PersonCorporateView;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.RegistrationRow;
import cn.zhijian.passport.statustype.CorporateEnum;

/**
 * 有关个人和注册相关DAO
 * 
 * @author kmtong
 *
 */
public interface PersonDAO {

	/**
	 * 个人注册
	 * 
	 * @param reg
	 * @return
	 */
	@Insert("insert into registration (username, password, name, email, mobile, validation_code, createdBy, createdAt)"
			+ " values (#{username}, #{password}, #{name}, #{email}, #{mobile}, #{validation_code}, #{createdBy}, #{createdAt})")
	int insertRegistration(RegistrationRow reg);

	/**
	 * 个人信息
	 * 
	 * @param prs
	 * @return
	 */
	@Insert("insert into person (username, password, name, email, mobile, createdBy, createdAt, passwordStrength, infoCompletion, walletId)"
			+ " values (#{username}, #{password}, #{name}, #{email}, #{mobile}, #{createdBy}, "
			+ "#{createdAt}, #{passwordStrength}, #{infoCompletion}, #{walletId})")
	@Options(useGeneratedKeys = true)
	int insertPerson(PersonRow prs);

	/**
	 * 绑定注册和个人关系
	 * 
	 * @param email
	 * @param personId
	 */
	@Update("update registration set personId = #{pid}, updatedAt = #{updatedAt} where mobile = #{mobile}")
	void updateRegistrationPerson(@Param("mobile") String mobile, @Param("pid") long personId,
			@Param("updatedAt") Date updatedAt);

	/**
	 * 注册验证
	 * 
	 * @param validationCode
	 * @param username
	 * @param password
	 * @return
	 */
	@Select("select * from registration where validation_code = #{validationCode} and "
			+ "username = #{username} and password = #{password}")
	RegistrationRow findByRegistrationConfirmation( //
			@Param("validationCode") String validationCode, //
			@Param("username") String username, //
			@Param("password") String password);

	/**
	 * 登入支持
	 * 
	 * @param username
	 * @param hashedPassword
	 * @return
	 */
	@Select("select * from person where (username = #{username} or mobile = #{username}) and password = #{hashedPassword}")
	PersonRow findPersonByLogin(@Param("username") String username, @Param("hashedPassword") String hashedPassword);

	/**
	 * 注册避免重复支持
	 * 
	 * @param username
	 * @param mobile
	 * @return
	 */
	@Select("select * from person where username = #{username}")
	List<PersonRow> findPersonByUsername(@Param("username") String username);

	@Select("select * from person where mobile = #{mobile}")
	List<PersonRow> finPersonByMobile(@Param("mobile") String mobile);
	
	/**
	 * 注册避免重复支持
	 * 
	 * @param username
	 * @param mobile
	 * @return
	 */
	@Select("select * from registration where  username = #{username} or mobile = #{mobile}")
	List<PersonRow> findRegistrationByUsernameOrMobile(@Param("username") String username,
			@Param("mobile") String mobile);

	/**
	 * 精准查找
	 * 
	 * @param id
	 * @return
	 */
	@Select("select * from person where id = #{id}")
	PersonRow load(@Param("id") long id);

	@Update("update person set avatarResourceId = #{rsc}, infoCompletion = #{infoCompletion} where id = #{id}")
	int changeAvatar(@Param("id") long id, @Param("rsc") String resourceId, @Param("infoCompletion") String infoCompletion);

	@Select("<script>select * from person where email in "
			+ "<foreach item=\"em\" collection=\"emailList\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>" //
			+ "</script>")
	List<PersonRow> findPersonByEmails(@Param("emailList") List<String> emailList);

	/**
	 * 验证重复用户名
	 * 
	 * @param username
	 * @return
	 */
	@Select("select * from registration where username = #{username}")
	List<PersonRow> findRegistrationByUsername(@Param("username") String username);

	/**
	 * 更新用户信息
	 * 
	 * @param username
	 * @return
	 */	
	@Update("update person set username = #{username},name = #{name},"
			+ "email=#{email},mobile=#{mobile},realName=#{realName},"
			+ "sex=#{sex},birthday=#{birthday},school=#{school},"
			+ "qq=#{qq},wx=#{wx},updatedBy=#{updatedBy},updatedAt=#{updatedAt},infoCompletion=#{infoCompletion} where id = #{id}")
	int updatePerson(PersonRow person);
	
	/**
	 * 密码修改
	 * 
	 * @param username
	 * @return
	 */	
	@Update("update person set password = #{password}, passwordStrength = #{passwordStrength} where username = #{username} and mobile = #{mobile}")
	int PasswordReset(@Param("password") String password, @Param("username") String username, 
			@Param("mobile") String mobile,@Param("passwordStrength") passwordStrengthType passwordStrengthType);
	
	/**
	 * 修改密码判断
	 * 
	 * @param username
	 * @return
	 */	
	@Select("select * from person where username = #{username} and mobile = #{mobile}")
	List<PersonRow> findPersonByUsernameAndMobile(@Param("username") String username, @Param("mobile") String mobile);
	
	@Update("update person set isBindingEmail = #{isBindingEmail}, email=#{email} where id = #{id}")
	int updateIsBindingEmail(@Param("isBindingEmail") int isBindingEmail,@Param("email") String email,@Param("id") long id);
	
	@Select("select * from corporate where id in (select corporateId from staff where personId=#{personId} and role='OWNER')")
	List<CorporateRow> getTheCorporateList(Long personId);
	
	@Select("select * from person where username = #{username} limit 1")
	PersonRow findPersonByAdminUsername(@Param("username") String username);
	
	@Select("<script>select c.id as corporateid,case when i.invitationtype is null then 'create' when i.invitationtype=0 then 'join' else 'invite' end companyType,"
			+ "c.name,'pass' as isPending,c.hsCode,c.corporateMark,c.creditCode "
			+ "from corporate c inner join staff s on c.id = s.corporateid "
			+ "left join invitation i on c.id = i.invitercorpid and i.personid = s.personid and s.role='STAFF' and i.iscancel=0 "
			+ "where s.personid =#{personId} "
			+ "<if test=\"isPendings!=null and isPendings.size()>0\"> and c.ispending in"
			+ "<foreach item=\"em\" collection=\"isPendings\" separator=\",\" open=\"(\" close=\")\">${em.getCode()}</foreach></if></script>")
	List<PersonCorporateView> getCorporateInviteList(@Param("personId") Long personId,@Param("isPendings") List<CorporateEnum> isPending);
	
	@Select("select * from person where walletId = #{walletId} ")
	PersonRow findPersonByWalletId(@Param("walletId") String walletId);
	
	@Select("select * from person where id = #{id} ")
	PersonRow findPersonByPId(@Param("id") long id);
	
	@Insert("insert into personapplication (personid,applicationid,isFree,useStart,useEnd) "
			+ "values(#{personid},#{applicationid},#{isFree},#{useStart},#{useEnd})")
	int createPersonApplication(PersonApplicationRow row);
}
