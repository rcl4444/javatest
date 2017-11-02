package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class ModifyCorporateAvatarCommand {
	
	final String resourceId;
	final Long corporateId;
}
