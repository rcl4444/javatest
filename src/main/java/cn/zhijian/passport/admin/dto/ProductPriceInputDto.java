package cn.zhijian.passport.admin.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class ProductPriceInputDto {

	Integer productid;
	@Valid
	@NotEmpty
	List<ProductPrice> prices;
	@Data
	public static class ProductPrice{
		@NotNull
		Integer time;
		@NotNull
		Integer timeindex;
		@NotNull
		Integer people;
		@NotNull
		Integer peopleindex;
		@DecimalMin(value = "0")
		Double timeprice;
		@DecimalMin(value = "0")
		Double peopleprice;
	}
}