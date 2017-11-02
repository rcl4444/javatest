package cn.zhijian.trade.dto;

import java.util.Date;

import cn.zhijian.pay.api.Pay.BehaviorType;
import lombok.Data;

@Data
public class VoucherListDto {	
	final long voucherId;
	final String voucherNo;
	final int useNum;
	final String usePeriod;
	final Date startTime;
	final Date endTime;
	final Date createdAt;
	final String productName;
	final String productIntr;
	final int isExpired;
}
