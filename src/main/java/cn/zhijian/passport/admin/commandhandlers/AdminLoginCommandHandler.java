package cn.zhijian.passport.admin.commandhandlers;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.AdminLoginCommand;
import cn.zhijian.passport.admin.db.AdminPersonMapper;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.domain.crypto.PasswordEncrypter;

import cn.zhijian.passport.db.row.AdminPersonRow;

public class AdminLoginCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(AdminLoginCommandHandler.class);
	
	final AdminPersonMapper dao;
	
	public AdminLoginCommandHandler(AdminPersonMapper dao){
		
		this.dao = dao;
	}
	
	@CommandHandler
	public Pair< String, AdminPersonRow> validUser(AdminLoginCommand cmd) {

		String hashedPassword = PasswordEncrypter.encrypt(cmd.getPassword());
		AdminPersonRow row = dao.findPersonByUsername(cmd.getUsername());
		if(row==null){
			return Pair.of("用户名不存在", null);
		}
		if(!row.getPassword().equals(hashedPassword)){
			return Pair.of("密码错误", null);
		}
		
		return Pair.of(null, row);
	}
}
