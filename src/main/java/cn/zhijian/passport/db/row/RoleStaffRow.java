package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleStaffRow {
	Long id;
	long roleId;
	long staffId;
	long corporateId;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
