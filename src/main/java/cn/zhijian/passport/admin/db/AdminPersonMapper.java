package cn.zhijian.passport.admin.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.AdminPersonRow;
import cn.zhijian.passport.db.row.PersonRow;

public interface AdminPersonMapper {

	@Update("insert into adminperson (username,password,name,email,mobile,createdBy,createdAt,updatedBy,updatedAt)"
			+ " values (#{username},#{password},#{name},#{email},#{mobile},#{createdBy},#{createdAt},#{updatedBy},#{updatedAt})")
	int insertPerson(AdminPersonRow prs);
	
	@Select("select * from adminperson where username = #{username}")
	AdminPersonRow findPersonByUsername(@Param("username") String username);
	
	@Select("select * from adminperson where id = #{id}")
	AdminPersonRow load(@Param("id") long id);
	

	@Select("<script>"
			+ "select count(*) from person where 1=1 "
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
	int getPersonCount(@Param("q") PagingQuery query);

	@Select("<script>"
			+ "select * from person where 1=1 "
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
	List<PersonRow> getPersonList(@Param("q") PagingQuery query);
}
