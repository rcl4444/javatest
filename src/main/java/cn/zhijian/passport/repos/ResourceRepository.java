package cn.zhijian.passport.repos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.Resource;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.row.ResourceRow;
import cn.zhijian.passport.session.SessionStore;

public class ResourceRepository {

	final static Logger logger = LoggerFactory.getLogger(ResourceRepository.class);

	final ResourceDAO dao;
	final SessionStore sessionStore;

	public ResourceRepository(SessionStore sessionStore, ResourceDAO dao) {
		this.dao = dao;
		this.sessionStore = sessionStore;
	}

	public Resource load(String sessionId, String resourceId) {
		return sessionStore.doInSession(sessionId, ctx -> convert(dao.load(resourceId)), null);
	}

	private Resource convert(ResourceRow row) {
		if (row == null) {
			return null;
		}
		return new Resource(row.getId(), row.getName(), row.getContentType(), row.getContent());
	}
}
