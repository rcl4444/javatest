package cn.zhijian.trade.dto;

import java.util.List;

import lombok.Data;

@Data
public class VoucheDetailsDto {
	final String appName;
	final List<VoucheModuleDto> modules;
}
