package cn.zhijian.shipper.person;

import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.commands.ModifyPersonCommand;
import cn.zhijian.passport.commands.SendEmailBindingCommand;
import cn.zhijian.shipper.IntegrationTestSupport;
import cn.zhijian.shipper.corporate.CorporateCommandTest;

public class PersonCommandTest extends IntegrationTestSupport {
	public static Logger logger = LoggerFactory.getLogger(CorporateCommandTest.class);
	
//	@Test
	public void testModityPerson() throws Exception {
		String sessionId = setupSession(1, 1);
		Person p = new Person((long) 1, "123", "123", null, "18565810588", null, "张三", 1, new Date(), null, "qq", "wx",null,null,null,null);
		ModifyPersonCommand cmd = new ModifyPersonCommand(sessionId,p);
		boolean isSuccess = sendCmd(cmd);
	}
	
//	@Test
	public void testsendbindingemail() throws Exception{
		String sessionId = setupSession(1, 1);
		SendEmailBindingCommand cmd =new SendEmailBindingCommand(sessionId, "372276834@qq.com");
		sendCmd(cmd);
	}
	
}
