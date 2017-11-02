package cn.zhijian.passport.api;

import java.security.Principal;
import java.util.Date;

import lombok.Data;

@Data
public class Person implements Principal {

	final Long id;
	final String username;
	final String name;
	final String email;
	final String mobile;
	final String avatar;
	final String realName;
	final Integer sex;
	final Date birthday;
	final String school;
	final String qq;
	final String wx;
	final Integer isBindingEmail;
	final passwordStrengthType passwordStrength;
	final String infoCompletion;
	final String walletId;
	public static enum passwordStrengthType {
		WEAK , MEDIUM, STRONG
	}
}
