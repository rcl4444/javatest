package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberRow {

	Long id;
	long corporateId;
	long teamId;
	long staffId;
	String role;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;

}
