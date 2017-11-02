package cn.zhijian.passport.init;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.simplejavamail.mailer.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.BackendConfiguration;
import cn.zhijian.passport.admin.commandhandlers.AdminApplicationCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminCorporateCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminFinanceCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminLoginCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminProductCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminResourceCommandHandler;
import cn.zhijian.passport.admin.commandhandlers.AdminSalePromotionCommandHandler;
import cn.zhijian.passport.admin.db.AdminCorporateMapper;
import cn.zhijian.passport.admin.db.AdminPersonMapper;
import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.admin.reps.AdminApplicationRepository;
import cn.zhijian.passport.admin.reps.AdminFinanceRepository;
import cn.zhijian.passport.admin.reps.AdminPersonRepository;
import cn.zhijian.passport.admin.reps.AdminProductRepository;
import cn.zhijian.passport.admin.reps.AdminPromotionRepository;
import cn.zhijian.passport.admin.reps.AdminResourceRepository;
import cn.zhijian.passport.admin.resource.AdminAccountResource;
import cn.zhijian.passport.admin.resource.AdminApplicationResource;
import cn.zhijian.passport.admin.resource.AdminCorporateResource;
import cn.zhijian.passport.admin.resource.AdminFinanceResource;
import cn.zhijian.passport.admin.resource.AdminPersonResource;
import cn.zhijian.passport.admin.resource.AdminProductResource;
import cn.zhijian.passport.admin.resource.AdminPromotionResource;
import cn.zhijian.passport.admin.resource.AdminResourceResource;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.bundles.AxonDropwizardInitializer;
import cn.zhijian.passport.bundles.ObjectStore;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.CertificateDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.InvitationDAO;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.PingMapper;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.SalesPromotionMapper;
import cn.zhijian.passport.db.SendBindingEmailMapper;
import cn.zhijian.passport.db.TeamDAO;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.BusinessEventRow.EventStatus;
import cn.zhijian.passport.db.row.GiftProductView;
import cn.zhijian.passport.db.row.ModuleOperationRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import cn.zhijian.passport.domain.commandhandlers.ApplicationCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.CorporateCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.CorporateRoleCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.EmailCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.LoginContextCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.MessageCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.OAuthCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.PersonCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.ResourceCommandHandler;
import cn.zhijian.passport.domain.commandhandlers.SMSCommandHandler;
import cn.zhijian.passport.domain.eventhandlers.CorporateEventHandler;
import cn.zhijian.passport.domain.eventhandlers.PersonEventHandler;
import cn.zhijian.passport.domain.eventhandlers.SessionEventHandler;
import cn.zhijian.passport.domain.events.PushEvent;
import cn.zhijian.passport.domain.services.MessagingService;
import cn.zhijian.passport.repos.CorporateRepository;
import cn.zhijian.passport.repos.HeadInfoRepository;
import cn.zhijian.passport.repos.ProductRepository;
import cn.zhijian.passport.repos.ResourceRepository;
import cn.zhijian.passport.repos.RoleRepository;
import cn.zhijian.passport.resources.CorporateResource;
import cn.zhijian.passport.resources.FoundationResource;
import cn.zhijian.passport.resources.LoginContextResource;
import cn.zhijian.passport.resources.MessageResource;
import cn.zhijian.passport.resources.Oauth2Resource;
import cn.zhijian.passport.resources.PersonResource;
import cn.zhijian.passport.resources.PingResource;
import cn.zhijian.passport.resources.ProductResource;
import cn.zhijian.passport.resources.ResourceResource;
import cn.zhijian.passport.resources.RoleResource;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.template.TemplateService;
import cn.zhijian.pay.api.Pay.DateType;
import cn.zhijian.pay.commandhanders.WalletCommandhander;
import cn.zhijian.pay.db.PayMapper;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.pay.resources.PayResource;
import cn.zhijian.pay.sdk.WXPay;
import cn.zhijian.trade.commandhanders.TradeCommandhander;
import cn.zhijian.trade.db.TradeMapper;
import cn.zhijian.trade.db.VoucherMapper;
import cn.zhijian.trade.db.row.SnapshotApplicationModuleRow;
import cn.zhijian.trade.db.row.SnapshotApplicationRow;
import cn.zhijian.trade.db.row.SnapshotModuleOperationRow;
import cn.zhijian.trade.db.row.SnapshotRow;
import cn.zhijian.trade.db.row.VoucherRow;
import cn.zhijian.trade.reps.TradeRepository;
import cn.zhijian.trade.resources.CartResource;
import cn.zhijian.trade.resources.OrderResource;
import cn.zhijian.trade.resources.TradeResource;
import cn.zhijian.trade.resources.VoucherResource;
import io.dropwizard.setup.Environment;

