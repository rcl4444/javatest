package cn.zhijian.passport.statustype;

public enum MessageSourceType implements BaseCodeEnum{

	JoinCorporateApply(0,"申请加入公司");

	private Integer code;
	private String des;
	MessageSourceType(Integer code,String des){
		
		this.code = code;
		this.des = des;
	}
	
	@Override
	public Integer getCode() {

		return this.code;
	}
	
	@Override
	public String getDes(){

		return this.des;
	}
}
