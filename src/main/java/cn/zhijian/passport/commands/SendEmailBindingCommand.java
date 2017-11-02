package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class SendEmailBindingCommand {
	final String sessionId;
	final String email;
}
