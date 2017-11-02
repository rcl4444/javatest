package cn.zhijian.passport.db.row;

import lombok.Data;

@Data
public class StaffInvitationView {

	Long invitationid;//申请ID
	String username;//账号
	String personname;//姓名
	String mobile;//手机
	String remark;//备注
	Integer status;//状态
}
