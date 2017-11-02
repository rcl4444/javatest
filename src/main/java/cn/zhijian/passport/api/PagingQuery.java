package cn.zhijian.passport.api;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PagingQuery {
	
	List<QueryInfo> query;
	Map<String,String> sort;
	Integer pageNo;
	Integer pageSize;
}