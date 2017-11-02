package cn.zhijian.passport.commands;

import java.util.List;

import lombok.Data;

@Data
public class ModityMessageCommand {

	final List<Long> messageid;
	final Boolean isread;
	final Boolean isdelete;
}