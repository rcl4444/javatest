package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.statustype.BaseCodeEnum;
import cn.zhijian.passport.statustype.BusinessType;
import lombok.Data;

@Data
public class SalesPromotionRow {

	Long id;
	String spname;
	Date begindate;
	Date enddate;
	SalesPromotionType saletype;
	SalesPromotionStatus state;
	Boolean showstate;
	String createdBy;
	Date createdAt;
	BusinessType type;
	
	@Data
	public static class GiftMainRow{
		Long id;
		Long salespromotionid;
		Long eventid;
		String promotiontitle;
		GiftType gifttype;
	}
	
	@Data
	public static class GiftProductRow{
		Long id;
		Long giftmainid;
		Long productid;
		Integer personnum;
		Integer duration;
	}

	@Data
	public static class GiftPromotionView{
		Long id;//活动ID	
		String spname; //活动名称
		String promotiontitle;//优惠标题
		Date begindate;//开始时间
		Date enddate;//结束时间
		String eventname;//事件名
		GiftType gifttype;//赠送方式
		Date createdAt;//创建时间
		SalesPromotionStatus state;//状态
	}
	
	public static enum PromotionStatus implements BaseCodeEnum{
		Default(0,"未发布"),
		UnStart(1,"未开始"),
		Continue(2,"进行中"),
		Over(3,"已结束"),
		Delete(4,"删除");
		
	    private Integer code;
	    private String des;
	    private PromotionStatus( Integer code, String des) { 
	    	this.code = code; 
	    	this.des = des;
	    }
	    
		@Override
		public Integer getCode() {
			return this.code;
		}

		public String getDes() {
			return this.des;
		}
	}
	
	public static enum GiftType implements BaseCodeEnum{
		Product(0,"赠送产品"),
//		Coupon(1,"优惠券")
		;
		
	    private Integer code;
	    private String des;
	    private GiftType( Integer code, String des) { 
	    	this.code = code; 
	    	this.des = des;
	    }
	    
		@Override
		public Integer getCode() {
			return this.code;
		}

		public String getDes() {
			return this.des;
		}
	}
	
	public static enum SalesPromotionType implements BaseCodeEnum{
		Gift(0,"直送");
		
	    private Integer code;
	    private String des;
	    private SalesPromotionType( Integer code, String des) { 
	    	this.code = code; 
	    	this.des = des;
	    }
	    
		@Override
		public Integer getCode() {
			return this.code;
		}

		public String getDes() {
			return this.des;
		}
	}
	
	public static enum SalesPromotionStatus implements BaseCodeEnum{
		Default(0,"未发布"),
		Start(1,"启用");
		
	    private Integer code;
	    private String des;
	    private SalesPromotionStatus( Integer code, String des) { 
	    	this.code = code; 
	    	this.des = des;
	    }
	    
		@Override
		public Integer getCode() {
			return this.code;
		}

		public String getDes() {
			return this.des;
		}
	}
}