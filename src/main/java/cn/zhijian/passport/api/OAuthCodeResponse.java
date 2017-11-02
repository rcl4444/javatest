package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class OAuthCodeResponse {
	
	final String code;
	final String callBackUrl;
	final String state;
	
}
