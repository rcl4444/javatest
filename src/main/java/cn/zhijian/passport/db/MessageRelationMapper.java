package cn.zhijian.passport.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.db.row.MessageRelationRow;
import cn.zhijian.passport.statustype.MessageSourceType;

public interface MessageRelationMapper {

	@Select("select * from messagerelation where id = #{id}")
	MessageRelationRow load(long id);
	
	@Insert("insert into messagerelation(messageid,sourceid,sourcetype) values (#{r.messageid},#{r.sourceid},${r.sourcetype.getCode()})")
	int insert(@Param("r") MessageRelationRow row);
	
	@Select("select * from messagerelation where sourceid=#{sourceid} and sourcetype=${type.getCode()}")
	MessageRelationRow findBySourceId(@Param("sourceid")long sourceid,@Param("type")MessageSourceType type);
}