package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Registration;
import lombok.Data;

@Data
public class PasswordResetCommand {
	final Registration data;
}
