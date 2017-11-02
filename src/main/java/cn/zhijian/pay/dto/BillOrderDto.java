package cn.zhijian.pay.dto;

import java.util.Date;

import lombok.Data;
@Data
public class BillOrderDto {
	final String body;
	final double totalFee;
	final Date createdAt;
}
