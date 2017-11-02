package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamRow {

	Long id;
	long corporateId;
	String name;
	String description;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;

}
