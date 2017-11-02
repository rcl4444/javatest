package cn.zhijian.pay.api;

import lombok.Data;

@Data
public class Pay {
	
	final String outTradeNo;
	final String body;
	final PayType payType;
	final double totalFee;
	final String walletId;
	final String tradeType;
	final int isPayed;
	final OrderType orderType;
	final int corporateNum;
	final int useDay;
	final DateType dateType;
	final ServiceType serviceType;
	final long applicationid;
	final BehaviorType behaviorType;
	public static enum PayType{
		WX,ALIPAY,BALANCE
	}
	public static enum OrderType{
		PAYMENT,RECHARGE,GIFT
	}
	public static enum DateType{
		DAY,MONTH,YEAR
	}
	public static enum ServiceType{
		APPLICATION,INCREMENTAL
	}
	public static enum BehaviorType{
		PERSONAL,ENTERPRISE
	}
}
