package cn.zhijian.trade.db.row;

import lombok.Data;

@Data
public class VoucherApplicationView {

	Long voucherid;
	Long snapshotid;
	Long applicationid;
	String applicationname;
}