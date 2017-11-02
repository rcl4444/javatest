package cn.zhijian.passport.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ExpensesInfoDto {
	final Date date;
	final String message;
	final double money;
}
