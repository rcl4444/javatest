package cn.zhijian.passport.commands;

import lombok.Data;

@Data
public class ValidateSMSCodeCommand {
	final String mobile;
	final String code;
}
