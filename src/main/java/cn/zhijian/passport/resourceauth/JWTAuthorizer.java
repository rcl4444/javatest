package cn.zhijian.passport.resourceauth;

import io.dropwizard.auth.Authorizer;

public class JWTAuthorizer implements Authorizer<JWTPrincipal> {

	@Override
	public boolean authorize(JWTPrincipal principal, String role) {
		
		return true;
	}
}