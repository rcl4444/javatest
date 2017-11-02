package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class OAuthCodeRequest {
	
	final String client_id;
	final String redirect_uri;
	final String response_type;
	final String state;
	
}
