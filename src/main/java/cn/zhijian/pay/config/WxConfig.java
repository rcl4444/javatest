package cn.zhijian.pay.config;

import lombok.Data;

@Data
public class WxConfig {
	
	String appID;
	String mchID;
	String key;
	String cert;
	String notify;
}
