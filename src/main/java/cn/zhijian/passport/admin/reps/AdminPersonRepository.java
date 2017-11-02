package cn.zhijian.passport.admin.reps;

import cn.zhijian.passport.admin.db.AdminPersonMapper;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.pay.db.PayMapper;

public class AdminPersonRepository {
	final AdminPersonMapper adminPersonMapper;
	
	public AdminPersonRepository(AdminPersonMapper adminPersonMapper) {
		// TODO Auto-generated constructor stub
		this.adminPersonMapper = adminPersonMapper;
	}
	
	public PagedResult<PersonRow> loadPersonList(PagingQuery query)
	{
		int totalRows = this.adminPersonMapper.getPersonCount(query);
		int s = query.getPageSize() == null? totalRows : query.getPageSize();
		return new PagedResult<>(this.adminPersonMapper.getPersonList(query),totalRows,query.getPageNo(),query.getPageSize());
	}
}
