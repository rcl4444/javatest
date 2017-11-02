package cn.zhijian.trade.commands;

import cn.zhijian.trade.api.CreateOrder;
import cn.zhijian.trade.api.Order;
import lombok.Data;

@Data
public class CreateOrderCommand {
	final CreateOrder createOrder;
	final String userName;
	final String createdBy;
	final long userId;
}
