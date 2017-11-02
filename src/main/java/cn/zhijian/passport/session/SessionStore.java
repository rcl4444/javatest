package cn.zhijian.passport.session;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.commands.CallerInfo;

public interface SessionStore {

	static Logger _logger = LoggerFactory.getLogger(SessionStore.class);

	void put(String sessionId, LoginContext ctx);
	
	void remove(String sessionId);

	LoginContext get(String sessionId);

	default <T> T doInSession(String sessionId, Function<LoginContext, T> func) {
		return doInSession(sessionId, func, null);
	}

	default <T> T doInSession(String sessionId, Function<LoginContext, T> func, SessionInvalidHandler<T> handler) {
		LoginContext ctx = get(sessionId);
		if (ctx != null) {
			return func.apply(ctx);
		}
		if (handler != null) {
			return handler.handle();
		}
		return null;
	}

	/**
	 * 
	 * @param sessionId
	 * @param appId
	 *            应用ID
	 * @param roleString
	 *            该应用的角色ID
	 * @param successFunc
	 *            验证通过调用此函数
	 * @param failFunc
	 *            验证失败调用此函数，入参的LoginContext有可能是NULL
	 * @return successFunc / failFunc 返回的值
	 */
	default <T> T doInRole(String sessionId, String appId, String roleString, Function<LoginContext, T> successFunc,
			Function<LoginContext, T> failFunc) {
		LoginContext ctx = get(sessionId);
		// XXX check role
		_logger.warn("doInRole() not implemented yet");
		if (ctx != null) {
			return successFunc.apply(ctx);
		} else {
			if (failFunc != null) {
				return failFunc.apply(ctx);
			}
		}
		throw new RuntimeException("Not Authorized");
	}

	default <T> T callerInRole(String sessionId, String appId, String roleString, Function<CallerInfo, T> func) {
		return doInRole(sessionId, appId, roleString, ctx -> {
			CallerInfo cinfo = new CallerInfo(ctx.getPerson().getId(), ctx.getPerson().getUsername(),
					ctx.getCurrentCorporate() != null ? ctx.getCurrentCorporate().getId() : null);
			return func.apply(cinfo);
		}, ctx -> {
			throw new RuntimeException("Not Authorized");
		});
	}

	@FunctionalInterface
	public static interface SessionInvalidHandler<T> {
		T handle();
	}
}