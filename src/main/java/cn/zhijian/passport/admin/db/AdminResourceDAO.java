package cn.zhijian.passport.admin.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.admin.row.AdminResourceRow;

/**
 * 资源上下载DAO
 * 
 * @author kmtong
 *
 */
public interface AdminResourceDAO {

	@Insert("insert into adminresource (id, name, contentType, content, ownerId, createdBy, createdAt, updatedBy, updatedAt)" //
			+ " values (#{id}, #{name}, #{contentType}, #{content}, #{ownerId}, #{createdBy}, #{createdAt}, #{updatedBy}, #{updatedAt})")
	int insert(AdminResourceRow row);

	@Select("select * from adminresource where id = #{id}")
	AdminResourceRow load(@Param("id") String id);
	
	@Select("<script>"
			+ "<if test=\"ids != null and ids.size() > 0\">"
			+ "select * from adminresource where id in "
			+ "<foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if></script>")
	List<AdminResourceRow> findByIds(@Param("ids")List<String> ids);
}
