package cn.zhijian.trade.api;

import java.util.List;

import lombok.Data;

@Data
public class JoinCartRequest {
	final List<Object> products;
}
