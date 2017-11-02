package cn.zhijian.passport.dto;

import java.util.List;

import lombok.Data;

@Data
public class WalletInfoDto {
	final double balance;
	final List<ExpensesInfoDto> expensesInfos;
}
