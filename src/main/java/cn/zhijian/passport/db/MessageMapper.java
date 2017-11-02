package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.db.row.MessageAfficheDetailRow;
import cn.zhijian.passport.db.row.MessageOpertionDetailRow;
import cn.zhijian.passport.db.row.MessageRow;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageType;



public interface MessageMapper {

	@Insert("insert into message(content,belongtype,messagetype,opertiontype,personid,corporateid,accesstype,isread,isdelete,createdAt) "
			+ "values(#{content},${belongtype.getCode()},${messagetype.getCode()},${opertiontype.getCode()},#{personid},"
			+ "#{corporateid},${accesstype.getCode()},#{isread},#{isdelete},#{createdAt})")
	@Options(useGeneratedKeys = true)
	long insert(MessageRow row);
	
	@Select("select * from message where id=#{id}")
	MessageRow load(Long id);
	
	@Update("<script>"
			+ "update message set isread=true where id in "
			+ "<foreach collection=\"messageids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</script>")
	long readMessage(@Param("messageids")List<Long> messageids);
	
	@Update("<script>"
			+ "update message set isdelete=true where id in "
			+ "<foreach collection=\"messageids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</script>")
	long deleteMessage(@Param("messageids")List<Long> messageids);
	
	@Insert("insert into messageaffichedetail(messageid,content) values (#{messageid},#{content})")
	long insertAfficheDetail(MessageAfficheDetailRow row);
	
	@Insert("insert into messageopertiondetail(messageid,linktxt,linkurl,linkclass) values (#{messageid},#{linktxt},#{linkurl},#{linkclass})")
	long insertOpertiondetail(MessageOpertionDetailRow row);
	
	@Select("<script>"
			+ "select * from message where messagetype=${messagetype.getCode()} and accesstype=${accesstype.getCode()}"
			+ " and belongtype=${belongtype.getCode()} and isread=#{isread} and isdelete=#{isdelete}"
			+ " and personid=#{personid} and "
			+ "<choose>"
			+ "<when test=\"corporateid==null\">corporateid is null</when>"
			+ "<otherwise>corporateid=#{corporateid}</otherwise>"
			+ "</choose>"
			+ "</script>")
	List<MessageRow> getMessage(@Param("messagetype") MessageType messagetype,@Param("accesstype") MessageAccessType accesstype,
			@Param("belongtype") MessageBelongType belongtype,
			@Param("isread") Boolean isread,
			@Param("isdelete") Boolean isdelete,
			@Param("personid") Long personid,
			@Param("corporateid") Long corporateid);
	
	@Select("select * from messageaffichedetail where messageid=#{messageid}")
	List<MessageAfficheDetailRow> getAfficheDetail(Long messageid);
	
	@Select("<script>select * from messageopertiondetail where messageid in"
			+ "<foreach collection=\"messageids\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach>"
			+ "</script>")
	List<MessageOpertionDetailRow> getOpertionDetail(@Param("messageids")List<Long> messageids);
	

}
