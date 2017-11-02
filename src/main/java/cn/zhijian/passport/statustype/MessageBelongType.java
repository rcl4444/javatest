package cn.zhijian.passport.statustype;

public enum MessageBelongType implements BaseCodeEnum{
	Person(0,"个人"),Corporate(1,"公司");

	private Integer code;
	private String des;
	MessageBelongType(Integer code,String des){
		
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
