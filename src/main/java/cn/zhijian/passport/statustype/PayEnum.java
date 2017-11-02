package cn.zhijian.passport.statustype;

public enum PayEnum implements BaseCodeEnum {
	NotPay(0,"未支付"),
	Payed(1,"已支付");
	
	private Integer code;
	private String des;

	private PayEnum(Integer code, String des) {
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
