package cn.zhijian.passport.admin.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CorporateAuditDto{
	final Long id;
	final String corporateName;
	final String creditCode;
	final String hsCode;
	final Integer isPending;
	final Date createdAt;
}
