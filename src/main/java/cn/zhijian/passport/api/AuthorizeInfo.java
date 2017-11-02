package cn.zhijian.passport.api;

import java.security.Principal;
import java.util.Date;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class AuthorizeInfo implements Principal{

	final Long personid;
	final String name;
	final String realname;
	@Nullable
	final Long corporateid;
	final Date expirestime;
	final Long applicationid;
}