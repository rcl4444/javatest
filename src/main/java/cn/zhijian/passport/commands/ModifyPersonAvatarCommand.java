package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.ResourceID;
import lombok.Data;

@Data
public class ModifyPersonAvatarCommand {

	final String sessionId;
	final ResourceID data;

}
