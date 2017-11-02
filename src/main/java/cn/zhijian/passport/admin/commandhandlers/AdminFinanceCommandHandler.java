package cn.zhijian.passport.admin.commandhandlers;

import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;

import cn.zhijian.passport.admin.api.FinanceAccount;
import cn.zhijian.passport.admin.commands.CreateFinanceAccountCommand;
import cn.zhijian.passport.admin.reps.AdminFinanceRepository;
import cn.zhijian.passport.admin.row.FinanceAccountRow;

public class AdminFinanceCommandHandler {
	final CommandGateway cmdGw;
	final AdminFinanceRepository adminFinanceRepository;

	public AdminFinanceCommandHandler(CommandGateway cmdGw, AdminFinanceRepository adminFinanceRepository) {
		this.cmdGw = cmdGw;
		this.adminFinanceRepository = adminFinanceRepository;
	}

	@CommandHandler
	public Pair<Boolean, String> createFinanceAccount(CreateFinanceAccountCommand cmd) {
		FinanceAccountRow row = convert(cmd.getFinanceAccount());
		row.setCreatedAt(new Date());
		row.setCreatedBy(cmd.getCreatedBy());

		if (adminFinanceRepository.insert(row) > 0) {
			return Pair.of(true, "成功");
		}
		return Pair.of(false, "创建失败");
	}

	private FinanceAccountRow convert(FinanceAccount financeAccount) {
		return new FinanceAccountRow(null, financeAccount.getReceiptAccountName(), financeAccount.getReceiptAccountNo(),
				financeAccount.getFlag(), null, null, null, null);
	}
}
