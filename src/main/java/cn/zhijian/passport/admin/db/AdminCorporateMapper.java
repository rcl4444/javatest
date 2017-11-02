package cn.zhijian.passport.admin.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.admin.row.SerialNumberRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.ResourceRow;

public interface AdminCorporateMapper {
	
	@Select("select * from Corporate where id = #{id}")
	CorporateRow load(@Param("id") long id);
	
	@Update("update Corporate set isPending = ${isPending.getCode()}, corporateMark = #{corporateMark},auditreason = #{auditreason}  where id = #{id}")
	int updateCorporate(CorporateRow row);
	
	@Select("select top 1 * from serialNumber order by id")
	SerialNumberRow findSerialNumberTop();
	
	@Insert("insert into serialNumber (nid) values(#{nid})")
	int insertSerialNumber(SerialNumberRow row);
	
	@Select("select * from resource where id = #{id}")
	ResourceRow loadResource(@Param("id") String id);
	
	@Select("select * from Corporate c inner join staff s on c.id=s.corporateid and s.role='OWNER' where c.id=#{corporateid}")
	PersonRow findOwner(@Param("corporateid")long corporateid);
}
