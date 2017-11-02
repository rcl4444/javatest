package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffPersonRow {

	// staff
	Long id;
	Long personId;
	long corporateId;
	String role;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
	boolean blocked;

	// person
	String username;
	String name;
	String email;
	String mobile;
	String status; // invited / accepted
	String avatarResourceId; // resource ID

}
