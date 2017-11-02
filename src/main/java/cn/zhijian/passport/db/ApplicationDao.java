package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateApplicationRow;
import cn.zhijian.passport.db.row.PersonApplicationRow;
import cn.zhijian.passport.db.row.ProductRow;

public interface ApplicationDao {

	@Select("select * from application where id=#{appid}")
	ApplicationRow load(long appid);
	
	@Insert("insert into application(appname,clientid,clientsecret,scope,callbackurl,mainurl,getInfoUrl,loginouturl,createdate)"
			+ " values (#{appname},#{clientid},#{clientsecret},#{scope},#{callbackurl},#{mainurl},#{getInfoUrl},#{loginouturl},#{createdate})")
	@Options(useGeneratedKeys = true)
	long insert(ApplicationRow row);
	
	@Update("update application set appname=#{c.appname},clientid=#{c.clientid},clientsecret=#{c.clientsecret},scope=#{c.scope},"
			+ "callbackurl=#{c.callbackurl},mainurl=#{c.mainurl},loginouturl=#{c.loginouturl},createdate=#{c.createdate}"
			+ " where id = #{id}")
	int update(@Param("c") ApplicationRow row, @Param("id") Long id);
	
	@Select("select top 1 * from application where clientid=#{clientid}")
	ApplicationRow findByClientid(@Param("clientid") String clientid);
	
	@Select("select a.* from application a where a.id not in (select a.id from application a inner join corporateapplication c on a.id = c.applicationid where corporateid = #{corporateid})")
	List<ApplicationRow> findApplicationNotCorp(@Param("corporateid") long corporateid);
	
	@Select("select a.* from application a inner join corporateapplication c on a.id = c.applicationid where corporateid = #{corporateid} ")
	List<ApplicationRow> findApplicationOnCorp(@Param("corporateid") long corporateid);
	
	@Select("select top 1 * from corporateapplication where applicationid= #{applicationid} and corporateid = #{corporateid}")
	CorporateApplicationRow findCorporateApplicationByAppId(@Param("applicationid") long applicationid, @Param("corporateid") long corporateid);
	
	@Select("select * from application where id = #{id}")
	ApplicationRow findApplicationbyId(@Param("id") Long id);
	
	@Select("select top 1 * from personapplication where applicationid= #{applicationid} and personid = #{personid}")
	PersonApplicationRow findPersonApplicationByAppId(@Param("applicationid") long applicationid, @Param("personid") long personid);
	
	@Select("select * from application where loginouturl is not null and loginouturl !=''")
	List<ApplicationRow> findSendLoginOutApp();
	
	@Select("select * from application where clientid=#{clientid} and clientsecret=#{clientsecret}")
	ApplicationRow findAppByClient(@Param("clientid")String clientid,@Param("clientsecret")String clientsecret);
	
	@Select("<script>"
			+ "select count(*) from application where 1=1 "
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
	int getAppCount(PagingQuery query);
	
	@Select("<script>"
			+ "select * from application where 1=1 "
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
	List<ApplicationRow> getAppList(PagingQuery query);
	
	
	@Select("select * from application where id in (select distinct applicationId from productappmodule where productid =#{ProductId})")
	List<ApplicationRow> findApplicationsByProductId(long ProductId);
	
	@Select("select * from product where id =#{productid}")
	ProductRow findProductById(long ProductId);
}
