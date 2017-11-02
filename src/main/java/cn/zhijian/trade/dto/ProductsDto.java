package cn.zhijian.trade.dto;

import lombok.Data;

@Data
public class ProductsDto {
	final long productId;
	final String productName;
	final int useNum;
	final String usePeriod;
	final double price;
	final String productIntr;
	final double peoplePrice;
	final double productPrice;
}
