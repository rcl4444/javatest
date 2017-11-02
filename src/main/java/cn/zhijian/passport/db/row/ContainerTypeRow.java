package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContainerTypeRow {
	protected long id;
	protected long corporateId;
	protected String name;
	protected Double length;
	protected Double width;
	protected Double height;
	protected String createdBy;
	protected Date createdAt;
	protected String updatedBy;
	protected Date updatedAt;
}
