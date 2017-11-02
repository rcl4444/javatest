package cn.zhijian.trade.api;

import cn.zhijian.pay.api.Pay.DateType;
import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.api.Pay.PayType;
import cn.zhijian.pay.api.Pay.ServiceType;
import lombok.Data;

@Data
public class CreateOrder {
	final String tradeId;
	final String tradeType;
	final String body;
	final PayType payType;
	final OrderType orderType;
	final DateType dateType;
	final ServiceType serviceType;
	final double money;
	final String walletId;
}