public class AppDomainInitializer implements AxonDropwizardInitializer<BackendConfiguration> {

	protected static Logger logger = LoggerFactory.getLogger(AppDomainInitializer.class);

	@Override
	public void config(Configurer ax, BackendConfiguration cfg, Environment environment, ObjectStore os) {

		final String siteUrl = cfg.getSite().getSiteUrl();
		logger.info("Site URL: " + siteUrl);

		// template service
		TemplateService temps = new TemplateService();
		os.put(temps);

		// aggregates: ax.configureAggregate(WorkflowAggregate.class);

		// command handlers
		ax.registerCommandHandler((c) -> {
			// email handling, register messaging service here
			MessagingService msgs = new MessagingService(c.commandGateway(), temps);
			os.put(msgs);
			return new EmailCommandHandler(os.get(Mailer.class), cfg.getSmtp().getSender());
		});

		ax.registerCommandHandler((c) -> new LoginContextCommandHandler(os.get(SessionStore.class),
				os.get(PersonDAO.class), os.get(CorporateMapper.class), os.get(TeamDAO.class),
				os.get(CorporateRoleMapper.class), os.get(CorporateStaffMapper.class), os.get(CorporateEnums.class)));
		ax.registerCommandHandler((c) -> new PersonCommandHandler(os.get(SessionStore.class), os.get(PersonDAO.class),
				os.get(InvitationDAO.class), c.commandGateway(), temps, siteUrl, os.get(SendBindingEmailMapper.class),
				c.eventBus(), os.get(SqlSessionManager.class), os.get(CorporateStaffMapper.class),
				os.get(CorporateMapper.class), os.get(CorporateEnums.class)));

		ax.registerCommandHandler((c) -> {
			return new CorporateCommandHandler(os.get(SessionStore.class), c.commandGateway(),
					os.get(CorporateMapper.class), os.get(CorporateStaffMapper.class), os.get(InvitationDAO.class),
					os.get(PersonDAO.class), os.get(TeamDAO.class), siteUrl, os.get(SqlSessionManager.class),
					c.eventBus(), os.get(AdminCorporateMapper.class), os.get(CorporateEnums.class));
		});

		ax.registerCommandHandler(
				(c) -> new ResourceCommandHandler(os.get(SessionStore.class), os.get(ResourceDAO.class)));

		ax.registerCommandHandler(
				(c) -> new OAuthCommandHandler(os.get(ApplicationDao.class), os.get(CertificateDao.class),
						os.get(CorporateMapper.class), os.get(PersonDAO.class), os.get(CorporateEnums.class)));

		ax.registerCommandHandler((c) -> new SMSCommandHandler());

		ax.registerCommandHandler((c) -> new CorporateRoleCommandHandler(os.get(CorporateMapper.class),
				os.get(CorporateRoleMapper.class), os.get(SqlSessionManager.class),os.get(VoucherMapper.class)));

		ax.registerCommandHandler((c) -> new WalletCommandhander(os.get(WXPay.class), siteUrl, c.commandGateway(),
				os.get(PayRepository.class), os.get(SqlSessionManager.class), os.get(TradeRepository.class),
				os.get(ProductMapper.class)));

		ax.registerCommandHandler((c) -> new AdminLoginCommandHandler(os.get(AdminPersonMapper.class)));
		ax.registerCommandHandler((c) -> new AdminCorporateCommandHandler(c.commandGateway(),
				os.get(SessionStore.class), os.get(AdminCorporateMapper.class), os.get(PayMapper.class),
				os.get(SqlSessionManager.class), c.eventBus()));
		ax.registerCommandHandler(c -> new MessageCommandHandler(os.get(MessageMapper.class)));
		ax.registerCommandHandler(c -> new ApplicationCommandHandler(os.get(SqlSessionManager.class)));
		ax.registerCommandHandler(c -> new AdminApplicationCommandHandler(os.get(SqlSessionManager.class)));
		ax.registerCommandHandler(c -> new TradeCommandhander(os.get(SqlSessionManager.class),
				os.get(TradeRepository.class), os.get(PayRepository.class), os.get(ApplicationDao.class),
				os.get(ApplicationModuleDao.class), c.commandGateway(), os.get(ProductMapper.class)));
		ax.registerCommandHandler(c -> new AdminProductCommandHandler(os.get(SqlSessionManager.class)));
		ax.registerCommandHandler(c -> new AdminResourceCommandHandler(os.get(AdminResourceDAO.class)));
		ax.registerCommandHandler(c->new AdminSalePromotionCommandHandler(os.get(SqlSessionManager.class)));
		ax.registerCommandHandler(c -> new AdminFinanceCommandHandler(c.commandGateway(), os.get(AdminFinanceRepository.class)));

		// event handlers
		EventHandlingConfiguration eventHandlers = new EventHandlingConfiguration();
		eventHandlers.registerEventHandler(
				conf -> new PersonEventHandler(os.get(MessageMapper.class), os.get(SqlSessionManager.class)));
		eventHandlers.registerEventHandler(
				conf -> new CorporateEventHandler(os.get(MessageMapper.class), os.get(SqlSessionManager.class)));
		eventHandlers.registerEventHandler(conf -> new SessionEventHandler(os.get(SqlSessionManager.class)));
		eventHandlers.registerHandlerInterceptor((c,str)->
		{
			if(!str.equals("cn.zhijian.passport.domain.eventhandlers")){
				return null;
			}
			return new GiftInterceptor(os.get(SqlSessionManager.class));
		});
		ax.registerModule(eventHandlers);
	}

