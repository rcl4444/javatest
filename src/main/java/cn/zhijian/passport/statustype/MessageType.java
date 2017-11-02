package cn.zhijian.passport.statustype;

public enum MessageType implements BaseCodeEnum{
	Prompt(0,"提示"),Operation(1,"操作"),Affiche(2,"公告");

	private Integer code;
	private String des;
	MessageType(Integer code,String des){
		
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
