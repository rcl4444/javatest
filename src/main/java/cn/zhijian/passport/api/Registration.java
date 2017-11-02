package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class Registration {

	final String username;
	final String password;
//	final String name;
//	final String email;
	final String mobile;
	final String code;
	final String invitationCode; // invitation feedback
	final String validationCode; // invitation feedback
}
