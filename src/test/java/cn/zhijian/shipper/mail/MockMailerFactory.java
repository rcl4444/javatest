package cn.zhijian.shipper.mail;

import org.mockito.Mockito;
import org.simplejavamail.mailer.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.config.SmtpConfig;
import cn.zhijian.passport.mail.MailerFactory;

public class MockMailerFactory implements MailerFactory {

	public static Logger logger = LoggerFactory.getLogger(MockMailerFactory.class);

	public static Mailer MOCK = Mockito.mock(Mailer.class);

	@Override
	public Mailer createMailer(SmtpConfig config) {
		logger.info("============ MOCKING MAILER ==============");
		return MOCK;
	}

}
