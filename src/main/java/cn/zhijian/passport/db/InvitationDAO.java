package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.InvitationRow;
import cn.zhijian.passport.db.row.JoinCorporateApplyView;

public interface InvitationDAO {

	@Insert("insert into invitation (inviterCorpId,inviterId,personId,username,personname,jobnum,residenceaddress,email,mobile,"
			+ "schoolrecord,qualificationrecord,advantage,invitationType,accepted,acceptedAt,createdBy,"
			+ "createdAt,remark) "
			+ "values (#{inviterCorpId},#{inviterId},#{personId},#{username},#{personname},#{jobnum},#{residenceaddress},#{email},#{mobile},"
			+ "#{schoolrecord},#{qualificationrecord},#{advantage},#{invitationType},#{accepted},#{acceptedAt},#{createdBy},"
			+ "#{createdAt},#{remark})")
	@Options(useGeneratedKeys = true)
	int insert(InvitationRow row);

	@Update("update invitation set inviterCorpId=#{inviterCorpId},inviterId=#{inviterId},personId=#{personId},"
			+ "username=#{username},personname=#{personname},jobnum=#{jobnum},residenceaddress=#{residenceaddress},"
			+ "email=#{email},mobile=#{mobile},schoolrecord=#{schoolrecord},qualificationrecord=#{qualificationrecord},"
			+ "advantage=#{advantage},invitationType=#{invitationType},accepted=#{accepted},acceptedAt=#{acceptedAt},createdBy=#{createdBy},"
			+ "createdAt=#{createdAt},remark=#{remark},iscancel=#{iscancel} where id=#{id} ")
	int update(InvitationRow row);
	
	@Select("select * from invitation where id=#{id}")
	InvitationRow load(long id);
	
	@Select("select c.id as corporateid,c.name as corporatename,i.accepted as accepted,c.hscode,c.corporatemark,c.creditcode,i.createdAt as applydate "
			+ "from invitation i inner join corporate c on i.invitercorpid  = c.id "
			+ "where i.personId=#{personid} and i.invitationType=0 and i.iscancel=0")
	List<JoinCorporateApplyView> findJoinCorporateApply(@Param("personid")Long personid);
	
	@Select("select i.* from invitation i inner join corporate c on i.invitercorpid  = c.id "
			+ "where i.personId=#{personid} and i.invitationType=0 and i.iscancel=0 order by i.createdAt desc limit 1")
	InvitationRow findLastJoinCorporateApply(@Param("personid")Long personid);
}
