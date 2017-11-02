package cn.zhijian.trade.commands;

import lombok.Data;

@Data
public class DeleteOrderCommand {
	final long orderId;
}
