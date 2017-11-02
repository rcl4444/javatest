package cn.zhijian.passport.config;

import java.io.UnsupportedEncodingException;

import lombok.Data;

@Data
public class SiteConfig {

	/**
	 * 前端访问路径, http://192.168.3.146/
	 */
	String siteUrl;
	
	//jwtt的加密密钥
	String jwtKey;
	
	String jwtCookieName;
	
	String jwtPrefix;
	
    public byte[] getJwtTokenSecret() throws UnsupportedEncodingException {
        return jwtKey.getBytes("UTF-8");
    }
}
