package cn.zhijian.shipper;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.axonframework.config.Configuration;
import org.axonframework.messaging.GenericMessage;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import cn.zhijian.passport.BackendApplication;
import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.session.SessionStore;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public abstract class IntegrationTestSupport {

	private static final String TMP_FILE = createTempFile();
	private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

	@ClassRule
	public static final DropwizardAppRule<BackendConfiguration> RULE = new DropwizardAppRule<>(BackendApplication.class,
			CONFIG_PATH, //
			ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE) //
	);

	@BeforeClass
	public static void migrateDb() throws Exception {
		RULE.getApplication().run("db", "migrate", "-i", "test", CONFIG_PATH);
	}

	private static String createTempFile() {
		try {
			return File.createTempFile("test-example", null).getAbsolutePath();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void testAppStartup() throws Exception {
		BackendApplication app = (BackendApplication) (RULE.getApplication());
		assertNotNull(app);
		assertNotNull(app.getAxonConfiguration());
		assertNotNull(app.getAxonConfiguration().commandGateway());
	}

	protected Configuration getAxonConfig() {
		BackendApplication app = (BackendApplication) (RULE.getApplication());
		if (app != null && app.getAxonConfiguration() != null) {
			return app.getAxonConfiguration();
		}
		return null;
	}

	protected <T> T getMapper(Class<T> mapperClass) {
		BackendApplication app = (BackendApplication) (RULE.getApplication());
		if (app != null && app.getMyBatisInitializer() != null) {
			return app.getMyBatisInitializer().getMapper(mapperClass);
		}
		return null;
	}

	protected <T> T getObject(Class<T> objCls) {
		BackendApplication app = (BackendApplication) (RULE.getApplication());
		if (app != null) {
			return app.getObject(objCls);
		}
		return null;
	}

	protected <T> T sendCmd(Object cmd) {
		return getAxonConfig().commandGateway().sendAndWait(cmd);
	}

	protected <T> T load(Class<T> aggregateClass, String id) {
		try {
			return DefaultUnitOfWork.startAndGet(new GenericMessage<String>(id)).executeWithResult(() -> {

				return getAxonConfig().repository(aggregateClass).load(id).invoke(c -> c);
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("load aggregate by id: " + id + " error!", e);
		}
	}

	protected SessionStore getSessionStore() {
		BackendApplication app = (BackendApplication) (RULE.getApplication());
		if (app != null && app.getMyBatisInitializer() != null) {
			return app.getSessionStore();
		}
		return null;
	}

	protected String uuid() {
		return UUID.randomUUID().toString();
	}

	protected String setupSession(long pid, long cid) {
		String sessionId = uuid();
		getSessionStore().put(sessionId, SessionHelper.makeContext(sessionId, pid, cid));
		return sessionId;
	}
}