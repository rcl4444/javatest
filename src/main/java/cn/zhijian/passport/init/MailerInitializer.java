package cn.zhijian.passport.init;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.simplejavamail.mailer.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.config.SmtpConfig;
import cn.zhijian.passport.mail.MailerFactory;
import io.dropwizard.setup.Environment;

public class MailerInitializer implements AxonDropwizardInitializer<BackendConfiguration> {

	protected static Logger logger = LoggerFactory.getLogger(MailerInitializer.class);

	@Override
	public void config(Configurer ax, BackendConfiguration cfg, Environment env, ObjectStore objStore) {
		Mailer mailer = createMailer(cfg.getSmtp());
		mailer.setDebug(true);
		objStore.put(mailer);
	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
	}

	private Mailer createMailer(SmtpConfig smtp) {
		if (smtp.getFactoryClass() == null) {
			Mailer mailer = new Mailer(smtp.serverConfig(), smtp.transportStrategy());
			return mailer;
		} else {
			try {
				MailerFactory factory = (MailerFactory) Class.forName(smtp.getFactoryClass()).newInstance();
				return factory.createMailer(smtp);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
