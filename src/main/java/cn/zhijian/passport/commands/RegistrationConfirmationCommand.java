package cn.zhijian.passport.commands;

import lombok.Data;

/**
 * 
 * @author kmtong
 *
 */
@Data
public class RegistrationConfirmationCommand {

	final String validationCode;
	final String username;
	final String password;

}
