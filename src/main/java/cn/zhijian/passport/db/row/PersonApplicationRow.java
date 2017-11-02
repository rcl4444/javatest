package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonApplicationRow {
	Long id;
	long personid;
	long applicationid;
	int isFree;
	Date useStart;
	Date useEnd;
}
