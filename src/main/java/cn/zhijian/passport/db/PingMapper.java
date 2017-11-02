package cn.zhijian.passport.db;

import org.apache.ibatis.annotations.Select;

public interface PingMapper {

	@Select("SELECT 1")
	int ping();

}
