package cn.zhijian.passport.domain.commandhandlers;

import org.axonframework.commandhandling.CommandHandler;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.EmailAddress;
import cn.zhijian.passport.commands.SendEmailCommand;

public class EmailCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(EmailCommandHandler.class);

	final Mailer mailer;
	final EmailAddress sender;
	final boolean async;

	public EmailCommandHandler(Mailer mailer, EmailAddress sender) {
		this.mailer = mailer;
		this.sender = sender;
		this.async = false;
	}

	@CommandHandler
	public void sendEmail(SendEmailCommand cmd) {
		EmailBuilder builder = new EmailBuilder();
		builder.from(sender.getName(), sender.getEmail());

		cmd.getTo().forEach(em -> {
			builder.to(em.getName(), em.getEmail());
		});
		cmd.getCc().forEach(em -> {
			builder.cc(em.getName(), em.getEmail());
		});
		builder.subject(cmd.getSubject());
		builder.text(cmd.getText());
		builder.textHTML(cmd.getTextHtml());
		cmd.getAttachments().forEach((name, content) -> {
			builder.addAttachment(name, content.getContent(), content.getContentType());
		});
		cmd.getImages().forEach((name, content) -> {
			builder.embedImage(name, content.getContent(), content.getContentType());
		});

		Email email = builder.build();
		logger.info("Sending mail to: {}", email.getRecipients());
		logger.debug("Email Content: {}", email.toString());
		mailer.sendMail(email, async);
	}

}
