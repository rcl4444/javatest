package cn.zhijian.trade.api;

import java.util.List;

import cn.zhijian.pay.api.Pay.BehaviorType;
import lombok.Data;

@Data
public class Trade {
	final BehaviorType behaviorType;
	final List<Products> products;
	
	@Data
	public static class Products {
		final Long productId;
		final String productName;
		final Integer useNum;
		final String usePeriod;
		final Double price;
		final Double peoplePrice;
		final Double productPrice;
	}
}
