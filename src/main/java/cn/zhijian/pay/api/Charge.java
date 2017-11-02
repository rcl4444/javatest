package cn.zhijian.pay.api;

import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.api.Pay.PayType;
import lombok.Data;

@Data
public class Charge {
	final String walletId;
	final double price;
	final PayType payType;
	final OrderType orderType;
	final String tradeType;
}
