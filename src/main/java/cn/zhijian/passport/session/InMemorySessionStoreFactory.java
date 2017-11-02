package cn.zhijian.passport.session;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;

import cn.zhijian.passport.bundles.HelpSession;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.db.CurrSessionMapper;

public class InMemorySessionStoreFactory implements SessionStoreFactory {

	@Override
	public SessionStore create(SessionStoreConfig config,ObjectStore objStore) {
		return new InMemorySessionStore(objStore.get(CurrSessionMapper.class));
	}
}
