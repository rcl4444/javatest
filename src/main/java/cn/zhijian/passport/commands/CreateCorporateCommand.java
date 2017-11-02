package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Corporate;
import lombok.Data;

/**
 * Response: long (corporate id created)
 * 
 * @author kmtong
 *
 */
@Data
public class CreateCorporateCommand {
	
	final Long applyPersonId;
	final String applyUserName;
	final Corporate data;
}
