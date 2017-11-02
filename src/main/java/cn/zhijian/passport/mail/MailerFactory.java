package cn.zhijian.passport.mail;

import org.simplejavamail.mailer.Mailer;

import cn.zhijian.passport.config.SmtpConfig;

public interface MailerFactory {

	Mailer createMailer(SmtpConfig config);
}
