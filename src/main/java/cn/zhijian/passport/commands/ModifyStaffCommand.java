package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class ModifyStaffCommand {

	final Long staffid;//员工ID
	final Long corporateid;//公司ID
	final String username;//用户名
	final String personname;//用户真实姓名
	final String jobnum;//工号
	final String residenceaddress;//户籍地址
	final String email;//邮箱
	final String mobile;//手机号码
	final String schoolrecord;//学历
	final String qualificationrecord;//资格证书
	final String advantage;//能力优势
	
	final String updateusername;
}