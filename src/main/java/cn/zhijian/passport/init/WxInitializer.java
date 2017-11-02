package cn.zhijian.passport.init;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.pay.impl.WXPayConfigImpl;
import cn.zhijian.pay.sdk.WXPay;
import cn.zhijian.pay.sdk.WXPayConfig;
import io.dropwizard.setup.Environment;

public class WxInitializer implements AxonDropwizardInitializer<BackendConfiguration> {
	protected static Logger logger = LoggerFactory.getLogger(WxInitializer.class);

	@Override
	public void config(Configurer axConfig, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
		
//		final String siteUrl = dwConfig.getSite().getSiteUrl() + "/wxnotify";
//		final String siteUrl = "http://wx-test.yunbaoguan.cn/pay/wxnotify";
		try {
			final String appId = dwConfig.getWx().getAppID();
			final String mchID = dwConfig.getWx().getMchID();
			final String key = dwConfig.getWx().getKey();
			final String cert = dwConfig.getWx().getCert();
			final String notify = dwConfig.getWx().getNotify();
			
			WXPayConfig config = WXPayConfigImpl.getInstance(appId,mchID,key,cert);

//			WXPay wx = new WXPay(config, siteUrl,true,true);
			WXPay wx = new WXPay(config, notify);
			
			objStore.put(wx);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
	}
}
