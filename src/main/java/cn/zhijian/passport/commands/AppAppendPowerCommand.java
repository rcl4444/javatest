package cn.zhijian.passport.commands;

import java.util.List;

import cn.zhijian.passport.api.AppAppendPowerInfo;
import lombok.Data;

@Data
public class AppAppendPowerCommand {

	final long appid;
	final List<AppAppendPowerInfo.AppModule> modules;
}