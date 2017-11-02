package cn.zhijian.passport.domain.commandhandlers;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Resource;
import cn.zhijian.passport.commands.CreateResourceCommand;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.row.ResourceRow;
import cn.zhijian.passport.domain.exceptions.NoSessionException;
import cn.zhijian.passport.session.SessionStore;

public class ResourceCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(ResourceCommandHandler.class);

	final ResourceDAO dao;
	final SessionStore sessionStore;

	public ResourceCommandHandler(SessionStore sessionStore, ResourceDAO dao) {
		this.sessionStore = sessionStore;
		this.dao = dao;
	}

	/**
	 * Return UUID of the resource
	 * 
	 * @param cmd
	 * @return
	 * @throws NoSessionException
	 */
	@CommandHandler
	public String create(CreateResourceCommand cmd) throws NoSessionException {
		String id = sessionStore.doInSession(cmd.getSessionId(), ctx -> {
			String uuid = UUID.randomUUID().toString();
			Resource r = cmd.getData();
			ResourceRow row = new ResourceRow(uuid, r.getName(), r.getContentType(), r.getContent(),
					ctx.getPerson().getId(), ctx.getPerson().getUsername(), new Date(), null, null);
			dao.insert(row);
			logger.debug("Resource Created: {} - {} ({})", uuid, r.getName(), r.getContentType());
			return uuid;
		}, null);
		if (id == null) {
			throw new NoSessionException();
		}
		return id;
	}

}
