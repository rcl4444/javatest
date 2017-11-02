package cn.zhijian.passport.resourceauth;

import java.util.Date;

import cn.zhijian.passport.api.AuthorizeInfo;
import cn.zhijian.passport.api.Person;
import io.dropwizard.auth.Authorizer;

public class OAuth2Authorizer implements Authorizer<AuthorizeInfo> {

	@Override
	public boolean authorize(AuthorizeInfo principal, String role) {
		
		if(principal.getExpirestime().getTime() < (new Date()).getTime()){
			return false;
		}
		return true;
	}
}
