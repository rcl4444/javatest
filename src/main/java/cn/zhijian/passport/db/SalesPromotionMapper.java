package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.BusinessEventRow.EventStatus;
import cn.zhijian.passport.db.row.GiftProductView;
import cn.zhijian.passport.db.row.SalesPromotionRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftMainRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftProductRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftPromotionView;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftType;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;

public interface SalesPromotionMapper {

	@Select("select * from salespromotion where id=#{0}")
	SalesPromotionRow load(Long id);
	
	@Insert("insert into salespromotion(spname,begindate,enddate,saletype,state,type,showstate,createdBy,createdAt)"
			+ "values (#{spname},#{begindate},#{enddate},${saletype.getCode()},${state.getCode()},"
			+ "${type.getCode()},#{showstate},#{createdBy},#{createdAt})")
	@Options(useGeneratedKeys = true)
	Long insert(SalesPromotionRow row);
	
	@Insert("insert into giftmain(salespromotionid,eventid,promotiontitle,gifttype) values (#{salespromotionid},#{eventid},"
			+ "#{promotiontitle},${gifttype.getCode()})")
	@Options(useGeneratedKeys = true)
	Long insertGiftMain(GiftMainRow row);
	
	@Insert("insert into giftproduct(giftmainid,productid,personnum,duration) values (#{giftmainid},#{productid},#{personnum},#{duration})")
	@Options(useGeneratedKeys = true)
	Long insertGiftProduct(GiftProductRow row);
	
	@Update("update salespromotion set spname=#{r.spname},begindate=#{r.begindate},enddate=#{r.enddate},"
			+ "saletype=${r.saletype.getCode()},state=${r.state.getCode()},type=${r.type.getCode()},"
			+ "showstate=#{r.showstate},createdBy=#{r.createdBy},createdAt=#{r.createdAt} where id=#{r.id}")
	int update(@Param("r")SalesPromotionRow row);
	
	@Update("update giftmain set salespromotionid=#{r.salespromotionid},eventid=#{r.eventid},promotiontitle=#{r.promotiontitle},"
			+ "gifttype=${r.gifttype.getCode()} where id=#{r.id}")
	int updateGiftMain(@Param("r")GiftMainRow row);
	
	@Select("<script>"
			+ "select s.id,s.spname,g.promotiontitle,s.begindate,s.enddate,b.eventname,g.gifttype,s.createdAt,s.state "
			+ "from salespromotion s inner join giftmain g on s.id = g.salespromotionid "
			+ "inner join businessevent b on g.eventid = b.id and s.type = b.businesstype "
			+ "where 1=1 <if test=\"q.query!=null and q.query.size()>0\"> and"
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
	List<GiftPromotionView> getGiftPromotionPaging(@Param("q") PagingQuery query);
	
	@Select("<script>"
			+ "select count(*) "
			+ "from salespromotion s inner join giftmain g on s.id = g.salespromotionid "
			+ "inner join businessevent b on g.eventid = b.id and s.type = b.businesstype "
			+ "where 1=1 <if test=\"q.query!=null and q.query.size()>0\"> and"
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
	int getGiftPromotionCount(@Param("q") PagingQuery query);
	
	@Select("select gp.productid from salespromotion s inner join giftmain g on s.id = g.salespromotionid "
			+ "inner join giftproduct gp on g.id = gp.giftmainid and g.gifttype=${gtype.getCode()} "
			+ "where s.state=${state.getCode()} "
			+ "and s.begindate<=now() and s.enddate>=now()")
	List<Long> getPromotionGiftProductId(@Param("gtype")GiftType type,@Param("state")SalesPromotionStatus state);
	
	@Select("select gp.productid from giftmain g inner join giftproduct gp on g.id = gp.giftmainid and g.gifttype=${gtype.getCode()} "
			+ "where g.salespromotionid=#{spid}")
	List<Long> getGiftProductIdsBySPId(@Param("gtype")GiftType type,@Param("spid")Long spid);
	
	@Select("select * from giftmain where salespromotionid=#{0}")
	GiftMainRow getGiftmain(Long spid);
	
	@Select("select * from giftproduct where giftmainid=#{0}")
	List<GiftProductRow> getGiftproduct(Long gmainid);
	
	@Select("select p.type,p.id as productid,p.productname,gp.personnum,gp.duration "
			+ "from product p inner join giftproduct gp on p.id = gp.productid where gp.giftmainid=#{0}")
	List<GiftProductView> getProductByGiftId(Long gmainid);
	
	@Delete("<script><if test=\"ids!=null and ids.size() > 0\">"
			+ "delete giftproduct where id in "
			+ "<foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if></script>")
	int deleteGiftProduct(@Param("ids")List<Long> ids);
	
	@Select("select sp.type,gp.productid,gp.personnum,gp.duration,p.productname "
			+ "from salespromotion sp inner join giftmain gm on sp.id = gm.salespromotionid "
			+ "inner join giftproduct gp on gm.id = gp.giftmainid "
			+ "inner join product p on gp.productid = p.id "
			+ "inner join businessevent b on gm.eventid = b.id and b.state=${eventState.getCode()}"
			+ "where sp.begindate<=now() and sp.enddate>=now() and sp.state=${spState.getCode()} and b.sign=#{eventSign}")
	List<GiftProductView> getPromotionGift(@Param("eventState")EventStatus eventState,
			@Param("spState")SalesPromotionStatus spState,@Param("eventSign")String eventSign);
}