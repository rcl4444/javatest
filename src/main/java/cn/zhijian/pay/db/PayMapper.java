package cn.zhijian.pay.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.db.row.BillOrderRow;
import cn.zhijian.pay.db.row.BillRow;
import cn.zhijian.pay.db.row.WalletRow;

public interface PayMapper {

	@Insert("insert into Wallet (id,balance,createdBy,createdAt) values(#{id},#{balance},#{createdBy},#{createdAt})")
	int CreateWallet(WalletRow row);

	@Insert("update Wallet set balance = #{balance} where id = #{id}")
	int ModityWalletBalance(@Param("balance") double balance, @Param("id") String id);

	@Select("select * from wallet where id = #{id}")
	WalletRow findWalletbyId(@Param("id") String id);
	
	@Select("SELECT sum(money) balance FROM BILL b inner join orderData o on b.orderId = o.id where o.isPayed=#{isPayed} and b.walletId= #{walletId}")
	WalletRow GetBalance(@Param("isPayed") int isPayed,@Param("walletId") String walletId);
	
	@Update("update orderData set isPayed=#{isPayed} where outTradeNo=#{outTradeNo}")
	int UpdateIsPayed(@Param("outTradeNo") String outTradeNo, @Param("isPayed") int isPayed);

	@Update("update corporate set useNum = #{useNum}  where id = #{id}")
	int updateIsUpgrade(@Param("useNum") int useNum, @Param("id") long id);
	
	@Insert("insert into bill (billNo,orderId,money,source,target,tradeType,payType,billType,walletId,createdBy,createdAt,userName) "
			+ "values(#{billNo},#{orderId},#{money},#{source},#{target},#{tradeType},#{payType},#{billType},#{walletId},#{createdBy},#{createdAt},#{userName})")
	int insertBill(BillRow row);

	@Select("select * from bill where orderId = #{orderId} and billType=#{billType}")
	BillRow findBillByOrderIdAndBillType(@Param("orderId") long orderId, @Param("billType") BillType billType);

	@Select("select * from bill where billNo = #{billNo} and billType=#{billType}")
	BillRow findBillByBillNoAndBillType(@Param("billNo") String billNo, @Param("billType") BillType billType);
	
	@Select("SELECT count(*) FROM BILL b inner join orderData o on b.orderid = o.id where o.ispayed=#{isPayed} and  b.billtype=#{billtype}"
			+ " and b.updatedat >= #{sTime} and b.updatedat <= #{eTime}"
			+ " and b.walletId=#{walletId}")
	int countbyBill(@Param("isPayed") int isPayed, @Param("billtype") BillType billtype, @Param("sTime") Date sTime,
			@Param("eTime") Date eTime, @Param("walletId") String walletId);
	
	@Select("SELECT o.body,b.money,b.updatedAt FROM bill b inner join orderData o on b.orderId = o.id where o.isPayed=#{isPayed} and  b.billType=#{billtype}"
			+ " and b.updatedat >= #{sTime} and b.updatedat <= #{eTime}"
			+ " and b.walletId=#{walletId} order by b.updatedAt desc limit #{limit} offset #{offset}")
	List<BillOrderRow> findBillFindByWalletId(@Param("isPayed") int isPayed, @Param("billtype") BillType billtype, @Param("sTime") Date sTime,
			@Param("eTime") Date eTime, @Param("walletId") String walletId, @Param("limit") int limit, @Param("offset") int offset);
	
	@Update("update bill set updatedAt = #{updatedAt} where orderId=#{orderId}")
	int updateBillUpdatedAt(@Param("orderId") long orderId, @Param("updatedAt") Date updatedAt);
	
	@Select("SELECT o.body,b.money,b.updatedAt FROM bill b inner join orderData o on b.orderId = o.id where o.isPayed=#{isPayed} and  b.billType=#{billtype} and o.outTradeNo=#{outTradeNo}")
	BillOrderRow findBillRechargeRecords(@Param("isPayed") int isPayed, @Param("billtype") BillType billtype, @Param("outTradeNo") String outTradeNo);

}
