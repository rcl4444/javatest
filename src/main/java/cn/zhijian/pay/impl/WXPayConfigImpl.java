package cn.zhijian.pay.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import cn.zhijian.pay.sdk.IWXPayDomain;
import cn.zhijian.pay.sdk.WXPayConfig;

public class WXPayConfigImpl extends WXPayConfig {

	private byte[] certData;
	private static WXPayConfigImpl INSTANCE;
	
	private String appID;
	private String mchID;
	private String key;
	

	private WXPayConfigImpl(String appID, String mchID, String key, String cert) throws Exception {
		this.appID = appID;
		this.mchID = mchID;
		this.key = key;
//		InputStream certStream = this.getClass().getResourceAsStream("/cert/apiclient_cert.p12");
		InputStream certStream = this.getClass().getResourceAsStream(cert);
		this.certData = IOUtils.toByteArray(certStream);
		certStream.read(this.certData);
		certStream.close();
	}

	public static WXPayConfigImpl getInstance(String appID, String mchID, String key, String cert) throws Exception {
		if (INSTANCE == null) {
			synchronized (WXPayConfigImpl.class) {
				if (INSTANCE == null) {
					INSTANCE = new WXPayConfigImpl(appID,mchID,key,cert);
				}
			}
		}
		return INSTANCE;
	}

	public String getAppID() {
		return this.appID;
	}

	public String getMchID() {
		return this.mchID;
	}

	public String getKey() {
		return this.key;
	}

	public InputStream getCertStream() {
		ByteArrayInputStream certBis;
		certBis = new ByteArrayInputStream(this.certData);
		return certBis;
	}

	public int getHttpConnectTimeoutMs() {
		return 2000;
	}

	public int getHttpReadTimeoutMs() {
		return 10000;
	}

	public IWXPayDomain getWXPayDomain() {
		return WXPayDomainSimpleImpl.instance();
	}

	public String getPrimaryDomain() {
		return "api.mch.weixin.qq.com";
	}

	public String getAlternateDomain() {
		return "api2.mch.weixin.qq.com";
	}

	@Override
	public int getReportWorkerNum() {
		return 1;
	}

	@Override
	public int getReportBatchSize() {
		return 2;
	}
}
