package cn.zhijian.passport.domain.events;

import cn.zhijian.passport.api.LoginContext;
import lombok.Data;

@Data
public class PersonLoginReplaceEvent {

	final String oldsesssionid;
	final LoginContext context;
}