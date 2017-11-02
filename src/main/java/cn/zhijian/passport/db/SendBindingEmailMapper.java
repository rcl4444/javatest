package cn.zhijian.passport.db;

import java.util.Date;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.db.row.SendBindingEmailRow;

public interface SendBindingEmailMapper {
	@Insert("insert into SendBindingEmail (personId,bindingCode,email,createdBy,createdAt)"
			+ "values (#{personId}, #{bindingCode}, #{email}, #{createdBy}, #{createdAt})")
	int insert(SendBindingEmailRow row);
	
	@Select("select * from SendBindingEmail where bindingCode = #{bindingCode}")
	SendBindingEmailRow findSendBindingEmailbyCode(@Param("bindingCode") String bindingCode);
}
