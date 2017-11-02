package cn.zhijian.passport.dto;

import cn.zhijian.passport.statustype.CorporateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorporateCertificationDto {

	long id;
	Integer industryType;
	Integer industry;
	Integer nature;
	Integer province;
	Integer city;
	String businessLicense;
	String address;
	Integer creditLevel;
	CorporateEnum isPending;
}
