package cn.zhijian.passport.admin.reps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.api.Resource;

public class AdminResourceRepository {

	final static Logger logger = LoggerFactory.getLogger(AdminResourceRepository.class);

	final AdminResourceDAO dao;

	public AdminResourceRepository(AdminResourceDAO dao) {
		this.dao = dao;
	}

	public Resource load(String resourceId) {
		return convert(dao.load(resourceId));
	}

	private Resource convert(AdminResourceRow row) {
		if (row == null) {
			return null;
		}
		return new Resource(row.getId(), row.getName(), row.getContentType(), row.getContent());
	}
}
