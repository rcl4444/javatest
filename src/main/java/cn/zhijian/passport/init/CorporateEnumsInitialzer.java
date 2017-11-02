package cn.zhijian.passport.init;

import java.util.ArrayList;
import java.util.List;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.statustype.CorporateEnum;
import io.dropwizard.setup.Environment;

public class CorporateEnumsInitialzer implements AxonDropwizardInitializer<BackendConfiguration>{

	@Override
	public void config(Configurer axConfig, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
		// TODO Auto-generated method stub
		
		List<CorporateEnum> corporateEnums = new ArrayList<>();
		corporateEnums.add(CorporateEnum.Audit_Pass);
		corporateEnums.add(CorporateEnum.Authentication_Apply);
		corporateEnums.add(CorporateEnum.Authentication_Not);
		corporateEnums.add(CorporateEnum.Authentication_Pass);
		corporateEnums.add(CorporateEnum.Authentication_Reject);
		CorporateEnums s = new CorporateEnums(corporateEnums);
		objStore.put(s);
	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
		// TODO Auto-generated method stub
		
	}

}
