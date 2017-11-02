package cn.zhijian.pay.api;

import java.util.Date;

import lombok.Data;

@Data
public class Expenses {
	final String walletId;
	final String body;
	final Date sTime;
	final Date eTime;
	final int pageNo;
	final int pageSize;
}
