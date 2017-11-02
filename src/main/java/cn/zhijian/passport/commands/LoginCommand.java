package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Login;
import lombok.Data;

/**
 * 
 * @author kmtong
 *
 */
@Data
public class LoginCommand {

	final Login data;
	final String sessionId;
	
}
