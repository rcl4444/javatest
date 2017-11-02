package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.CurrSessionRow;

public interface CurrSessionMapper {

	@Insert("insert into currsession(sessionid,personid,content) values (#{row.sessionid},#{row.personid},#{row.content})")
	long insert(@Param("row")CurrSessionRow row);
	
	@Update("update currsession set personid=#{personid},content=#{content} where sessionid=#{sessionid}")
	int update(CurrSessionRow row);
	
	@Delete("delete currsession where sessionid=#{sessionid}")
	int delete(String sessionid);
	
	@Select("select * from currsession;")
	List<CurrSessionRow> loadAll();
	
	@Select("select * from currsession where sessionid=#{sessionid}")
	CurrSessionRow load(String sessionid);
}
