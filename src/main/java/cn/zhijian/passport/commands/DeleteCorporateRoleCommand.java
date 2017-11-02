package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class DeleteCorporateRoleCommand {
	
	final long roleid;
	final long corpId;
}
