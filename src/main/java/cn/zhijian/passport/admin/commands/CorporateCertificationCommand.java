package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.statustype.CorporateEnum;
import lombok.Data;

@Data
public class CorporateCertificationCommand {

	final Long corporateId;
	final CorporateEnum status;
	final String reason;
}
