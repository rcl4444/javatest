package cn.zhijian.passport.resourceauth;

import java.security.Principal;

import lombok.Data;

@Data
public class JWTPrincipal implements Principal {

	final Long userId;
	final String name;
	final String personName;
}
