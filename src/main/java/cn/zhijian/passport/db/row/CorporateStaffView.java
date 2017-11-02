package cn.zhijian.passport.db.row;

import lombok.Data;

@Data
public class CorporateStaffView {

	Long staffid;//员工id
	String staffpersonname;//员工姓名
	int sex;//性别
	String mobile; //手机
	String email; //邮箱
	Integer blocked; //状态
	String teamname;//部门名
	String rolename;//角色名
}