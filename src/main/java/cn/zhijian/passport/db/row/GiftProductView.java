package cn.zhijian.passport.db.row;

import cn.zhijian.passport.statustype.BusinessType;
import lombok.Data;

@Data
public class GiftProductView {
	
	BusinessType type;
	Long productid;
	String productname;
	Integer personnum;
	Integer duration;
}