package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class RefreshTokenCommand {
	
	final String refreshtoken;
	final Long personID;
}