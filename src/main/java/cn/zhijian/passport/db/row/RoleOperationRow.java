package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class RoleOperationRow {

	Long id;
	Long roleid;
	Long voucherid;
	Long applicationid;
	Long moduleid;
	Long operationid;
	Date createdate;
}