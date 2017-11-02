package cn.zhijian.trade.db.row;

import java.util.Date;

import cn.zhijian.pay.api.Pay.BehaviorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeRow {
	String id;
	String walletId;
	BehaviorType behaviorType;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
