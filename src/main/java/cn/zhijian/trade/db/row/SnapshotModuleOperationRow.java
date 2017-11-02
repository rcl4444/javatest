package cn.zhijian.trade.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotModuleOperationRow {
	Long operationId;
	long applicationId;
	long moduleId;
	long snapshotId;
	String operationName;
}
