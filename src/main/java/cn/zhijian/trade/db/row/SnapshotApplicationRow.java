package cn.zhijian.trade.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotApplicationRow {
	long applicationId;
	long snapshotId;
}
