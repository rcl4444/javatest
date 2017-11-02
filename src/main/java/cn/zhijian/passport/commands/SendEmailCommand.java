package cn.zhijian.passport.commands;

import java.util.List;
import java.util.Map;

import cn.zhijian.passport.api.Content;
import cn.zhijian.passport.api.EmailAddress;
import lombok.Data;

@Data
public class SendEmailCommand {

	final List<EmailAddress> to;
	final List<EmailAddress> cc;
	final String subject;
	final String text;
	final String textHtml;

	final Map<String, Content> attachments;
	final Map<String, Content> images;

}
