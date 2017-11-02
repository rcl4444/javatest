package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceRow {

	String id;
	String name;
	String contentType;
	byte[] content;
	long ownerId;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
