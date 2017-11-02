package cn.zhijian.passport.converters;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.db.row.CorporateRow;

public class CorporateConverter {

	public static Corporate convertRow(CorporateRow row) {
		return new Corporate(row.getId(), 
				row.getName(),
				row.getWebsite(), 
				row.getAddress(), 
				row.getLogoResourceId(), 
				row.getHsCode(), 
				row.getCreditCode(), 
				row.getCustomArea(), 
				row.getContactsName(),
				row.getContactsSex(), 
				row.getContactsDuties(), 
				row.getContactsMobile(), 
				row.getContactsTel(),
				row.getCorporateMark(),
				row.getIsPending(),
				row.getContactsEmail(),
				row.getCreditLevel(),
				row.getWalletId(),
				row.getIndustryType(),
				row.getIndustry(),
				row.getNature(),
				row.getProvince(),
				row.getCity(),
				row.getBusinessLicense(),
				row.getUseNum());
	}
}
