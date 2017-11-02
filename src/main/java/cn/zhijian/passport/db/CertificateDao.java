package cn.zhijian.passport.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.CertificateRow;


public interface CertificateDao {

	@Insert("insert into Certificate(personid,applicationid,code,codeexpiresdate,token,tokenexpiresdate,refreshtoken,isrefresh,createdate,corporateid)"
			+ " values (#{personid},#{applicationid},#{code},#{codeexpiresdate},#{token},#{tokenexpiresdate},#{refreshtoken},#{isrefresh},#{createdate},#{corporateid})")
	@Options(useGeneratedKeys = true)
	long insert(CertificateRow row);
	
	@Update("update Certificate set personid=#{c.personid},applicationid=#{c.applicationid},code=#{c.code},codeexpiresdate=#{c.codeexpiresdate},"
			+ "token=#{c.token},tokenexpiresdate=#{c.tokenexpiresdate},refreshtoken=#{c.refreshtoken},isrefresh=#{c.isrefresh},createdate=#{c.createdate},corporateid=#{c.corporateid}"
			+ " where id = #{id}")
	int update(@Param("c") CertificateRow row, @Param("id") Long id);
	
	@Select("select top 1 * from Certificate where code=#{code} order by createdate desc")
	CertificateRow findByCode(@Param("code") String code);
	
	@Select("select top 1 * from Certificate where refreshtoken=#{refreshtoken} order by createdate desc")
	CertificateRow findByRefreshToke(@Param("refreshtoken") String refreshtoke);
	
	@Select("select top 1 * from Certificate where token=#{token} order by createdate desc")
	CertificateRow findByToke(@Param("token") String token);
}
