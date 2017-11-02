package cn.zhijian.passport.dto;

import lombok.Data;

@Data
public class CorporateStaffDto {

	final Long id;// 员工id
	final String relName;// 员工姓名
	final int sex;// 性别
	final String phone; // 手机
	final String email; // 邮箱
	final Integer blocked; // 状态
	final String teamname;//部门
	final String rolename;//角色
}
