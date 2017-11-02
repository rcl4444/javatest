package cn.zhijian.passport.statustype;

public enum BusinessType implements BaseCodeEnum {
	
	Person(0,"个人"),
	Corporate(1,"企业");
	
    private Integer code;
    private String des;
    private BusinessType( Integer code, String des) { 
    	this.code = code; 
    	this.des = des;
    }
    
	@Override
	public Integer getCode() {
		return this.code;
	}

	public String getDes() {
		return this.des;
	}
}