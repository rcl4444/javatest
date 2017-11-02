package cn.zhijian.passport.admin.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ProductChangeStateDto {

	@NotNull
	Long productid;
	@NotNull
    Integer state;
}