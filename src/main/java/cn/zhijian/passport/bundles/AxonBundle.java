package cn.zhijian.passport.bundles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class AxonBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T> {

	protected Configurer configurer;
	protected Configuration configuration;
	// shared between init and startup phases
	protected ObjectStore properties;

	protected List<AxonDropwizardInitializer<T>> inits;

	@SafeVarargs
	public AxonBundle(AxonDropwizardInitializer<T>... inits) {
		this.properties = new ObjectStore();
		if (inits != null) {
			this.inits = Arrays.asList(inits);
		} else {
			this.inits = new ArrayList<>();
		}
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		this.configurer = DefaultConfigurer.defaultConfiguration();
	}

	@Override
	public void run(T dwConfig, Environment environment) throws Exception {

		inits.forEach(i -> i.config(this.configurer, dwConfig, environment, properties));
		this.configuration = configurer.buildConfiguration();
		HelpSession.setEventBus(this.configuration.eventBus());
		
		environment.lifecycle().manage(new Managed() {
			@Override
			public void start() throws Exception {
				configuration.start();
				inits.forEach(i -> i.started(configuration, dwConfig, environment, properties));
			}

			@Override
			public void stop() throws Exception {
				configuration.shutdown();
			}
		});
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public <K> K getObject(Class<K> clz) {
		return properties.get(clz);
	}

	@FunctionalInterface
	public static interface AxonInitFunc<T> {
		void config(Configurer axConfig, T dwConfig, Environment environment, ObjectStore objStore);
	}

	@FunctionalInterface
	public static interface AxonStartupCallback<T> {
		void started(Configuration configuration, T dwConfig, Environment environment, ObjectStore objStore);
	}

}
