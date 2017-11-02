package cn.zhijian.shipper.bundles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.session.InMemorySessionStore;
import cn.zhijian.passport.session.SessionStore;

public class ObjectStoreTest {

	@Test
	public void test() {
		
		String sr = String.format("Hi,%s:%s.%s", "王南","王力","王张");
		assertEquals(1, ObjectStore.getClassHier(Object.class).size());

		System.out.println(ObjectStore.getClassHier(String.class));
		System.out.println(ObjectStore.getClassHier(InMemorySessionStore.class));

		ObjectStore s = new ObjectStore();
		s.put(new InMemorySessionStore());

		SessionStore ss = s.get(SessionStore.class);
		assertNotNull(ss);

		InMemorySessionStore is = s.get(InMemorySessionStore.class);
		assertEquals(ss, is);

	}

}
