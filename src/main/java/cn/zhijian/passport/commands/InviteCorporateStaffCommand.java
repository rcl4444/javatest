package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Staff;
import lombok.Data;

@Data
public class InviteCorporateStaffCommand {
	
	final long invitationpersonid;
	final String invitationpersonName;
	final String invitationuserName;
	final long corporateId;
	final Staff staffinfo;
}