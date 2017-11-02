package cn.zhijian.passport.domain.services;

import java.util.Map;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.commands.SendEmailCommand;
import cn.zhijian.passport.commands.builders.SendEmailCommandBuilder;
import cn.zhijian.passport.template.TemplateService;

public class MessagingService {

	final static Logger logger = LoggerFactory.getLogger(MessagingService.class);

	final TemplateService templateService;
	final CommandGateway cmdGw;

	public MessagingService(CommandGateway cmdGw, TemplateService templateService) {
		this.cmdGw = cmdGw;
		this.templateService = templateService;
	}

	public void sendEmail(String templateName, String email, String subject, Map<String, Object> vars) {
		cmdGw.sendAndWait(composeEmail(templateName, email, subject, vars));
	}

	public void sendSystemMessage(String templateName, long personId, String subject, Map<String, Object> vars) {
		// XXX
		logger.warn("==========================================");
		logger.warn("SEND SYSTEM MESSAGE NOT IMPLEMENTED YET!!!");
		logger.warn("==========================================");
	}

	/**
	 * Caller should place two template file under the resource:
	 * <code>%s/txt.ftl</code> and <code>%s/html.ftl</code>
	 * 
	 * Variables available to teamplte:
	 * <ul>
	 * <li>link
	 * </ul>
	 * See <code>src/main/resources/templates/</code> folders.
	 * 
	 * @param templateName
	 *            (e.g. invitation)
	 * @param row
	 * @return
	 */
	private SendEmailCommand composeEmail(String templateName, String recipientEmail, String subject,
			Map<String, Object> vars) {
		String textTemplate = String.format("%s/txt.ftl", templateName);
		String htmlTemplate = String.format("%s/html.ftl", templateName);
		return SendEmailCommandBuilder.builder().addTo(null, recipientEmail) //
				.setSubject("Invitation to Join") //
				.setText(templateService.getContent(textTemplate, vars)) //
				.setTextHtml(templateService.getContent(htmlTemplate, vars)) //
				.build();
	}

}
