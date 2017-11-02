package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import lombok.Data;

@Data
public class PromotionChangeStateCommand {

	final Long salepromotionid;
	final SalesPromotionStatus state;
	final Long personid;
	final String personname;
}