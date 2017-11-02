package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageType;
import lombok.Data;

@Data
public class MessageRow {

	protected Long id;
	protected String content;
	protected MessageBelongType belongtype;
	protected MessageType messagetype;
	protected MessageOpertionType opertiontype;
	protected Long personid;
	protected Long corporateid;
	protected MessageAccessType accesstype;
	protected Boolean isread;
	protected Boolean isdelete;
	protected Date createdAt;
}