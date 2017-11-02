package cn.zhijian.pay.commands;

import cn.zhijian.pay.api.Pay;
import lombok.Data;

@Data
public class PaymentedCommand {
	final Pay pay;
	final String username;
}
