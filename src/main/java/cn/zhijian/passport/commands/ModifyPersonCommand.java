package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Person;
import lombok.Data;

@Data
public class ModifyPersonCommand {
	
	final String sessionId;
	final Person person;
}
