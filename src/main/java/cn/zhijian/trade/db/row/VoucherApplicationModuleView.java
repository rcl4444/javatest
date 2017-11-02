package cn.zhijian.trade.db.row;

import lombok.Data;

@Data
public class VoucherApplicationModuleView {

	Long voucherid;
	Long snapshotid;
	Long applicationid;
	Long applicationmoduleid;
	String applicationmodulename;
}