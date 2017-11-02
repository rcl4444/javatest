package cn.zhijian.trade.commands;

import cn.zhijian.trade.api.Trade;
import lombok.Data;

@Data
public class CreateTradeCommand {
	final Trade trade;
	final String walletId;
	final String createdBy;
}
