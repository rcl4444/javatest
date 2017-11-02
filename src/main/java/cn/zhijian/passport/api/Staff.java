package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class Staff {

	final Long id;//员工ID
	final String accountNo;//用户名
	final String relName;//用户真实姓名
	final String workNo;//工号
	final String birthOrigin;//户籍地址
	final String email;//邮箱
	final String phone;//手机号码
	final String eduBg;//学历
	final String qualifi;//资格证书
	final String strongPoint;//能力优势
	final String mark;//备注
}
