package cn.zhijian.shipper.corporate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.api.Registration;
import cn.zhijian.passport.commands.CallerInfo;
import cn.zhijian.passport.commands.CreateCorporateCommand;
import cn.zhijian.passport.commands.CreateCorporateRoleCommand;
import cn.zhijian.passport.commands.JoinCorporateStaffCommand;
import cn.zhijian.passport.commands.ModifyCorporateCommand;
import cn.zhijian.passport.commands.ModityCorporateRoleCommand;
import cn.zhijian.passport.commands.RegistrationCommand;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.shipper.IntegrationTestSupport;

public class CorporateCommandTest extends IntegrationTestSupport {
	public static Logger logger = LoggerFactory.getLogger(CorporateCommandTest.class);
	public CorporateRoleMapper corporateRoleDao =  getObject(CorporateRoleMapper.class);
	@Test
	public void testCreateCorporate() throws Exception {
//		String sessionId = setupSession(1, 1);
//		Corporate c = new Corporate((long)1, 
//				"测试大壮", 
//				"www.baidu.com", 
//				"断罪小学", 
//				"/1.png", 
//				"123", 
//				"竞技及地区", 
//				"赵日天", 
//				"网打败", 
//				1, 
//				"火星人", 
//				"愚蠢的地球听", 
//				"块块来", 
//				"大一卡",
//				CorporateEnum.Audit_Apply,
//				null,
//				null,null,null,null,null,null,null,null);
//		CallerInfo ci = new CallerInfo(1, "123", null);
//
//		CorporateStaffMapper corporateStaffMapper = getObject(CorporateStaffMapper.class);
//		
//		CreateCorporateCommand cmd = new CreateCorporateCommand(1L,"123",c);
//		Pair< String, Long> result = sendCmd(cmd);
//		logger.info(String.valueOf(result.getLeft()));
//		assertTrue(result.getRight() > 0);
//		CorporateMapper corporateMapper = getObject(CorporateMapper.class);
//		CorporateRow crow = corporateMapper.load(result.getRight());
//		assertEquals(c.getName(),crow.getName());
//		assertEquals(c.getWebsite(),crow.getWebsite());
//		assertEquals(c.getAddress(),crow.getAddress());
//		//assertEquals(c.getLogo(),crow.getLogoResourceId());
//		assertEquals(c.getHsCode(),crow.getHsCode());
//		assertEquals(c.getCreditCode(),crow.getCreditCode());
//		assertEquals(c.getCustomArea(),crow.getCustomArea());
//		assertEquals(c.getContactsName(),crow.getContactsName());
//		assertEquals(c.getContactsSex(),crow.getContactsSex());
//		assertEquals(c.getContactsDuties(),crow.getContactsDuties());
//		assertEquals(c.getContactsMobile(),crow.getContactsMobile());
//		assertEquals(c.getContactsTel(),crow.getContactsTel());
//		assertEquals(c.getCorporateMark(),crow.getCorporateMark());
		//assertEquals(c.getIsPending(),crow.getIsPending());
//		List<CorporateRoleRow> roles  =  corporateRoleDao.findByCorporateRoleId(result.getRight());
//		assertNotNull(roles.stream().filter(r->"管理员".equals(r.getRolename())));
//		assertNotNull(roles.stream().filter(r->"总经理".equals(r.getRolename())));
//		assertNotNull(roles.stream().filter(r->"报关员".equals(r.getRolename())));
//		assertNotNull(roles.stream().filter(r->"财务".equals(r.getRolename())));
	}

	@Test
	public void testModityCorporate() throws Exception {
		String sessionId = setupSession(1, 1);
		Corporate c = new Corporate((long)1, "123", "123", "123", null, "123", "123", "123", "123", 1, "123", "123", "123", "123",null,null,null,null,null,null,null,null,null,null,null);
		ModifyCorporateCommand cmd = new ModifyCorporateCommand(1L,"123",c);
		Pair< Boolean, String> a = sendCmd(cmd);
		logger.info(String.valueOf(a.getRight()));
		
	}
	
	@Test 
	public void createRole() throws Exception{
//		setupSession(1, 1);
//		CreateCorporateRoleCommand cmd = new CreateCorporateRoleCommand(1, "报关员", "");
//		boolean a = sendCmd(cmd);
//		assertTrue(a);
	}
	
	@Test 
	public void modityRepeatRole() throws Exception{
//		setupSession(1, 1);
//		CreateCorporateRoleCommand cmd = new CreateCorporateRoleCommand(1, "报关员", "");
//		CreateCorporateRoleCommand cmd1 = new CreateCorporateRoleCommand(1, "报关员2", "");
//		sendCmd(cmd);
//		sendCmd(cmd1);
//		ModityCorporateRoleCommand cmd2 = new ModityCorporateRoleCommand(1,1, "报关员3", "");
//		boolean b = sendCmd(cmd2);
//		assertTrue(b);
//		List<CorporateRoleRow> row = corporateRoleDao.findByCorporateRoleId((long)1);
//		assertNotNull(row.stream().filter(r ->"报关员2".equals(r.getRolename())));
	}
	
	@Test
	public void joinCorporate() throws Exception{
//		String session = setupSession(2, 346);
//		Corporate corporate =new Corporate((long)346, "Test Corporate1", null, null, null, 
//				null, null, null, null, null, null, null, null, "41FED05D-A664-41FA-B074-E538EF84CBCA", null, null, null) {};
//		JoinCorporateStaffCommand cmd = new JoinCorporateStaffCommand(session, corporate);
//		Integer b =sendCmd(cmd);
//		assertTrue(b>0);
	}
}
