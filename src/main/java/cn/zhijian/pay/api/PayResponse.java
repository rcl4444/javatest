package cn.zhijian.pay.api;

import java.util.Date;

import cn.zhijian.pay.api.Pay.OrderType;
import lombok.Data;

@Data
public class PayResponse {
	 String outTradeNo;
	 OrderType orderType;
	 double moery;
	 Date date;
	 PayDetails details;
}
