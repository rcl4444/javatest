package cn.zhijian.passport.api;

import cn.zhijian.passport.statustype.CorporateEnum;
import lombok.Data;

@Data
public class Corporate {

	final Long id;
	final String name;
	final String website;
	final String address;
	final String logo;
	final String hsCode;
	final String creditCode;
	final String customArea;
	final String contactsName;
	final Integer contactsSex;
	final String contactsDuties;
	final String contactsMobile;
	final String contactsTel;
	final String corporateMark;
	final CorporateEnum isPending;
	final String contactsEmail;
	final Integer creditLevel;
	final String walletId;
	final Integer industryType;
	final Integer industry;
	final Integer nature;
	final Integer province;
	final Integer city;
	final String businessLicense;
	final Integer useNum;
}
