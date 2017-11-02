package cn.zhijian.passport.domain.events;

public interface PushEvent {

	String getUniqueSign();
	
	String getWalletId();
}