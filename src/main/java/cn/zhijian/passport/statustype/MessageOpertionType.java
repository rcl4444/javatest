package cn.zhijian.passport.statustype;

public enum MessageOpertionType implements BaseCodeEnum{
	PromptAccountRegister(0,"账号注册"),
	PromptPersonApplyResult(1,"个人申请结果"),
	PromptCorporateApplyResult(2,"公司审批结果"),
	PromptWalletRecharge(3,"钱包充值"),
	PromptOpenNotice(4,"应用开通通知"),
	OperationPersonApply(100,"用户申请"),
	OperationCorporateInvitation(101,"企业邀请"),
	AfficheContent(200,"公告内容"),
	AfficheLink(201,"公告链接");

	private Integer code;
	private String des;
	MessageOpertionType(Integer code,String des){
		
		this.code = code;
		this.des = des;
	}
	
	@Override
	public Integer getCode() {

		return this.code;
	}
	
	public String getDes(){

		return this.des;
	}
}
