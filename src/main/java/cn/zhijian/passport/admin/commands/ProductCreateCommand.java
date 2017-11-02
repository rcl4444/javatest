package cn.zhijian.passport.admin.commands;

import cn.zhijian.passport.admin.dto.ProductInputDto;
import lombok.Data;

@Data
public class ProductCreateCommand {

	final ProductInputDto product;
	final Long personid;
	final String personname;
}