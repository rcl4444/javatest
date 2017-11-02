package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.OAuthTokenRequire;
import lombok.Data;

@Data
public class AuthorizationCodeCommand {
	
	final OAuthTokenRequire oAuthTokenRequire;
}
