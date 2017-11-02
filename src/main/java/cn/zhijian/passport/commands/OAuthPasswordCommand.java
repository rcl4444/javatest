package cn.zhijian.passport.commands;

import javax.ws.rs.FormParam;

import lombok.Data;

@Data
public class OAuthPasswordCommand {
	
	final String clientid;
	final String clientsecret;
	final String username;
	final String password;
}
