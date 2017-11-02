package cn.zhijian.passport.admin.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.statustype.BusinessType;

public interface AdminApplicationMapper {

	@Select("select * from application where id=#{appid}")
	ApplicationRow load(long appid);
	
	@Insert("insert into application(appname,clientid,clientsecret,scope,callbackurl,mainurl,getInfoUrl,loginouturl,createdate,type,avatarresourceid)"
			+ " values (#{appname},#{clientid},#{clientsecret},#{scope},#{callbackurl},#{mainurl},#{getInfoUrl},#{loginouturl},#{createdate},${type.getCode()},"
			+ "#{avatarresourceid})")
	@Options(useGeneratedKeys = true)
	long insert(ApplicationRow row);
	
	@Update("update application set appname=#{c.appname},clientid=#{c.clientid},clientsecret=#{c.clientsecret},scope=#{c.scope},"
			+ "callbackurl=#{c.callbackurl},mainurl=#{c.mainurl},loginouturl=#{c.loginouturl},createdate=#{c.createdate},type=${c.type.getCode()},"
			+ "avatarresourceid=#{c.avatarresourceid},getInfoUrl=#{c.getInfoUrl} "
			+ "where id = #{id}")
	int update(@Param("c") ApplicationRow row, @Param("id") Long id);
	
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
	int getAppCount(@Param("q") PagingQuery query);
	
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
	List<ApplicationRow> getAppList(@Param("q")PagingQuery query);
	
	@Select("<script>"
			+ "select count(*) from application where appname=#{appname} and type=${type.getCode()}"
			+ "<if test=\"ignoreids!=null and ignoreids.size()>0\">"
			+ "and id not in <foreach item=\"em\" collection=\"ignoreids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if>"
			+ "</script>")
	int getAppByName(@Param("appname")String appname,@Param("type")BusinessType type,@Param("ignoreids")List<Long> ignoreids);
	
	@Select("select * from application where type=${type.getCode()}")
	List<ApplicationRow> getAppByType(@Param("type")BusinessType type);
	
	@Select("<script>"
			+ "select * from applicationmodule where applicationid in "
			+ "<foreach item=\"em\" collection=\"appids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</script>")
	List<ApplicationModuleRow> getAppModuleByAppId(@Param("appids")List<Long> appids);
	
	@Select("<script>"
			+ "select * from applicationmodule where id in "
			+ "<foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</script>")
	List<ApplicationModuleRow> getAppModuleByIds(@Param("ids")List<Long> ids);
}