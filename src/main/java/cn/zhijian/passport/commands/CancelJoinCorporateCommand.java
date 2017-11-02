package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class CancelJoinCorporateCommand {

	final long personid;
	final String personusername;
}