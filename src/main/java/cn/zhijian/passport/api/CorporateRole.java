package cn.zhijian.passport.api;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class CorporateRole {
	final Long id;
	@NotEmpty
    final String rolename;
    final String description;
    final List<VoucherOperation> certificates;
    final List<Long> moduleIds;
    
    @Data
    public static class VoucherOperation{
    	@NotNull
    	final Long certificateid;
    	@NotNull
    	final List<Long> operationids;
    }
}