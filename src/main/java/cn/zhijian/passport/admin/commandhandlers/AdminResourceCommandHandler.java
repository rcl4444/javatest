package cn.zhijian.passport.admin.commandhandlers;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.AdminCreateResourceCommand;
import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.domain.exceptions.NoSessionException;

public class AdminResourceCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(AdminResourceCommandHandler.class);

	final AdminResourceDAO dao;

	public AdminResourceCommandHandler(AdminResourceDAO dao) {
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
	public String create(AdminCreateResourceCommand cmd){
		
		String uuid = UUID.randomUUID().toString();
		AdminResourceRow row = new AdminResourceRow(uuid, cmd.getName(), cmd.getContentType(), cmd.getContent(),
				cmd.getUploadpersonid(), cmd.getUploadpersonname(), new Date(), null, null);
		dao.insert(row);
		return uuid;
	}

}
