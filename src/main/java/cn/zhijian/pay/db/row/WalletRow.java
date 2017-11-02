package cn.zhijian.pay.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRow {

	String id;
	double balance;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
