package cn.zhijian.passport.admin.reps;

import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationRow;

public class AdminApplicationRepository {

	final AdminApplicationMapper appMapper;
	
	public AdminApplicationRepository(AdminApplicationMapper appMapper){
	
		this.appMapper = appMapper;
	}
	
	public PagedResult<ApplicationRow> filterApplication(PagingQuery query){
		
		int totalRows = this.appMapper.getAppCount(query);
		query.setPageSize(query.getPageSize() == null? 10 : query.getPageSize());
		return new PagedResult<>(this.appMapper.getAppList(query),totalRows,query.getPageNo(),query.getPageSize());
	}
	
	public ApplicationRow findById(long appid){
		
		return this.appMapper.load(appid);
	}
}