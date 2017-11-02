package cn.zhijian.pay.api;

import cn.zhijian.pay.api.Pay.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {
	String outTradeNo;
	String totalFee;
	String body;
	String code;
	String message;
}
