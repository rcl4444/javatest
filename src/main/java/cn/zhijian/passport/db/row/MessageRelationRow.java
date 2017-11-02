package cn.zhijian.passport.db.row;

import cn.zhijian.passport.statustype.MessageSourceType;
import lombok.Data;

@Data
public class MessageRelationRow {

	long id;
	long messageid;
	long sourceid;
	MessageSourceType sourcetype;
}