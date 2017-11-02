package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorporateRoleRow {

	Long id;
	Long corporateid;
	String rolename;
	String description;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
