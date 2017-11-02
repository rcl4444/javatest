package cn.zhijian.passport.admin.dto;

import java.util.Date;

import lombok.Data;

@Data
public class GiftPromotionListDto {

	Long id;
    String promotionname;//活动名称
    String title;//优惠标题
    String period;//活动时间段
    String way;//优惠方式
    String state;//活动状态
    Date createdAt;//创建时间
}