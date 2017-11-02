package cn.zhijian.passport.admin.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class GiftProductDetailDto {
  	String promotionname;
    String title;
    Date periodstart;
    Date periodend;
    Integer method;
    Long event;
    Boolean issign;
    Integer type;
    Integer peopleno;
    Integer time;
    List<GiftProduct> products;
    
    @Data
    public static class GiftProduct{
    	Long id;
    	String productname;
    }
}