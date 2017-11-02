package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class Resource {

	final String id;
	final String name;
	final String contentType;
	final byte[] content;

}
