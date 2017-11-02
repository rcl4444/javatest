package cn.zhijian.passport;

import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.axonframework.config.Configuration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.admin.db.AdminCorporateMapper;
import cn.zhijian.passport.admin.db.AdminFinanceMapper;
import cn.zhijian.passport.admin.db.AdminPersonMapper;
import cn.zhijian.passport.admin.db.AdminProductMapper;
import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.api.AuthorizeInfo;
import cn.zhijian.passport.bundles.AxonBundle;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.BusinessEventMapper;
import cn.zhijian.passport.db.CertificateDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.CurrSessionMapper;
import cn.zhijian.passport.db.InvitationDAO;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.MessageRelationMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.PingMapper;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.SalesPromotionMapper;
import cn.zhijian.passport.db.SendBindingEmailMapper;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.init.AppDomainInitializer;
import cn.zhijian.passport.init.AxonInfrastructureInitializer;
import cn.zhijian.passport.init.CorporateEnumsInitialzer;
import cn.zhijian.passport.init.MailerInitializer;
import cn.zhijian.passport.init.MyBatisInitializer;
import cn.zhijian.passport.init.RepositoryInitializer;
import cn.zhijian.passport.init.SessionStoreInitializer;
import cn.zhijian.passport.init.WxInitializer;
import cn.zhijian.passport.resourceauth.JWTAuthenticator;
import cn.zhijian.passport.resourceauth.JWTAuthorizer;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import cn.zhijian.passport.resourceauth.OAuth2Authenticator;
import cn.zhijian.passport.resourceauth.OAuth2Authorizer;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.pay.db.PayMapper;
import cn.zhijian.trade.db.TradeMapper;
import cn.zhijian.trade.db.VoucherMapper;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class BackendApplication extends Application<BackendConfiguration> {

	protected static Logger logger = LoggerFactory.getLogger(BackendApplication.class);

	AxonBundle<BackendConfiguration> axon;
	MyBatisInitializer mybatis;

	public static void main(final String[] args) throws Exception {
		new BackendApplication().run(args);
	}

	@Override
	public String getName() {
		return "ppbe";
	}

	public Configuration getAxonConfiguration() {
		return axon.getConfiguration();
	}

	public MyBatisInitializer getMyBatisInitializer() {
		return mybatis;
	}

	public <V> V getObject(Class<V> cls) {
		return axon.getObject(cls);
	}

	public SessionStore getSessionStore() {
		return getObject(SessionStore.class);
	}

	@Override
	public void initialize(final Bootstrap<BackendConfiguration> bootstrap) {
		bootstrap.addBundle(new MultiPartBundle());
		bootstrap.addBundle(new MigrationsBundle<BackendConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(BackendConfiguration configuration) {
				return configuration.getDatabase();
			}
		});
        bootstrap.addBundle(new SwaggerBundle<BackendConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(BackendConfiguration configuration) {
                return configuration.getSwagger();
            }
        });
		if (mybatis == null) {
			mybatis = new MyBatisInitializer( //
					PingMapper.class, // more follows
					InvitationDAO.class, //
					CorporateMapper.class, //
					CorporateStaffMapper.class, //
					PersonDAO.class, //
					TeamDAO.class, //
					ResourceDAO.class, //
					ApplicationDao.class,
					CertificateDao.class,
					SendBindingEmailMapper.class,
					CorporateRoleMapper.class,
					ApplicationModuleDao.class,
					AdminPersonMapper.class,
					MessageMapper.class,
					PayMapper.class,
					AdminCorporateMapper.class,
					CurrSessionMapper.class,
					MessageRelationMapper.class,
					AdminApplicationMapper.class,
					TradeMapper.class,
					AdminProductMapper.class,
					AdminResourceDAO.class,
					ProductMapper.class,
					VoucherMapper.class,
					SalesPromotionMapper.class,
					BusinessEventMapper.class,
					AdminFinanceMapper.class			
				);
		}
		if (axon == null) {
			axon = new AxonBundle<BackendConfiguration>(//
					mybatis, new SessionStoreInitializer(), //
					new AxonInfrastructureInitializer(), //
					new MailerInitializer(),
					new CorporateEnumsInitialzer(), //
					new RepositoryInitializer(), //
					new AppDomainInitializer(), //
					new WxInitializer());
		}
		bootstrap.addBundle(mybatis);
		bootstrap.addBundle(axon);

	}

	@Override
	public void run(final BackendConfiguration configuration, final Environment environment) throws UnsupportedEncodingException {
		setupCORS(environment);

		//忽略JSON字符串包含多余不匹配属性反序列化
//		environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		environment.getObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setAllowedClockSkewInSeconds(120)
                .setRequireExpirationTime()
                .setRequireSubject()
                .setVerificationKey(new HmacKey(configuration.getSite().getJwtTokenSecret()))
                .setRelaxVerificationKeyValidation()
                .build();
        
        JwtAuthFilter jwtFilter = new JwtAuthFilter.Builder<JWTPrincipal>()
        		.setJwtConsumer(consumer)
        		.setCookieName(configuration.getSite().getJwtCookieName())
                .setAuthenticator(new JWTAuthenticator())
                .setAuthorizer(new JWTAuthorizer())
                .setPrefix(configuration.getSite().getJwtPrefix())
                .buildAuthFilter();

	    AuthFilter<String, AuthorizeInfo> oauthFilter = new OAuthCredentialAuthFilter.Builder<AuthorizeInfo>()
	            .setAuthenticator(new OAuth2Authenticator(getObject(CertificateDao.class),getObject(CorporateStaffMapper.class)))
	            .setAuthorizer(new OAuth2Authorizer())
	            .setPrefix("Bearer")
	            .buildAuthFilter();
		PolymorphicAuthDynamicFeature feature = new PolymorphicAuthDynamicFeature<>(ImmutableMap.of(
				AuthorizeInfo.class, oauthFilter,
				JWTPrincipal.class,jwtFilter));
		AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
		    ImmutableSet.of(AuthorizeInfo.class , JWTPrincipal.class));
			
		environment.jersey().register(feature);
		environment.jersey().register(binder);
	}

	private void setupCORS(final Environment environment) {
		final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

		// Configure CORS parameters
		cors.setInitParameter("allowedOrigins", "*");
		cors.setInitParameter("allowedHeaders", "*");
		cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
		cors.setInitParameter("allowCredentials", "true");
		
		// Add URL mapping
		cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
	}

}
