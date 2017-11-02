package cn.zhijian.trade.dto;

import java.util.Date;
import java.util.List;

import cn.zhijian.pay.api.Pay.PayType;
import lombok.Data;

@Data
public class OrderDto {
	final long orderId;
	final String outTradeNo;
	final Date createdAt;
	final int isPayed;
	final double total;
	final double paymentDue;
	final PayType payType;
	final List<ProductsDto> products;
}
