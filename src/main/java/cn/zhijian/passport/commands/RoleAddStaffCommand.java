package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.StaffList;
import lombok.Data;

@Data
public class RoleAddStaffCommand {
	final long roleId;
	final long corpId;
	final StaffList staffList;
	final String username;
}
