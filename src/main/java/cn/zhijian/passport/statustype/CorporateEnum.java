package cn.zhijian.passport.statustype;

public enum CorporateEnum implements BaseCodeEnum {

	Audit_Apply(0, "审核申请中"), 
	Audit_Reject(20, "审核否决"), 
	Audit_Pass(10, "审核通过"),
	Authentication_Not(30,"未认证"),
	Authentication_Apply(40,"认证申请中"),
	Authentication_Pass(50, "认证通过"),
	Authentication_Reject(60, "认证否决");
	
	

    private Integer code;
    private String des;
    private CorporateEnum( Integer code, String des) { 
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