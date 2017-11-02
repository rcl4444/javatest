package cn.zhijian.passport.init;

import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import io.dropwizard.setup.Environment;

public class AxonInfrastructureInitializer implements AxonDropwizardInitializer<BackendConfiguration> {

	final static Logger logger = LoggerFactory.getLogger(AxonInfrastructureInitializer.class);

	@Override
	public void config(Configurer ax, BackendConfiguration cfg, Environment environment, ObjectStore os) {

		// XXX reuse mybatis connection
		SqlSessionManager mybatis = os.get(SqlSessionManager.class);
		// event store
		ax.configureEmbeddedEventStore((c) -> {
			mybatis.startManagedSession();
			JdbcEventStorageEngine engine = new JdbcEventStorageEngine(() -> mybatis.openSession().getConnection(),
					NoTransactionManager.INSTANCE);
			// table creation handled by liquibase
			return engine;
		});

	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore os) {
	}

}
