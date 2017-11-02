package cn.zhijian.trade.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeDetailsDto {
	double productPrice;
	double sumPayPrice;
	List<ProductsDto> products;
}
