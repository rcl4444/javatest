package cn.zhijian.trade.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsRow {
	Long id;
	long orderId;
	long productId;
	int useNum;
	String usePeriod;
	double price;
	Double applicationcost;
	Double personcost;
}
