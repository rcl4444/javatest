package cn.zhijian.passport.admin.commands;

import lombok.Data;

@Data
public class AdminCreateResourceCommand {

	final String name;
	final String contentType;
	final byte[] content;
	final Long uploadpersonid;
	final String uploadpersonname;
}
