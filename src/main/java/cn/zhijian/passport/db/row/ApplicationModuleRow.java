package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class ApplicationModuleRow {

	Long id;
	Long applicationid;
	String modulename;
	Date createdate;
}
