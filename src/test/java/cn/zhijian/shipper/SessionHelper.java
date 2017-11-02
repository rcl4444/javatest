package cn.zhijian.shipper;

import com.google.common.collect.Lists;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.Person;

public class SessionHelper {

	public static LoginContext makeContext(String sessionId, long pid, long cid) {
		Person p = new Person(pid, "test", "Test Person", null, null, null, null, null, null, null, null, null,null,null,null,null);
		Corporate c = new Corporate(cid, "Test Corporate", null, null, null,null, null, null, null, null, null,null, null, null,null,null,null,null,null,null,null,null,null,null,null);
		LoginContext ctx = new LoginContext(sessionId, p, c, Lists.newArrayList(c),null,null);
		return ctx;
	}
}
