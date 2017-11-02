package cn.zhijian.passport.domain.events;

import java.util.Date;

import lombok.Data;

@Data
public class PersonRegisterEvent implements PushEvent{

	final Long personid;
	final Date registertime;
	final String walletid;
	
	@Override
	public String getUniqueSign() {
		return "register";
	}
	
	@Override
	public String getWalletId() {
		return this.walletid;
	}
}