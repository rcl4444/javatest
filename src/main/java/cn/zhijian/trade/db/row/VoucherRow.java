package cn.zhijian.trade.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRow {
	Long id;
	String voucherNo;
	Long orderId;
	long snapshotId;
	Integer useNum;

	String usePeriod;
	Date startTime;
	Date endTime;
	String walletId;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
	long productId;
}
