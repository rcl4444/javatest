package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.admin.dto.GiftProductInputDto;
import lombok.Data;

@Data
public class GiftPromotionModifyCommand {
	
	final GiftProductInputDto input;
	final Long currAccountId;
	final String currAccountUserName;
}