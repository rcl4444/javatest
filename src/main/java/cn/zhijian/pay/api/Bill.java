package cn.zhijian.pay.api;

import java.util.Date;

import cn.zhijian.pay.api.Pay.PayType;
import lombok.Data;

@Data
public class Bill {
	final long orderId;
	final double money;
	final Long source; //來源账号
	final Long target; //目标账号
	final String tradeType; //微信使用字段
	final PayType payType; //支付类型 微信、余额
	final String createdBy;
	final Date createdAt;
	final String walletId;
	final String userName;
	
	public static enum BillType{
		INCOME,EXPEND
	}
}
