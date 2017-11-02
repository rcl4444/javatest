package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class Content {

	final String name;
	final String contentType;
	final byte[] content;

}
