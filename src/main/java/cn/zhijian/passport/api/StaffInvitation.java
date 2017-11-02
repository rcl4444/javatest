package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class StaffInvitation {

	final Corporate corporate;
	final EmailAddress inviter;
	final EmailAddress invitee;

}
