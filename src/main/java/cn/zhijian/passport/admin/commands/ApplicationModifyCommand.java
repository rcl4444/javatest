package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.admin.api.ApplicationEditInfo;
import lombok.Data;

@Data
public class ApplicationModifyCommand {

	final ApplicationEditInfo modifyInfo;
}