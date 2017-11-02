package cn.zhijian.pay.commands;

import cn.zhijian.pay.api.Bill;
import lombok.Data;

@Data
public class Rechargecommand {
	final Bill bill;
}
