package cn.zhijian.passport.admin.commands;

import lombok.Data;

@Data
public class AdminLoginCommand {

	final String username;
	final String password;
}
