package cn.zhijian.trade.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeDetailsRow {
	Long id;
	String tradeId;
	long productId;
	int useNum;
	String usePeriod;
	double price;//选中的产品费用
	Double applicationcost;
	Double personcost;
}
