package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.admin.api.FinanceAccount;
import lombok.Data;

@Data
public class CreateFinanceAccountCommand {
	final FinanceAccount  financeAccount;
	final String createdBy;
}
