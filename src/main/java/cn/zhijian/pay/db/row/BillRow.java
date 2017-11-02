package cn.zhijian.pay.db.row;

import java.util.Date;

import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.Pay.PayType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillRow {
	Long id;
	String billNo;
	long orderId;
	double money;
	long source;
	Long target; //目标账号
	String tradeType; //微信使用字段
	PayType payType; //支付类型 微信、余额
	BillType billType;
	String walletId;
	String userName;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
