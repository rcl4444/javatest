package cn.zhijian.trade.commands;

import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.db.row.TradeDetailsRow;
import lombok.Data;

@Data
public class CreateVoucherCommand {
	final OrderRow order;
	final int useNum;
	final String usePeriod;
	final long productId;
}
