package cn.zhijian.passport.commands;

import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageType;
import lombok.Data;

@Data
public class GainMessageCommand {
	
	final MessageType messagetype;
	final MessageAccessType accesstype;
	final MessageBelongType belongtype;
	final Boolean isread;
	final Boolean isdelete;
	final Long personid;
	final Long corporateid;
}
