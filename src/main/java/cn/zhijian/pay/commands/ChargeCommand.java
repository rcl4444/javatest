package cn.zhijian.pay.commands;

import cn.zhijian.pay.api.Charge;
import lombok.Data;

@Data
public class ChargeCommand {
	final Charge charge;
	final String userName;
	final String CreatedBy;
	final long userId;
}
