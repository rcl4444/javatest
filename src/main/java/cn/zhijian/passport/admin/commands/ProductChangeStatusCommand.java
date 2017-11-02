package cn.zhijian.passport.admin.commands;

import lombok.Data;

@Data
public class ProductChangeStatusCommand {

	final Long productid;
	final Integer status;
	final Long personid;
	final String personname;
}