package cn.zhijian.passport.db.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeqRow {

	long id;
	String name;
	long corporateId;
	String date_pattern;
	long last_value;
	long increment_value;
}
