package cn.zhijian.passport.statustype;

public enum MessageAccessType implements BaseCodeEnum{
	Pull(0,"拉取"),Push(1,"操作");

	private Integer code;
	private String des;
	MessageAccessType(int code,String des){
		
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
