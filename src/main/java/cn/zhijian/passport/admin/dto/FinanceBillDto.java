package cn.zhijian.passport.admin.dto;

import java.util.Date;

import lombok.Data;

@Data
public class FinanceBillDto {
	final Date time;
	final String flowNo;
	final String summary;
	final String income;
	final String expend;
	final double balance;
	final String paymentAccount;
	final String oppoSiteName;
	final String oppoSiteAccount;
}
