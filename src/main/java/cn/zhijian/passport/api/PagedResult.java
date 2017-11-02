package cn.zhijian.passport.api;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class PagedResult<T> {

	final List<T> result;
	final int totalCount;
	final int pageNo;
	final int pageSize;

	public <P> PagedResult<P> map(Function<T, P> transferFunc) {
		return new PagedResult<>( //
				result.stream().map(transferFunc).collect(Collectors.toList()), //
				totalCount, pageNo, pageSize);
	}

}
