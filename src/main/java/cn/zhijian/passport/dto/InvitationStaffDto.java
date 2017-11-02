package cn.zhijian.passport.dto;

import lombok.Data;

@Data
public class InvitationStaffDto {

	final Long id;// 申请ID
	final String accountNo;// 账号
	final String relName;// 姓名
	final String phone;// 手机
	final String mark;// 备注
	final Integer status;// 状态
}