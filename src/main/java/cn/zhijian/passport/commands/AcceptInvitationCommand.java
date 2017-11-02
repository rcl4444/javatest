package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class AcceptInvitationCommand {

	final String invitationCode;
	final String validationCode;
	final long personId;

}
