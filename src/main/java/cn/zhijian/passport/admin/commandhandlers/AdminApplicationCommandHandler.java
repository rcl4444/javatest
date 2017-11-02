package cn.zhijian.passport.admin.commandhandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.ApplicationCreateCommand;
import cn.zhijian.passport.admin.commands.ApplicationModifyCommand;
import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;

public class AdminApplicationCommandHandler {

	final Logger logger = LoggerFactory.getLogger(AdminApplicationCommandHandler.class);
	
	final SqlSessionManager sqlSessionManager;
	
	public AdminApplicationCommandHandler(SqlSessionManager sqlSessionManager){
		
		this.sqlSessionManager = sqlSessionManager;
	}
	
	@CommandHandler
	public Pair<Boolean,String> appCreate(ApplicationCreateCommand cmd){
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				Date currDate = new Date();
				AdminApplicationMapper appMapper = session.getMapper(AdminApplicationMapper.class);
				if(appMapper.getAppByName(cmd.getAddInfo().getAppname(),CodeEnumUtil.codeOf(BusinessType.class,cmd.getAddInfo().getType()), null)>0){
					return Pair.of(false, String.format("应用名称%s已经存在",cmd.getAddInfo().getAppname()));
				}
				ApplicationRow addrow = new ApplicationRow();
				addrow.setAppname(cmd.getAddInfo().getAppname());
				addrow.setClientid(UUID.randomUUID().toString());
				addrow.setClientsecret(UUID.randomUUID().toString());
				addrow.setScope("api");
				addrow.setCallbackurl(cmd.getAddInfo().getCallbackLink());
				addrow.setMainurl(cmd.getAddInfo().getWebsiteLink());
				addrow.setGetInfoUrl(cmd.getAddInfo().getDataLink());
				addrow.setLoginouturl(cmd.getAddInfo().getExitLink());
				addrow.setAvatarresourceid(cmd.getAddInfo().getActiveid());
				addrow.setCreatedate(currDate);
				addrow.setType(CodeEnumUtil.codeOf(BusinessType.class, cmd.getAddInfo().getType()));
				appMapper.insert(addrow);
				session.commit();
				return Pair.of(true, StringUtils.EMPTY);
			} 
			catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}
	
	@CommandHandler
	public Pair<Boolean,String> appModify(ApplicationModifyCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				AdminApplicationMapper appMapper = session.getMapper(AdminApplicationMapper.class);
				ApplicationRow modifyRow = appMapper.load(cmd.getModifyInfo().getId());
				if(modifyRow == null){
					return Pair.of(false, "应用信息不存在");
				}
				if(appMapper.getAppByName(cmd.getModifyInfo().getAppname(), CodeEnumUtil.codeOf(BusinessType.class, cmd.getModifyInfo().getType()),
						Arrays.asList(modifyRow.getId()))>0){
					return Pair.of(false, String.format("应用名称%s已经存在",cmd.getModifyInfo().getAppname()));
				}
				modifyRow.setAppname(cmd.getModifyInfo().getAppname());
				modifyRow.setCallbackurl(cmd.getModifyInfo().getCallbackLink());
				modifyRow.setMainurl(cmd.getModifyInfo().getWebsiteLink());
				modifyRow.setGetInfoUrl(cmd.getModifyInfo().getDataLink());
				modifyRow.setLoginouturl(cmd.getModifyInfo().getExitLink());
				modifyRow.setAvatarresourceid(cmd.getModifyInfo().getActiveid());
				modifyRow.setType(CodeEnumUtil.codeOf(BusinessType.class, cmd.getModifyInfo().getType()));
				appMapper.update(modifyRow,modifyRow.getId());
				session.commit();
				return Pair.of(true, StringUtils.EMPTY);
			} 
			catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}
}