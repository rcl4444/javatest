package cn.zhijian.passport.db.row;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import cn.zhijian.passport.statustype.BaseCodeEnum;
import cn.zhijian.passport.statustype.BusinessType;
import lombok.Data;

@Data
public class ProductRow {

	Long id;
	String productname;
	BusinessType type;
    ProductStatus status;
    String remark;
    Date createdt;
    String avatarresourceid;
    String description;
     
    public static enum ProductStatus implements BaseCodeEnum{
    	PullOffShelves(0,"下架"),
    	PutOnShelves(1,"上架");
    	
    	private Integer code;
    	private String des;
    	ProductStatus(int code,String des){
    		this.code = code;
    		this.des = des;
    	}
    	
    	@Override
    	public Integer getCode() {
    		return code;
    	}

    	@Override
    	public String getDes() {
    		return des;
    	}
    }
    
    @Data
    public static class ProductAppModuleRow{
    	
    	Long id;
    	Long productid;
    	Long applicationid;
    	Long applicationmoduleid;
    }
    
    @Data
    public static class ProductPriceRow{
    	
    	Long id;
    	Long productid;
    	PersonOption personnum;
    	UseTimeOption usetime;
    	Double applicationcost;
    	Double personcost;
    }
    
    public static enum PersonOption implements BaseCodeEnum{
    	Option1(0,(byte)5),Option2(1,(byte)10),Option3(2,(byte)15),Option4(3,(byte)20);
    	
    	private Integer code;
    	private byte personNum;
    	PersonOption(int code,byte personnum){
    		
    		this.code = code;
    		this.personNum = personnum;
    	}
    	
    	@Override
    	public Integer getCode() {

    		return this.code;
    	}
    	
    	@Override
    	public String getDes(){

    		return String.format("%s人", this.personNum);
    	}
    	
    	public int getPersonNum(){
    		return this.personNum;
    	}
    }
    
    public static enum UseTimeOption implements BaseCodeEnum{
    	Option1(0,(byte)3),Option2(1,(byte)6),Option3(2,(byte)12),Option4(3,(byte)18);
    	
    	private Integer code;
    	private byte month;
    	UseTimeOption(int code,byte month){
    		
    		this.code = code;
    		this.month = month;
    	}
    	
    	@Override
    	public Integer getCode() {

    		return this.code;
    	}
    	
    	@Override
    	public String getDes(){
    		return String.format("%s个月", this.month);
    	}
    	
    	public Integer getMonth(){
    		return (int) this.month;
    	}
    	
    	public Date getUseTime(Date inputDate){
    		
    		return DateUtils.addMonths(inputDate, month);
    	}
    }
}