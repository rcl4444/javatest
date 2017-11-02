package cn.zhijian.passport.admin.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class GiftProductInputDto {
	
	Long id;
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
    List<Long> productids;
}