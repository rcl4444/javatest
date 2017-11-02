package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.StaffList;
import lombok.Data;

@Data
public class TeamAddStaffCommand {
	final long corpId;
	final long teamid;
	final StaffList staffids;
	final String username;
}
