package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class AdminPersonRow {

	protected Long id;
	protected String username;
	protected String password;
	protected String name;
	protected String email;
	protected String mobile;
	protected String createdBy;
	protected Date createdAt;
	protected String updatedBy;
	protected Date updatedAt;
}
