package cn.zhijian.passport.api;

import java.util.List;

import lombok.Data;

@Data
public class AppAppendPowerInfo {

	String clientid;
	String clientsecret;
	List<AppModule> modules;
	
	@Data
	public static class AppModule{
		
		String modulename;
		List<AppModuleOperation> operations;
	}
	
	@Data
	public static class AppModuleOperation{
		
		String operationname;
	}
}