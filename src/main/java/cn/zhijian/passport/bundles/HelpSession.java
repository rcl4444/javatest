package cn.zhijian.passport.bundles;

import org.axonframework.eventhandling.EventBus;

public class HelpSession {

	protected static EventBus finger;
	
	static void setEventBus(EventBus eb){
		finger = eb;
	}
	
	public static EventBus getEventBus(){
		return finger;
	}
}
