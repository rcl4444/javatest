package cn.zhijian.passport.init;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.session.SessionStoreFactory;
import io.dropwizard.setup.Environment;

public class SessionStoreInitializer implements AxonDropwizardInitializer<BackendConfiguration> {

	protected static Logger logger = LoggerFactory.getLogger(SessionStoreInitializer.class);

	@Override
	public void config(Configurer ax, BackendConfiguration cfg, Environment env, ObjectStore objStore) {
		// session store
		try {
			Class<?> cls = Class.forName(cfg.getSessionStore().getFactoryClass());
			SessionStoreFactory factory = (SessionStoreFactory) cls.newInstance();
			SessionStore sessionStore = factory.create(cfg.getSessionStore(),objStore);
			objStore.put(sessionStore);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
	}

}
