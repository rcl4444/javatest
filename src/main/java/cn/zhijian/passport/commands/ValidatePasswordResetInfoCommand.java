package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Registration;
import lombok.Data;

@Data
public class ValidatePasswordResetInfoCommand {
	final Registration data;
}
