package cn.zhijian.trade.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotRow {
	Long id;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
