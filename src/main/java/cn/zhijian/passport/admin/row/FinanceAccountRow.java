package cn.zhijian.passport.admin.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinanceAccountRow {
	Long id;
	String financeAccountName;
	String financeAccountNo;
	String flag;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
}
