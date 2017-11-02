package cn.zhijian.passport.session;

import org.axonframework.commandhandling.gateway.CommandGateway;

import cn.zhijian.passport.bundles.ObjectStore;

public interface SessionStoreFactory {

	SessionStore create(SessionStoreConfig config,ObjectStore objStore);

}
