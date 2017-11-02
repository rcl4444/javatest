package cn.zhijian.passport.domain.events;

import cn.zhijian.passport.api.LoginContext;
import lombok.Data;

@Data
public class PersonLoginEvent {
	
	final LoginContext context;
}