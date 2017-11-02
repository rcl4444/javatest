package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class CorporateAudit {
	long id;
	boolean status;
	String reason;
}
