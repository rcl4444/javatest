package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Corporate;
import lombok.Data;

@Data
public class CorporateCertificationCommand {
	final Corporate corporate; 
	final String relname;
}
