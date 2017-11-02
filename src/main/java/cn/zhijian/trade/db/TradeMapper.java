package cn.zhijian.trade.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.trade.db.row.OrderDetailsRow;
import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.db.row.SnapshotApplicationModuleRow;
import cn.zhijian.trade.db.row.SnapshotApplicationRow;
import cn.zhijian.trade.db.row.SnapshotModuleOperationRow;
import cn.zhijian.trade.db.row.SnapshotRow;
import cn.zhijian.trade.db.row.TradeDetailsRow;
import cn.zhijian.trade.db.row.TradeRow;
import cn.zhijian.trade.db.row.VoucherRow;

public interface TradeMapper {

	@Insert("insert into orderData "
			+ "(outTradeNo,body,payType,totalFee,walletId,isPayed,createdBy,createdAt,orderType,DateType,serviceType,isDelete)"
			+ " values"
			+ "(#{outTradeNo},#{body},#{payType},#{totalFee},#{walletId},#{isPayed},#{createdBy},#{createdAt},#{orderType},#{DateType},#{serviceType},#{isDelete})")
	@Options(useGeneratedKeys=true,keyProperty="id")
	long CreateOrder(OrderRow row);

	@Insert("insert into orderDetails (orderId,productId,useNum,usePeriod,price,applicationcost,personcost) "
			+ "values(#{orderId},#{productId},#{useNum},#{usePeriod},#{price},#{applicationcost},#{personcost})")
	int insertOrderDetails(OrderDetailsRow row);
	
	@Select("select * from orderData where outTradeNo = #{outTradeNo}")
	OrderRow findOrderbyOutTradeNo(@Param("outTradeNo") String outTradeNo);
	
	@Select("select * from orderData where id = #{id}")
	OrderRow findOrderbyId(@Param("id") long id);
	
	@Update("update orderData set isPayed=#{isPayed} where outTradeNo=#{outTradeNo}")
	int UpdateIsPayed(@Param("outTradeNo") String outTradeNo, @Param("isPayed") int isPayed);
	
	@Select("<script>"
			+ "select count(*) from orderData where (orderType is null or orderType &lt;&gt; 'RECHARGE') and isDelete=0 and createdAt <![CDATA[>=]]> #{sTime} and createdAt <![CDATA[<=]]> #{eTime} and walletId=#{walletId} "
			+ "<if test=\"isPayed != null\">and isPayed = #{isPayed}</if>"
			+ "<if test=\"outTradeNo != null and outTradeNo != ''\">and outTradeNo = #{outTradeNo}</if>"
			+ "</script>")
	int countbyOrder(@Param("isPayed") Integer isPayed, @Param("walletId") String walletId,@Param("outTradeNo")String outTradeNo,
			@Param("sTime") Date sTime, @Param("eTime") Date eTime);

	@Select("<script>select * from orderData where (orderType is null or orderType &lt;&gt; 'RECHARGE') and isDelete=0 and createdAt<![CDATA[>=]]>#{sTime} and createdAt<![CDATA[<=]]>#{eTime} and "
			+ "walletId=#{walletId} "
			+ "<if test=\"isPayed!=null\">and isPayed=#{isPayed} </if>"
			+ "<if test=\"outTradeNo != null and outTradeNo != ''\">and outTradeNo = #{outTradeNo}</if>"
			+ "order by createdAt " + "limit #{limit} offset #{offset}</script>")
	List<OrderRow> loadByOrder(@Param("isPayed") Integer isPayed, @Param("walletId") String walletId,@Param("outTradeNo")String outTradeNo,
			@Param("offset") int offset, @Param("limit") int limit, @Param("sTime") Date sTime,
			@Param("eTime") Date eTime);
	
	@Select("select * from orderDetails where orderId=#{orderId}")
	List<OrderDetailsRow> findOrderDetailsByOrderId(@Param("orderId") long orderId);

	@Insert("insert into trade (id,behaviorType,walletId,createdBy,createdAt) values(#{id},#{behaviorType},#{walletId},#{createdBy},#{createdAt})")
	int insertTrade(TradeRow row); 
	
	@Insert("insert into tradeDetails (tradeId,productId,useNum,usePeriod,price,applicationcost,personcost) "
			+ "values(#{tradeId},#{productId},#{useNum},#{usePeriod},#{price},#{applicationcost},#{personcost})")
	int insertTradeDetails(TradeDetailsRow row);
	
	@Select("select * from tradeDetails where tradeId=#{tradeId}")
	List<TradeDetailsRow> findTradeDetailsByTradeId(@Param("tradeId") String tradeId);
	
	@Select("select * from trade where id = #{id}")
	TradeRow findTradeById(@Param("id") String id);

	@Insert("insert into voucher (voucherNo,orderId,snapshotId,useNum,usePeriod,startTime,endTime,walletId,createdBy,createdAt,productId) "
			+ "values(#{voucherNo},#{orderId},#{snapshotId},#{useNum},#{usePeriod},#{startTime},#{endTime},#{walletId},#{createdBy},#{createdAt},#{productId})")
	int insertVoucher(VoucherRow row);
	
	@Insert("insert into snapshot (createdBy,createdAt) values(#{createdBy},#{createdAt})")
	@Options(useGeneratedKeys = true)
	long insertSnapshot(SnapshotRow row);
	
	@Insert("insert into snapshotApplication (applicationId,snapshotId) values(#{applicationId},#{snapshotId})")
	int insertSnapshotApplication(SnapshotApplicationRow row);
	
    @Insert("insert into snapshotApplicationModule (moduleId,applicationId,moduleName,snapshotId) values(#{moduleId},#{applicationId},#{moduleName},#{snapshotId})")
    int insertsnapshotApplicationModule(SnapshotApplicationModuleRow row);

    @Insert("insert into snapshotModuleOperation (operationId,applicationId,moduleId,operationName,snapshotId) values(#{operationId},#{applicationId},#{moduleId},#{operationName},#{snapshotId})")
    int insertSnapshotModuleOperation(SnapshotModuleOperationRow row);

	@Select("<script>"
			+ "select count(*) from voucher where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
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
	int getCertCount(@Param("q") PagingQuery query);

	@Select("<script>"
			+ "select * from voucher where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
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
	List<VoucherRow> getCertList(@Param("q") PagingQuery query);

	@Select("select * from voucher where id = #{id}")
	VoucherRow findVoucherById(@Param("id") long id);
	
	@Select("select * from snapshot where id = #{id}")
	SnapshotRow findSnapshotById(@Param("id") long id);
	
	@Select("select * from snapshotApplication where snapshotId = #{snapshotId}")
	List<SnapshotApplicationRow> findSnapshotApplicationBySnapshotId(@Param("snapshotId") long snapshotId);
	
	@Select("select * from snapshotApplicationModule where snapshotId = #{snapshotId}")
	List<SnapshotApplicationModuleRow> findSnapshotApplicationModuleBySnapshotId(@Param("snapshotId") long snapshotId);
	
	@Update("update orderData set isDelete=1 where id=#{id}")
	int deleteOrder(@Param("id") long id);
	
	@Select("select * from financeAccount where flag=#{flag}")
	FinanceAccountRow findFinanceAccount(@Param("flag") String flag);
}
