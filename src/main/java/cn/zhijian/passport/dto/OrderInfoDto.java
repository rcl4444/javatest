package cn.zhijian.passport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDto {
	String outTradeNo;
	String totalFee;
	String body;
	String code;
}
