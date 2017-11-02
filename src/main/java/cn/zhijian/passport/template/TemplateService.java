package cn.zhijian.passport.template;

import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class TemplateService {

	final Configuration cfg;

	public TemplateService() {
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "/templates/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}

	public String getContent(String templateName, Object model) {
		try {
			Template temp = cfg.getTemplate(templateName);
			StringWriter sw = new StringWriter();
			temp.process(model, sw);
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
