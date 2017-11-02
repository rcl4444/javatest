package cn.zhijian.passport.commands;

import lombok.Data;

/**
 * 
 * @author kmtong
 *
 */
@Data
public class RefreshLoginContextCommand {

	final String sessionId;

}
