package cn.zhijian.passport.init;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.admin.db.AdminFinanceMapper;
import cn.zhijian.passport.admin.db.AdminPersonMapper;
import cn.zhijian.passport.admin.db.AdminProductMapper;
import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.admin.reps.AdminApplicationRepository;
import cn.zhijian.passport.admin.reps.AdminFinanceRepository;
import cn.zhijian.passport.admin.reps.AdminPersonRepository;
import cn.zhijian.passport.admin.reps.AdminProductRepository;
import cn.zhijian.passport.admin.reps.AdminPromotionRepository;
import cn.zhijian.passport.admin.reps.AdminResourceRepository;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.BusinessEventMapper;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.SalesPromotionMapper;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.repos.CorporateRepository;
import cn.zhijian.passport.repos.HeadInfoRepository;
import cn.zhijian.passport.repos.ProductRepository;
import cn.zhijian.passport.repos.ResourceRepository;
import cn.zhijian.passport.repos.RoleRepository;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.pay.db.PayMapper;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.trade.db.TradeMapper;
import cn.zhijian.trade.db.VoucherMapper;
import cn.zhijian.trade.reps.TradeRepository;
import io.dropwizard.setup.Environment;

public class RepositoryInitializer implements AxonDropwizardInitializer<BackendConfiguration> {

	@Override
	public void config(Configurer axConfig, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {
		// use prop to share between stages
		objStore.put(new CorporateRepository(objStore.get(SessionStore.class), objStore.get(CorporateMapper.class),
				objStore.get(CorporateStaffMapper.class), objStore.get(TeamDAO.class),
				objStore.get(CorporateRoleMapper.class), objStore.get(PersonDAO.class),
				objStore.get(ApplicationDao.class)));
		objStore.put(new ResourceRepository(objStore.get(SessionStore.class), objStore.get(ResourceDAO.class)));
		objStore.put(new HeadInfoRepository(objStore.get(PersonDAO.class), objStore.get(CorporateMapper.class),
				objStore.get(ResourceDAO.class), objStore.get(CorporateEnums.class)));
		objStore.put(new PayRepository(objStore.get(PayMapper.class), objStore.get(CorporateMapper.class),
				objStore.get(ApplicationDao.class), objStore.get(PersonDAO.class)));
		objStore.put(new AdminPersonRepository(objStore.get(AdminPersonMapper.class)));
		objStore.put(new AdminApplicationRepository(objStore.get(AdminApplicationMapper.class)));
		objStore.put(new AdminProductRepository(objStore.get(AdminProductMapper.class),
				objStore.get(AdminApplicationMapper.class)));
		objStore.put(new AdminResourceRepository(objStore.get(AdminResourceDAO.class)));
		objStore.put(new ProductRepository(objStore.get(ProductMapper.class),objStore.get(AdminResourceDAO.class)));
		objStore.put(new TradeRepository(objStore.get(TradeMapper.class), objStore.get(ApplicationDao.class),
				objStore.get(ApplicationModuleDao.class),objStore.get(ProductMapper.class), objStore.get(ProductRepository.class)));
		objStore.put(new RoleRepository(objStore.get(CorporateMapper.class),objStore.get(CorporateRoleMapper.class),objStore.get(VoucherMapper.class)));
		objStore.put(new AdminPromotionRepository(objStore.get(SalesPromotionMapper.class),objStore.get(BusinessEventMapper.class),
				objStore.get(AdminProductMapper.class)));
		objStore.put(new AdminFinanceRepository(objStore.get(AdminFinanceMapper.class)));

	}

	@Override
	public void started(Configuration configuration, BackendConfiguration dwConfig, Environment environment,
			ObjectStore objStore) {

	}

}