	@Override
	public void started(Configuration ax, BackendConfiguration cfg, Environment env, ObjectStore prop) {

		env.jersey().register(new LoginContextResource(ax.commandGateway(), prop.get(SessionStore.class)));
		env.jersey().register(
				new PersonResource(prop.get(SessionStore.class), ax.commandGateway(), prop.get(PersonDAO.class)));
		env.jersey().register(new CorporateResource(prop.get(SessionStore.class), ax.commandGateway(), //
				prop.get(CorporateRepository.class), prop.get(PersonDAO.class), prop.get(CorporateRoleMapper.class),
				prop.get(PayMapper.class)));
		env.jersey().register(new ResourceResource(prop.get(SessionStore.class), //
				ax.commandGateway(), //
				prop.get(ResourceRepository.class)));
		env.jersey()
				.register(new Oauth2Resource(prop.get(SessionStore.class), ax.commandGateway(),
						prop.get(ApplicationDao.class), prop.get(CorporateMapper.class), cfg.getSite().getSiteUrl(),
						prop.get(CorporateRoleMapper.class)));
		env.jersey().register(new FoundationResource(prop.get(SessionStore.class), prop.get(HeadInfoRepository.class),
				prop.get(CorporateMapper.class)));
		env.jersey().register(new PayResource(ax.commandGateway(), prop.get(SessionStore.class),
				prop.get(PayRepository.class), prop.get(ApplicationDao.class), prop.get(TradeRepository.class)));
		env.jersey().register(new AdminAccountResource(cfg.getSite(), ax.commandGateway()));
		env.jersey().register(new AdminCorporateResource(ax.commandGateway(), prop.get(CorporateRepository.class),
				prop.get(CorporateMapper.class), prop.get(AdminCorporateMapper.class), prop.get(PayRepository.class)));
		env.jersey().register(
				new MessageResource(ax.commandGateway(), prop.get(SessionStore.class), prop.get(MessageMapper.class)));
		env.jersey().register(
				new AdminPersonResource(prop.get(AdminPersonRepository.class), prop.get(PayRepository.class)));
		env.jersey().register(
				new AdminApplicationResource(ax.commandGateway(), prop.get(AdminApplicationRepository.class)));
		env.jersey().register(new CartResource());
		env.jersey().register(new VoucherResource(prop.get(SessionStore.class), prop.get(TradeRepository.class)));
		env.jersey().register(new OrderResource(ax.commandGateway(), prop.get(SessionStore.class),
				prop.get(PayRepository.class), prop.get(TradeRepository.class)));
		env.jersey().register(new TradeResource(prop.get(SessionStore.class), ax.commandGateway(),
				prop.get(TradeRepository.class), prop.get(PayRepository.class)));
		env.jersey().register(new AdminProductResource(ax.commandGateway(), prop.get(AdminProductRepository.class)));
		env.jersey().register(new AdminResourceResource(ax.commandGateway(), prop.get(AdminResourceRepository.class)));
		env.jersey().register(new ProductResource(prop.get(ProductRepository.class)));
		env.jersey().register(new RoleResource(prop.get(SessionStore.class), ax.commandGateway(),
				prop.get(RoleRepository.class), prop.get(CorporateRoleMapper.class)));
		env.jersey().register(new AdminPromotionResource(prop.get(AdminPromotionRepository.class),ax.commandGateway()));
		env.jersey().register(new AdminFinanceResource(ax.commandGateway(),prop.get(AdminFinanceRepository.class)));
		// mybatis mapper test
		env.jersey().register(new PingResource(prop.get(PingMapper.class)));

	}
	
