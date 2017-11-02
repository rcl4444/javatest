package cn.zhijian.trade.db.row;

import java.util.Date;

import cn.zhijian.pay.api.Pay.BehaviorType;
import cn.zhijian.pay.api.Pay.DateType;
import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.api.Pay.PayType;
import cn.zhijian.pay.api.Pay.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRow {

	 Long id;
	 String outTradeNo;
	 String body;
	 PayType payType;
	 double totalFee;
	 String walletId;
	 int isPayed;
	 OrderType orderType;
	 DateType DateType;
	 String createdBy;
	 Date createdAt;
	 String updatedBy;
	 Date updatedAt;
	 
	 ServiceType serviceType;
	 BehaviorType behaviorType;
	 int isDelete;
}
