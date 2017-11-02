package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorporateRoleModuleRow {

	Long id;
	Long roleId;
	Long moduleId;
	Long corporateId;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
