package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.CorporateRole;
import lombok.Data;

@Data
public class CreateCorporateRoleCommand {

	final Long corporaterId;
	final String handleusername;
	final CorporateRole role;
}
