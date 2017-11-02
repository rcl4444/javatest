package cn.zhijian.pay.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillOrderRow {
	String body;
	double money;
	Date updatedAt;
}
