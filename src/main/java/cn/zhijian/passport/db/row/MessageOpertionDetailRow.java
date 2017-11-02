package cn.zhijian.passport.db.row;

import lombok.Data;

@Data
public class MessageOpertionDetailRow {

	protected Long id;
	protected Long messageid;
	protected String linktxt;
	protected String linkurl;
	protected String linkclass;
}
