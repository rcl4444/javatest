package cn.zhijian.passport.admin.dto;

import lombok.Data;

@Data
public class FinanceAccountListDto {
	final long id;
	final String financeName;
	final double balance;
}
