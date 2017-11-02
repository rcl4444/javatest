package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.admin.dto.ProductPriceInputDto;
import lombok.Data;

@Data
public class ProductPriceSetCommand {

	final ProductPriceInputDto input;
	final Long personid;
	final String personname;
}