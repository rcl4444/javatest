package cn.zhijian.pay.api;

import java.util.List;

import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.pay.api.Pay.DateType;
import lombok.Data;

@Data
public class PayDetails {
	int useDay;
	DateType DateType;
	Integer useNum;
}
