package cn.zhijian.passport.admin.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class ProductInputDto {

	Long id;
    @NotEmpty
	@Size(min = 1, max = 25)
	String productname;
    @NotEmpty
    @Size(max=100)
    String description;
    @Size(max = 255)
    String remark;
    @NotNull
    Integer type;
    @NotEmpty
    String activeid;
    @NotNull
    List<Long> modules;
}