package cn.zhijian.passport.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;


public interface ApplicationModuleDao {

	@Insert("insert into applicationmodule(applicationid,modulename,createdate)"
			+ " values (#{applicationid},#{modulename},#{createdate})")
	@Options(useGeneratedKeys = true)
	int insert(ApplicationModuleRow row);
	
	@Insert("insert into moduleoperation(applicationid,moduleid,operationname,createdate)"
			+" values (#{applicationid},#{moduleid},#{operationname},#{createdate})")
	@Options(useGeneratedKeys = true)
	int insertOperation(ModuleOperationRow row);
	
	@Update("update applicationmodule set applicationid=#{applicationid},modulename=#{modulename},createdate=#{createdate}"
			+ " where id = #{id}")
	int update(@Param("c") ApplicationModuleRow row, @Param("id") Long id);
	
	@Select("select * from applicationmodule where applicationid=#{applicationid}")
	List<ApplicationModuleRow> findByApplicationid(@Param("applicationid") Long applicationid);
	
	@Select("select * from moduleoperation where applicationid=#{appid}")
	List<ModuleOperationRow> findOperationByAppid(long appid);
	
	@Select("<script>"
			+ "select mo.* from " 
			+ "applicationmodule am inner join moduleoperation mo on am.id = mo.moduleid "
			+ "inner join (<foreach collection=\"tb\" index=\"index\" item=\"item\" separator=\" union \">"
			+ "select ${item.get(\"appid\")}<if test=\"index==0\"> as appid</if>,"
			+ "'${item.get(\"modulename\")}'<if test=\"index==0\"> as modulename</if>,"
			+ "'${item.get(\"operationname\")}'<if test=\"index==0\"> as operationname</if>"
			+ "</foreach>) tb on am.applicationid = tb.appid and am.modulename = tb.modulename and mo.operationname = tb.operationname"
			+ "</script>")
	List<ModuleOperationRow> findOperationByTable(@Param("tb")List<Map<String,Object>> tb);
	
	@Select("<script>"
			+ "select * from applicationmodule where applicationid=#{appid} and modulename in "
			+ "<foreach collection=\"modulenames\" index=\"index\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</script>")
	List<ApplicationModuleRow> findModuleByName(@Param("appid")long appid,@Param("modulenames")List<String> modulename);
	
	@Select("select * from applicationmodule where id = #{id}")
	ApplicationModuleRow findApplicationModuleById(@Param("id") long id);
	
	@Select("select * from applicationmodule where id in (select distinct applicationmoduleid  from productappmodule where applicationid = #{applicationid})")
	List<ApplicationModuleRow> findByApplicationModuleInApplicationModuleid(@Param("applicationid") Long applicationid);
}
