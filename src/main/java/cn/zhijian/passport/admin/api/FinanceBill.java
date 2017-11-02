package cn.zhijian.passport.admin.api;

import java.util.Date;

import lombok.Data;

@Data
public class FinanceBill {
	final long target;
	final String flowNo;
	final String oppoSiteAccount;
	final Date sTime;
	final Date eTime;
	final Integer pageNo;
	final Integer pageSize;
}
