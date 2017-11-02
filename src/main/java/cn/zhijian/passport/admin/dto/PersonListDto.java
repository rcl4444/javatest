package cn.zhijian.passport.admin.dto;

import java.util.Date;

import lombok.Data;

@Data
public class PersonListDto {
	final long id;
	final String username;
	final String realname;
	final String email;
	final String mobile;
	final Date createdAt;
	final String walletId;
}
