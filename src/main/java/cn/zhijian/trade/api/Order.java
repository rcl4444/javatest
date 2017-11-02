package cn.zhijian.trade.api;

import java.util.Date;

import cn.zhijian.pay.api.Pay.BehaviorType;
import lombok.Data;

@Data
public class Order {
	final BehaviorType behaviorType;
	final Date sTime;
	final Date eTime;
	final Integer pageNo;
	final Integer pageSize;
	final String outTradeNo;
}
