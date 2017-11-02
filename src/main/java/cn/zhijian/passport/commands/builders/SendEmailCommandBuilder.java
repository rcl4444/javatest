package cn.zhijian.passport.commands.builders;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.zhijian.passport.api.Content;
import cn.zhijian.passport.api.EmailAddress;
import cn.zhijian.passport.commands.SendEmailCommand;

public class SendEmailCommandBuilder {

	List<EmailAddress> to = new LinkedList<>();
	List<EmailAddress> cc = new LinkedList<>();
	String subject;
	String text;
	String textHtml;

	Map<String, Content> attachments = new HashMap<>();
	Map<String, Content> images = new HashMap<>();

	public SendEmailCommandBuilder addTo(String name, String email) {
		to.add(new EmailAddress(email, name));
		return this;
	}

	public SendEmailCommandBuilder addCc(String name, String email) {
		cc.add(new EmailAddress(email, name));
		return this;
	}

	public SendEmailCommandBuilder setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public SendEmailCommandBuilder setText(String text) {
		this.text = text;
		return this;
	}

	public SendEmailCommandBuilder setTextHtml(String text) {
		this.textHtml = text;
		return this;
	}

	public SendEmailCommandBuilder addImage(String cid, Content data) {
		this.images.put(cid, data);
		return this;
	}

	public SendEmailCommandBuilder addAttachment(String cid, Content data) {
		this.attachments.put(cid, data);
		return this;
	}

	public SendEmailCommand build() {
		return new SendEmailCommand(to, cc, subject, text, textHtml, attachments, images);
	}

	public static SendEmailCommandBuilder builder() {
		return new SendEmailCommandBuilder();
	}
}
