package cn.zhijian.passport.domain.events;

import java.util.Date;

import lombok.Data;

@Data
public class CorporatePassEvent implements PushEvent {
	
	final Long corporateid;
	final Long personid;
	final Date createtime;
	final String walletid;
	
	@Override
	public String getUniqueSign() {
		return "companyAudit";
	}
	
	@Override
	public String getWalletId() {
		return this.walletid;
	}
}