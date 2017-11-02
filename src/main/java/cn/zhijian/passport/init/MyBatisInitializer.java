package cn.zhijian.passport.init;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loginbox.dropwizard.mybatis.MybatisBundle;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.db.row.BusinessEventRow.EventStatus;
import cn.zhijian.passport.db.row.ProductRow.PersonOption;
import cn.zhijian.passport.db.row.ProductRow.ProductStatus;
import cn.zhijian.passport.db.row.ProductRow.UseTimeOption;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftType;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionType;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumTypeHandler;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageSourceType;
import cn.zhijian.passport.statustype.MessageType;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import jersey.repackaged.com.google.common.collect.Lists;

public class MyBatisInitializer extends MybatisBundle<BackendConfiguration>
		implements AxonDropwizardInitializer<BackendConfiguration> {

	protected static Logger logger = LoggerFactory.getLogger(MyBatisInitializer.class);

	protected SqlSessionManager manager;
	protected List<Class<?>> mappers;

	public MyBatisInitializer(Class<?> mapper, Class<?>... mappers) {
		super(mapper, mappers);
		this.mappers = Lists.newArrayList(mapper);
		this.mappers.addAll(Lists.newArrayList(mappers));
	}

	@Override
	public DataSourceFactory getDataSourceFactory(BackendConfiguration configuration) {
		return configuration.getDatabase();
	}

	@Override
	public void config(Configurer axConfig, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {

		// initialize manager first
		SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
		String databaseId = determineDatabaseId(sqlSessionFactory.getConfiguration());
		logger.info("Database ID: {}", databaseId);
		sqlSessionFactory.getConfiguration().setDatabaseId(databaseId);

		// 取得类型转换注册器
		TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
		typeHandlerRegistry.register(CorporateEnum.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(MessageAccessType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(MessageBelongType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(MessageOpertionType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(MessageType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(MessageSourceType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(BusinessType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(ProductStatus.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(PersonOption.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(UseTimeOption.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(GiftType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(SalesPromotionType.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(SalesPromotionStatus.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(SalesPromotionStatus.class, CodeEnumTypeHandler.class);
		typeHandlerRegistry.register(EventStatus.class, CodeEnumTypeHandler.class);
		manager = SqlSessionManager.newInstance(sqlSessionFactory);
		manager.getConfiguration().setMapUnderscoreToCamelCase(true);
		objStore.put(manager);

		// register mapper to object store
		mappers.forEach(m -> objStore.put(getMapper(m)));
	}

	@Override
	public void started(Configuration ax, BackendConfiguration dwConfig, Environment env, ObjectStore objStore) {
	}

	/**
	 * Expose mapper to outside world.
	 * 
	 * @param mapperClass
	 * @return
	 */
	public <T> T getMapper(Class<T> mapperClass) {
		if (manager != null) {
			return manager.getMapper(mapperClass);
		}
		throw new RuntimeException("SqlSessionManager not initialized");
	}

	private String determineDatabaseId(org.apache.ibatis.session.Configuration configuration) {
		VendorDatabaseIdProvider p = new VendorDatabaseIdProvider();
		Properties prop = new Properties();
		prop.setProperty("Microsoft SQL Server", "mssql");
		prop.setProperty("H2", "h2");
		prop.setProperty("MySQL", "mysql");
		p.setProperties(prop);
		return p.getDatabaseId(configuration.getEnvironment().getDataSource());
	}

}
