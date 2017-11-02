package cn.zhijian.passport.admin.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResourceRow {

	String id;
	String name;
	String contentType;
	byte[] content;
	Long ownerId;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}