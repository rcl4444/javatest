package cn.zhijian.passport.domain.events;

import lombok.Data;

@Data
public class PersonLoginOutEvent {
	
	final String sessionid;
	final Long personid;
}