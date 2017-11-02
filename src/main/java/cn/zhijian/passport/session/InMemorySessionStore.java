package cn.zhijian.passport.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.GenericEventMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.bundles.HelpSession;
import cn.zhijian.passport.db.CurrSessionMapper;
import cn.zhijian.passport.db.row.CurrSessionRow;
import cn.zhijian.passport.domain.events.PersonLoginEvent;
import cn.zhijian.passport.domain.events.PersonLoginOutEvent;
import cn.zhijian.passport.domain.events.PersonLoginReplaceEvent;

public class InMemorySessionStore implements SessionStore {

	private static Cache<String, LoginContext> MEM;
	private static Map<Long, String> loginUser;

	ObjectMapper mapper = new ObjectMapper();

	CurrSessionMapper sessionMapper;
	
	public InMemorySessionStore() {
	}

	public InMemorySessionStore(CurrSessionMapper sessionMapper) {
		this.sessionMapper = sessionMapper;
		CacheLoader<String, LoginContext> loader = new CacheLoader<String, LoginContext>() {
			@Override
			public LoginContext load(String key) {
				return null;
			}
		};
		MEM = CacheBuilder.newBuilder().refreshAfterWrite(5, TimeUnit.MINUTES).build(loader);
		loginUser = new HashMap<>();
		try{
			List<CurrSessionRow> sessions = this.sessionMapper.loadAll();
			if (sessions.size() > 0) {
				sessions.stream().forEach(o -> {
					try {
						this.loginUser.put(o.getPersonid(), o.getSessionid());
						this.MEM.put(o.getSessionid(), mapper.readValue(o.getContent(), LoginContext.class));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				});
			}
		}catch (Exception e) {
			//e.printStackTrace();
		}
	}

	@Override
	public void put(String sessionid, LoginContext ctx) {
		if (StringUtils.isEmpty(sessionid)) {
			throw new RuntimeException("Session is NULL");
		}
		if (ctx.getPerson() != null) {
			if (!this.loginUser.containsKey(ctx.getPerson().getId())) {
				HelpSession.getEventBus().publish(new GenericEventMessage<>(new PersonLoginEvent(ctx)));
			} else if (!this.loginUser.get(ctx.getPerson().getId()).equals(ctx.getSessionId())) {
				HelpSession.getEventBus().publish(new GenericEventMessage<>(new PersonLoginReplaceEvent(this.loginUser.get(ctx.getPerson().getId()),ctx)));
				this.MEM.invalidate(this.loginUser.get(ctx.getPerson().getId()));
			}
			this.loginUser.put(ctx.getPerson().getId(), ctx.getSessionId());
			this.MEM.put(sessionid, ctx);
		}
	}

	@Override
	public LoginContext get(String sessionId) {
		if (sessionId != null) {
			return MEM.getIfPresent(sessionId);
		} else {
			return null;
		}
	}

	@Override
	public void remove(String sessionid) {
		if (!StringUtils.isEmpty(sessionid)) {
			LoginContext lc = this.get(sessionid);
			if (lc != null) {
				HelpSession.getEventBus().publish(new GenericEventMessage<>(new PersonLoginOutEvent(sessionid,lc.getPerson().getId())));
				this.loginUser.remove(lc.getPerson().getId());
				this.MEM.invalidate(sessionid);
			}
		}
	}
}
