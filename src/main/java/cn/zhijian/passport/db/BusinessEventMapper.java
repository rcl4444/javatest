package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.db.row.BusinessEventRow;
import cn.zhijian.passport.db.row.BusinessEventRow.EventStatus;
import cn.zhijian.passport.db.row.BusinessEventRow.EventView;
import cn.zhijian.passport.statustype.BusinessType;

public interface BusinessEventMapper {

	@Select("select * from businessevent where id=#{0}")
	BusinessEventRow load(Long id);
	
	@Select("select * from businessevent where businesstype=${type.getCode()} and state=${state.getCode()}")
	List<BusinessEventRow> findByType(@Param("type")BusinessType type,@Param("state")EventStatus state);
	
	@Select("select distinct sign,eventname from businessevent")
	List<EventView> findUniqueEvent();
}