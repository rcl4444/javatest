package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class OAuthTokenRequire {
	
	final String client_id;
	final String client_secret;
	final String code;
	final String grant_type;
	final String redirect_uri;
}
