package cn.zhijian.passport.dto;

import lombok.Data;

@Data
public class CorporateHomeInfoDto {
	final CorporateStructureInfoDto corporateStructureInfo;
	final WalletInfoDto walletInfo;
	final AEODto aeoInfo; 
}
