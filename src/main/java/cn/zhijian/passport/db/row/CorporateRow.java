package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.statustype.CorporateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorporateRow {

	protected Long id;
	protected String name;
	protected String website;
	protected String address;
	protected String logoResourceId;
	protected String hsCode;
	protected String creditCode;
	protected String customArea;
	protected String contactsName;
	protected Integer contactsSex;
	protected String contactsDuties;
	protected String contactsMobile;
	protected String contactsTel;
	protected String corporateMark;
	protected String contactsEmail;
	protected Integer creditLevel;
	protected CorporateEnum isPending;
	protected String auditreason;
	protected String createdBy;
	protected Date createdAt;
	protected String updatedBy;
	protected Date updatedAt;
	protected String walletId;
	protected Integer isUpgrade;
	protected Date useStart;
	protected Date useEnd;
	protected Integer useNum;
	protected Integer industryType;
	protected Integer industry;
	protected Integer nature;
	protected Integer province;
	protected Integer city;
	protected String businessLicense;
}
