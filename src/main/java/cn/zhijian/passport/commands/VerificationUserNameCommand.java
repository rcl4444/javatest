package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class VerificationUserNameCommand {

	final String username;
	final String mobile;
}
