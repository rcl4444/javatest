package cn.zhijian.passport.admin.dto;

import lombok.Data;

@Data
public class CorporateCertDto {
	final Long id;
	final Integer isPending;
	final Integer industryType;
	final Integer industry;
	final Integer nature;
	final Integer province;
	final Integer city;
	final String businessLicense;
}
