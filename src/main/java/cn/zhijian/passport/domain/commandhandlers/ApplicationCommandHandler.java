package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.AppAppendPowerInfo;
import cn.zhijian.passport.commands.AppAppendPowerCommand;
import cn.zhijian.passport.commands.SearchAppPowerCommand;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;

public class ApplicationCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(ApplicationCommandHandler.class);
	
	final SqlSessionManager sqlSessionManager;
	
	public ApplicationCommandHandler(SqlSessionManager sqlSessionManager){
		
		this.sqlSessionManager = sqlSessionManager;
	}
	
	@CommandHandler
	public Pair<Boolean,String> appendAppPower(AppAppendPowerCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				Date currDate = new Date();
				ApplicationModuleDao sessionAppModuleMapper = session.getMapper(ApplicationModuleDao.class);
				List<Map<String, Object>> tb = new ArrayList<>();
				for(AppAppendPowerInfo.AppModule module : cmd.getModules()){
					String modulename = module.getModulename();
					for(AppAppendPowerInfo.AppModuleOperation operation : module.getOperations()){
						Map<String,Object> row = new HashMap<String,Object>();
						row.put("appid", cmd.getAppid());
						row.put("modulename", modulename);
						row.put("operationname", operation.getOperationname());
						tb.add(row);
					}
				}
				List<ModuleOperationRow> existOperations = sessionAppModuleMapper.findOperationByTable(tb);
				if(existOperations.size() > 0){
					return Pair.of(false, String.format("操作\"%s\"在系统中已存在", StringUtils.join(existOperations.stream().map(o->o.getOperationname()).collect(Collectors.toList()),",")));
				}
				List<ApplicationModuleRow> appModules = sessionAppModuleMapper.findModuleByName(cmd.getAppid(), 
						cmd.getModules().stream().map(o->o.getModulename()).collect(Collectors.toList()));
				for(AppAppendPowerInfo.AppModule module : cmd.getModules()){
					ApplicationModuleRow appmodule;
					if(!appModules.stream().filter(o->o.getModulename().equals(module.getModulename())).findFirst().isPresent()){
						appmodule = new ApplicationModuleRow();
						appmodule.setApplicationid(cmd.getAppid());
						appmodule.setModulename(module.getModulename());
						appmodule.setCreatedate(currDate);
						sessionAppModuleMapper.insert(appmodule);
					}
					else{
						appmodule = appModules.stream().filter(o->o.getModulename().equals(module.getModulename())).findFirst().get();
					}
					for(AppAppendPowerInfo.AppModuleOperation operation : module.getOperations()){
						ModuleOperationRow mo = new ModuleOperationRow();
						mo.setApplicationid(cmd.getAppid());
						mo.setModuleid(appmodule.getId());
						mo.setOperationname(operation.getOperationname());
						mo.setCreatedate(currDate);
						sessionAppModuleMapper.insertOperation(mo);
					}	
				}
				session.commit();
				return Pair.of(true, "添加完毕");
			} catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}

	@CommandHandler
	public List<Object> searchAppPower(SearchAppPowerCommand cmd){
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				List<Object> result = new ArrayList<>();
				ApplicationModuleDao sessionAppModuleMapper = session.getMapper(ApplicationModuleDao.class);
				List<ApplicationModuleRow> modules = sessionAppModuleMapper.findByApplicationid(cmd.getAppid());
				List<ModuleOperationRow> operations =  sessionAppModuleMapper.findOperationByAppid(cmd.getAppid());
				for(ApplicationModuleRow module : modules){
					Map<String,Object> appModule = new HashMap<String,Object>();
					appModule.put("modulename",module.getModulename());
					List<Map<String,Object>> appOperations = operations.stream().filter(o->o.getModuleid().equals(module.getId())).map(o->{
						Map<String,Object> appOperation = new HashMap<String,Object>();
						appOperation.put("operationname", o.getOperationname());
						return appOperation;
					}).collect(Collectors.toList());
					appModule.put("operations",appOperations);
					result.add(appModule);
				}
				return result;
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}
}