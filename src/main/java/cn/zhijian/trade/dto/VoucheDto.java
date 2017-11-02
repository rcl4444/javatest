package cn.zhijian.trade.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class VoucheDto {
	final String voucherNo;
	final int useNum;
	final String usePeriod;
	final Date startTime;
	final Date endTime;
	final Date createdAt;
	final List<VoucheDetailsDto> applications;
	final String productName;
	final String productIntr;
	final byte[] productIntrImg;
}
