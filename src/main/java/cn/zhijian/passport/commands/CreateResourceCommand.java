package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Resource;
import lombok.Data;

@Data
public class CreateResourceCommand {

	final String sessionId;
	final Resource data;

}
