package cn.zhijian.passport.db.row;

import cn.zhijian.passport.statustype.BaseCodeEnum;
import cn.zhijian.passport.statustype.BusinessType;
import lombok.Data;

@Data
public class BusinessEventRow {

	Long id;
	String eventname;
	String sign;
	String description;
	EventStatus state;
	BusinessType businesstype;
	
	@Data
	public static class EventView{
		String sign;
		String eventname;
	}
	
	public static enum EventStatus implements BaseCodeEnum{
		Stop(0,"未启用"),
		Start(1,"启用");
		
	    private Integer code;
	    private String des;
	    private EventStatus( Integer code, String des) { 
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