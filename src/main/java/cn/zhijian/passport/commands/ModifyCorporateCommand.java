package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Corporate;
import lombok.Data;

@Data
public class ModifyCorporateCommand {

	final Long applyPersonId;
	final String applyUserName;
	final Corporate data;
}
