package cn.zhijian.pay.api;

import java.util.Date;

import cn.zhijian.pay.api.Bill.BillType;
import lombok.Data;

@Data
public class WalletRequest {

	final String walletId;
	final Date sTime;
	final Date eTime;
	final Integer pageNo;
	final Integer pageSize;
}
