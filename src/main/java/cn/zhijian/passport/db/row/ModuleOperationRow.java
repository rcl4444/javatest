package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class ModuleOperationRow {

	Long id;
	Long applicationid;
	Long moduleid;
	String operationname;
	Date createdate;
}