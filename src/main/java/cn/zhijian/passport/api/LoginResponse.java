package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class LoginResponse {

	final String sessionId;
	final String locationUrl;
	
}
