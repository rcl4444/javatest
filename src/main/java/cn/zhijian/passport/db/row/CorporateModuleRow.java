package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class CorporateModuleRow {

	Long id;
	String moduleName;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