	public static class GiftInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T>
	{
		final SqlSessionManager manager;
		
		public GiftInterceptor(SqlSessionManager manager){
			this.manager = manager;
		}

		@Override
		public Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain) throws Exception {
			if(PushEvent.class.isAssignableFrom(unitOfWork.getMessage().getPayload().getClass())){
				try (SqlSession session = this.manager.openSession()) {
					try {
						PushEvent message = (PushEvent)unitOfWork.getMessage().getPayload();
						Date currDate = new Date();
						SalesPromotionMapper spMapper = session.getMapper(SalesPromotionMapper.class);
						TradeMapper tradeMapper = session.getMapper(TradeMapper.class);
						ApplicationDao applicationDao = session.getMapper(ApplicationDao.class);
						ApplicationModuleDao applicationModuleDao = session.getMapper(ApplicationModuleDao.class);
						List<GiftProductView> products = spMapper.getPromotionGift(EventStatus.Start, SalesPromotionStatus.Start, message.getUniqueSign());
						if(products.size() > 0){
							long snapshotId = tradeMapper.insertSnapshot(new SnapshotRow(null, "system", currDate, null, null));
							for(GiftProductView p : products){
								List<ApplicationRow> applications = applicationDao.findApplicationsByProductId(p.getProductid());
								for (ApplicationRow applicationRow : applications) {
									tradeMapper.insertSnapshotApplication(new SnapshotApplicationRow(applicationRow.getId(), snapshotId));
									List<ApplicationModuleRow> ApplicationModules = applicationModuleDao
											.findByApplicationModuleInApplicationModuleid(applicationRow.getId());
									for (ApplicationModuleRow applicationModuleRow : ApplicationModules) {
										tradeMapper.insertsnapshotApplicationModule(
												new SnapshotApplicationModuleRow(applicationModuleRow.getId(), applicationRow.getId(),
														snapshotId, applicationModuleRow.getModulename()));
										List<ModuleOperationRow> moduleOperations = applicationModuleDao
												.findOperationByAppid(applicationRow.getId());
										for (ModuleOperationRow moduleOperationRow : moduleOperations) {
											tradeMapper.insertSnapshotModuleOperation(new SnapshotModuleOperationRow(
													moduleOperationRow.getId(), applicationRow.getId(), applicationModuleRow.getId(),
													snapshotId, moduleOperationRow.getOperationname()));
										}
									}
								}
								VoucherRow row;
								if(p.getDuration() == null){
									row = new VoucherRow(null, getFixLenthString(12), null, snapshotId,
											p.getPersonnum(), p.getDuration().toString(), currDate, null, message.getWalletId(),
											"system", currDate, null, null, p.getProductid());
								}
								else{
									int day = converDate(p.getDuration(), DateType.MONTH);
									row = new VoucherRow(null, getFixLenthString(12), null, snapshotId,
											p.getPersonnum(), p.getDuration().toString(), currDate, getNextDay(currDate, day), message.getWalletId(),
											"system", currDate, null, null, p.getProductid());	
								}
								tradeMapper.insertVoucher(row);
							}
						}
						session.commit();
					} 
					catch (Exception e) {
						session.rollback();
						e.printStackTrace();
					}
				}
			}
			return interceptorChain.proceed();
		}
		
		private int converDate(int useDay, DateType dateType) {
			if (dateType == DateType.DAY) {
				return useDay;
			}
			if (dateType == DateType.MONTH) {
				return useDay * 31;
			}
			if (dateType == DateType.YEAR) {
				return useDay * 31 * 12;
			}
			return 0;
		}
		
		private String getFixLenthString(int strLength) {

			Random rm = new Random();
			double pross = (1 + rm.nextDouble()) * Math.pow(14, strLength);
			String fixLenthString = String.valueOf(pross);
			return fixLenthString.substring(2, strLength + 1);
		}

		private Date getNextDay(Date date, int day) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, +day);
			date = calendar.getTime();
			return date;
		}
	}
}
