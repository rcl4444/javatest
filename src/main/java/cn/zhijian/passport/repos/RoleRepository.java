package cn.zhijian.passport.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.zhijian.passport.api.CorporateRole;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.row.CorporateModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;
import cn.zhijian.passport.db.row.RoleOperationRow;
import cn.zhijian.trade.db.VoucherMapper;
import cn.zhijian.trade.db.row.VoucherApplicationModuleView;
import cn.zhijian.trade.db.row.VoucherApplicationView;
import cn.zhijian.trade.db.row.VoucherRow;
import lombok.Data;

public class RoleRepository {

	final CorporateMapper corporateMapper;
	final CorporateRoleMapper roleMapper;
	final VoucherMapper voucherMapper;
	
	public RoleRepository(CorporateMapper corporateMapper,CorporateRoleMapper roleMapper,VoucherMapper voucherMapper){
		this.corporateMapper = corporateMapper;
		this.roleMapper = roleMapper;
		this.voucherMapper = voucherMapper;
	}
	
	public PagedResult<CorporateRole> loadCorporateRole(long corpId,String query, Integer pageNo, Integer pageSize)
	{
		String q = null;
		if(query != null && !query.trim().isEmpty())
		{
			q = "%" + query.trim() + "%";
		}
		int offset = (pageNo - 1) * pageSize;
		int totalRows = this.roleMapper.countByCorporateRole(q,corpId);
		int s = pageSize == null? totalRows : pageSize;
		return new PagedResult<>(this.roleMapper.loadByCorporateRole(q,corpId, offset, s),totalRows,pageNo,pageSize).map(this::convert);
	}
	
	private CorporateRole convert(CorporateRoleRow row) {
		return new CorporateRole(row.getId(),row.getRolename(),row.getDescription(),null,null);
	}
	
	public Object GetRoleInfo(Long corporateid,Long roleid,String walletId){
		
		List<VoucherRow> vouchers = this.voucherMapper.findByWalletId(walletId);
		List<VoucherApplicationView> apps = vouchers.size() > 0 ? this.voucherMapper.findAppByVoucherId(vouchers.stream().map(o->o.getId()).collect(Collectors.toList()))
				: new ArrayList<>();
		List<VoucherApplicationModuleView> modules = vouchers.size() > 0 ?  this.voucherMapper.findAppModuleByVoucherId(vouchers.stream()
				.map(o->o.getId()).collect(Collectors.toList())) : new ArrayList<>();
		List<ModuleOperationRow> operations = modules.size() > 0 ? this.roleMapper.getAppModuleOperationByModule(modules.stream()
				.map(o->o.getApplicationmoduleid()).collect(Collectors.toList())) : new ArrayList<>();
		
		RoleInfo result = new RoleInfo();
		List<RoleOperationRow> rolePower;
		if(roleid!=null && roleid > 0){
			CorporateRoleRow role = this.roleMapper.load(roleid);
			result.setRoleid(role.getId());
			result.setRolename(role.getRolename());
			result.setRoledescription(role.getDescription());
			rolePower = this.roleMapper.getRolePower(roleid);
		}
		else{
			rolePower = new ArrayList<>();
		}
		List<VoucherInfo> voucherinfos = new ArrayList<>();
		for(VoucherRow vr : vouchers){
			VoucherInfo voucherinfo = new VoucherInfo();
			voucherinfo.setCertificateid(vr.getId());
			voucherinfo.setCertificateno(vr.getVoucherNo());
			List<VoucherApplicationView> vapps = apps.stream().filter(o->o.getVoucherid().equals(vr.getId())).collect(Collectors.toList());
			List<RoleAppsInfo> roleApps = new ArrayList<>();
			for(VoucherApplicationView app : vapps){
				RoleAppsInfo roleApp = new RoleAppsInfo();
				roleApp.setAppid(app.getApplicationid());
				roleApp.setAppname(app.getApplicationname());
				List<RoleAppsModuleInfo> roleAppModules = new ArrayList<>();
				List<VoucherApplicationModuleView> appModules = modules.stream()
						.filter(o->o.getApplicationid()==app.getApplicationid()&& o.getVoucherid() == vr.getId())
						.collect(Collectors.toList());
				for(VoucherApplicationModuleView module: appModules){
					RoleAppsModuleInfo roleAppModule = new RoleAppsModuleInfo();
					roleAppModule.setModuleid(module.getApplicationmoduleid());
					roleAppModule.setModulename(module.getApplicationmodulename());
					List<RoleAppsModuleOperationInfo> roleAppModuleOperations = new ArrayList<>();
					List<ModuleOperationRow> appModuleOperation = operations.stream()
							.filter(o->o.getModuleid().equals(module.getApplicationmoduleid())).collect(Collectors.toList());
					for(ModuleOperationRow operation :appModuleOperation){
						RoleAppsModuleOperationInfo roleAppModuleOperation = new RoleAppsModuleOperationInfo();
						roleAppModuleOperation.setOperationid(operation.getId());
						roleAppModuleOperation.setOperationname(operation.getOperationname());
						if(rolePower.stream().filter(o->o.getOperationid()==operation.getId()&&o.getVoucherid()==vr.getId()).count()>0){
							roleAppModuleOperation.setStatus(1);
						}
						else{
							roleAppModuleOperation.setStatus(0);
						}
						roleAppModuleOperations.add(roleAppModuleOperation);
					}
					roleAppModule.setOperations(roleAppModuleOperations);
					roleAppModules.add(roleAppModule);
				}
				roleApp.setModules(roleAppModules);
				roleApps.add(roleApp);
			}
			voucherinfo.setApps(roleApps);
			voucherinfos.add(voucherinfo);
		}
		result.setCertificates(voucherinfos);
		
		List<CorporateModuleRow> corporateModules = roleMapper.findAllModule();
		List<CorporateRoleModuleRow> corporateRoleModuleRows = roleMapper.findRoleModulebyCorpId(corporateid,roleid);
		List<RoleModuleInfo> roleModuleInfo = new ArrayList<>();
		for (CorporateModuleRow corporateModuleRow : corporateModules) {
			RoleModuleInfo info = new RoleModuleInfo();
			info.setModuleid(corporateModuleRow.getId());
			info.setModuleName(corporateModuleRow.getModuleName());
			
			if(corporateRoleModuleRows.stream().filter(o->o.getModuleId().equals(corporateModuleRow.getId())).count()>0)
			{
				info.setStatus(1);
			}
			else {
				info.setStatus(0);
			}
			roleModuleInfo.add(info);
		}
		result.setMolus(roleModuleInfo);
		return result;
	}
	
	@Data
	private class RoleAppsModuleOperationInfo{
		
		Long operationid;
		String operationname;
		int status;
	}
	
	@Data
	private class RoleAppsModuleInfo{
		
		Long moduleid;
		String modulename;
		List<RoleAppsModuleOperationInfo> operations;
	}
	
	@Data
	private class RoleAppsInfo{
		
		Long appid;
		String appname;
		List<RoleAppsModuleInfo> modules;
	}
	
	@Data
	private class VoucherInfo{
		Long certificateid;
		String certificateno;
		List<RoleAppsInfo> apps;
	}
	
	@Data
	private class RoleInfo{
	    Long roleid;
	    String rolename;
	    String roledescription;
	    List<VoucherInfo> certificates;
	    List<RoleModuleInfo> molus;
	}
	
	@Data
	private class RoleModuleInfo{
		long moduleid;
		String moduleName;
		int status;
	}
}