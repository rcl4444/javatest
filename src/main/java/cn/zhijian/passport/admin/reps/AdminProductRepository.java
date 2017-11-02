package cn.zhijian.passport.admin.reps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.admin.db.AdminProductMapper;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.PersonOption;
import cn.zhijian.passport.db.row.ProductRow.ProductAppModuleRow;
import cn.zhijian.passport.db.row.ProductRow.ProductPriceRow;
import cn.zhijian.passport.db.row.ProductRow.UseTimeOption;
import cn.zhijian.passport.statustype.BusinessType;

public class AdminProductRepository {

	final AdminProductMapper productMapper;
	
	final AdminApplicationMapper appMapper;
	
	public AdminProductRepository(AdminProductMapper productMapper,AdminApplicationMapper appMapper){
		
		this.productMapper = productMapper;
		this.appMapper = appMapper;
	}
	
	public PagedResult<ProductRow> filterApplication(PagingQuery query){
		
		int totalRows = this.productMapper.getProductCount(query);
		query.setPageSize(query.getPageSize() == null? 10 : query.getPageSize());
		return new PagedResult<>(this.productMapper.getProductList(query),totalRows,query.getPageNo(),query.getPageSize());
	}
	
	public ProductRow findById(long productid){
		
		return this.productMapper.load(productid);
	}
	
	public List<Object> getProductPower(Long productid){
		
		List<Object> result = new ArrayList<>();
		ProductRow pr = this.productMapper.load(productid);
		if(pr == null){
			return result;
		}
		List<ApplicationRow> allApps = this.appMapper.getAppByType(pr.getType());
		List<ApplicationModuleRow> allAppModules = this.appMapper.getAppModuleByAppId(allApps.stream().map(o->o.getId()).collect(Collectors.toList()));
		List<ProductAppModuleRow> pams = this.productMapper.getProductModuleByProductId(productid);
		for(ApplicationRow app : allApps){
			Map<String,Object> a = new HashMap<>();
			a.put("appid",app.getId());
			a.put("appName", app.getAppname());
			List<Map<String,Object>> am = new ArrayList<>();
			List<ApplicationModuleRow> appModules = allAppModules.stream().filter(o->o.getApplicationid().equals(app.getId())).collect(Collectors.toList());
			for(ApplicationModuleRow appModule : appModules){
				Map<String,Object> m = new HashMap<>();
				m.put("moduleid", appModule.getId());
				m.put("modulename",appModule.getModulename());
				m.put("status",pams.stream().filter(o->o.getApplicationmoduleid().equals(appModule.getId())).count() > 0 ? true : false);
				am.add(m);
			}
			a.put("modules", am);
			result.add(a);
		}
		return result;
	}
	
	public List<Object> getProductPower(BusinessType type){
		
		List<Object> result = new ArrayList<>();
		List<ApplicationRow> allApps = this.appMapper.getAppByType(type);
		List<ApplicationModuleRow> allAppModules = this.appMapper.getAppModuleByAppId(allApps.stream().map(o->o.getId()).collect(Collectors.toList()));
		for(ApplicationRow app : allApps){
			Map<String,Object> a = new HashMap<>();
			a.put("appid",app.getId());
			a.put("appName", app.getAppname());
			List<Map<String,Object>> am = new ArrayList<>();
			List<ApplicationModuleRow> appModules = allAppModules.stream().filter(o->o.getApplicationid().equals(app.getId())).collect(Collectors.toList());
			for(ApplicationModuleRow appModule : appModules){
				Map<String,Object> m = new HashMap<>();
				m.put("moduleid", appModule.getId());
				m.put("modulename",appModule.getModulename());
				m.put("status",false);
				am.add(m);
			}
			a.put("modules", am);
			result.add(a);
		}
		return result;
	}
	
	public List<ProductPriceRow> getProductPrice(long productid){
		
		List<ProductPriceRow> rows = this.productMapper.getProductPriceByProductId(productid);
		if(rows.size() == 0){
			rows = new ArrayList<ProductPriceRow>();
			for(UseTimeOption time: UseTimeOption.values()){
				for(PersonOption person: PersonOption.values()){
					ProductPriceRow pp = new ProductPriceRow();
					pp.setUsetime(time);
					pp.setPersonnum(person);
					rows.add(pp);
				}
			}
		}
		return rows;
	}
}