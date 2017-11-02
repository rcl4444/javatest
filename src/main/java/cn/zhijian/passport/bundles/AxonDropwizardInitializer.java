package cn.zhijian.passport.bundles;

import cn.zhijian.passport.bundles.AxonBundle.AxonInitFunc;
import cn.zhijian.passport.bundles.AxonBundle.AxonStartupCallback;

public interface AxonDropwizardInitializer<T> extends AxonInitFunc<T>, AxonStartupCallback<T> {

}
