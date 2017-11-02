package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class CorporateInviteAduitCommand {

	final long invitationid;
	final boolean ispass;
	final String operateusername;
}
