package cn.zhijian.pay.commands;

import cn.zhijian.pay.api.Bill;
import lombok.Data;

@Data
public class Expensescommand {
	final Bill bill;
}
