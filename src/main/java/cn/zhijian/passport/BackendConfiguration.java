package cn.zhijian.passport;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import cn.zhijian.passport.config.SiteConfig;
import cn.zhijian.passport.config.SmtpConfig;
import cn.zhijian.passport.session.SessionStoreConfig;
import cn.zhijian.pay.config.WxConfig;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;
import lombok.Setter;

public class BackendConfiguration extends Configuration {

	@Valid
	@NotNull
	@Getter
	@Setter
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	@NotNull
	@Getter
	@Setter
	private SmtpConfig smtp = new SmtpConfig();

	@Valid
	@NotNull
	@Getter
	@Setter
	private SiteConfig site = new SiteConfig();

	@Valid
	@NotNull
	@Getter
	@Setter
	private SessionStoreConfig sessionStore = new SessionStoreConfig();

	@Valid
	@NotNull
	@Getter
	@Setter
	private WxConfig wx = new WxConfig();
	
	@Valid
	@NotNull
	@Getter
	@Setter
	private SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();
}
