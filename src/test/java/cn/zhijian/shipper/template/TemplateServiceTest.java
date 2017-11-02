package cn.zhijian.shipper.template;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

import cn.zhijian.passport.template.TemplateService;

public class TemplateServiceTest {

	@Test
	public void testGetContent() throws Exception {
		TemplateService temp = new TemplateService();
		Map<String, Object> map = Maps.newHashMap();
		map.put("link", "http://somelink");
		String content = temp.getContent("test/txt.ftl", map);
		System.out.println(content);
		assertEquals("Visit http://somelink for Account Activation", content);
	}

}
