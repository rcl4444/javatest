package cn.zhijian.trade.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotApplicationModuleRow {

	long moduleId;
	long applicationId;
	long snapshotId;
	String moduleName;
}
