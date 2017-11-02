package cn.zhijian.passport.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.db.row.ResourceRow;

/**
 * 资源上下载DAO
 * 
 * @author kmtong
 *
 */
public interface ResourceDAO {

	@Insert("insert into resource (id, name, contentType, content, ownerId, createdBy, createdAt, updatedBy, updatedAt)" //
			+ " values (#{id}, #{name}, #{contentType}, #{content}, #{ownerId}, #{createdBy}, #{createdAt}, #{updatedBy}, #{updatedAt})")
	int insert(ResourceRow row);

	@Select("select * from resource where id = #{id}")
	ResourceRow load(@Param("id") String id);
	
	@Select("select * from adminresource where id=#{id}")
	AdminResourceRow loadApplicationResource(@Param("id") String id);
}
