package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.SwitchCorporate;
import lombok.Data;

/**
 * 
 * @author kmtong
 *
 */
@Data
public class SwitchCorporateCommand {

	final String sessionId;
	final SwitchCorporate switchTo;

}
