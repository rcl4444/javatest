package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class OAuthTokenResponse {
	
	final String access_token;
	final String token_type;
	final Long expires_in;
	final String refresh_token;
}
