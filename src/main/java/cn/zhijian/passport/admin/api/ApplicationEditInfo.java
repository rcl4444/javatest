package cn.zhijian.passport.admin.api;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class ApplicationEditInfo {

	Long id;
    @NotEmpty
	String appname;
    @NotEmpty
	String websiteLink;
    @NotEmpty
	String callbackLink;
	String exitLink;
	String dataLink;
	String activeid;
	Integer type;
}