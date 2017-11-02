package cn.zhijian.trade.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderDetailsDto {
	final List<ProductsDto> products;
}
