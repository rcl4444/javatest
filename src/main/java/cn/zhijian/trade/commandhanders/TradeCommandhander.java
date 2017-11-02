package cn.zhijian.trade.commandhanders;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;
import cn.zhijian.passport.domain.commandhandlers.CorporateRoleCommandHandler;
import cn.zhijian.pay.api.Bill;
import cn.zhijian.pay.api.Pay.DateType;
import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.api.Pay.PayType;
import cn.zhijian.pay.commands.Expensescommand;
import cn.zhijian.pay.commands.Rechargecommand;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.trade.api.CreateOrder;
import cn.zhijian.trade.api.Trade.Products;
import cn.zhijian.trade.commands.CreateOrderCommand;
import cn.zhijian.trade.commands.CreateTradeCommand;
import cn.zhijian.trade.commands.CreateVoucherCommand;
import cn.zhijian.trade.commands.DeleteOrderCommand;
import cn.zhijian.trade.commands.JoinCartCommand;
import cn.zhijian.trade.db.row.OrderDetailsRow;
import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.db.row.SnapshotApplicationModuleRow;
import cn.zhijian.trade.db.row.SnapshotApplicationRow;
import cn.zhijian.trade.db.row.SnapshotModuleOperationRow;
import cn.zhijian.trade.db.row.SnapshotRow;
import cn.zhijian.trade.db.row.TradeDetailsRow;
import cn.zhijian.trade.db.row.TradeRow;
import cn.zhijian.trade.db.row.VoucherRow;
import cn.zhijian.trade.reps.TradeRepository;

public class TradeCommandhander {
	private static Logger logger = LoggerFactory.getLogger(CorporateRoleCommandHandler.class);
	private SqlSessionManager sqlSessionManager;
	private TradeRepository tradeRepository;
	private PayRepository payRepository;
	private ApplicationDao applicationDao;
	private ApplicationModuleDao applicationModuleDao;
	private CommandGateway cmdGw;
	private ProductMapper productMapper;

	public TradeCommandhander(SqlSessionManager sqlSessionManager, TradeRepository tradeRepository,
			PayRepository payRepository, ApplicationDao applicationDao, ApplicationModuleDao applicationModuleDao,
			CommandGateway cmdGw, ProductMapper productMapper) {
		this.sqlSessionManager = sqlSessionManager;
		this.tradeRepository = tradeRepository;
		this.payRepository = payRepository;
		this.applicationDao = applicationDao;
		this.applicationModuleDao = applicationModuleDao;
		this.cmdGw = cmdGw;
		this.productMapper = productMapper;
	}

	@CommandHandler
	public Pair<Boolean, String> CreateOrder(CreateOrderCommand cmd) {

		OrderRow row = null;
		double total = 0.0;
		double balance = 0.0;
		String outTradeNo = newOutTradeNo();

		
		FinanceAccountRow financeAccountRow = tradeRepository.findFinanceAccount(cmd.getCreateOrder().getPayType().toString());


		if (cmd.getCreateOrder().getOrderType() == OrderType.RECHARGE) {
			try (SqlSession session = sqlSessionManager.openSession()) {
				try {
					row = new OrderRow(null, outTradeNo, "充值" + cmd.getCreateOrder().getMoney(),
							cmd.getCreateOrder().getPayType(), cmd.getCreateOrder().getMoney(),
							cmd.getCreateOrder().getWalletId(), 0, cmd.getCreateOrder().getOrderType(), null,
							cmd.getCreatedBy(), new Date(), null, null, null, null, 0);
					tradeRepository.CreateOrder(row);
					// 应收
					cmdGw.sendAndWait(new Rechargecommand(new Bill(row.getId(), cmd.getCreateOrder().getMoney(),
							cmd.getUserId(), financeAccountRow.getId(), cmd.getCreateOrder().getTradeType(), cmd.getCreateOrder().getPayType(),
							row.getCreatedBy(), new Date(), row.getWalletId(), cmd.getUserName())));
					session.commit();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					session.rollback();
					return Pair.of(false, "操作失败");
				}
			}
		} 
		else {
			TradeRow tradeRow = tradeRepository.findTradeById(cmd.getCreateOrder().getTradeId());
			if (tradeRow == null) {
				return Pair.of(false, "Trade ID not find");
			}
			List<TradeDetailsRow> tradeDetailsRows = tradeRepository.findTradeDetailsByTradeId(tradeRow.getId());

			row = convertOrderRow(outTradeNo, cmd.getCreateOrder().getBody(), cmd.getCreateOrder(), tradeRow);

			row.setCreatedAt(new Date());
			row.setCreatedBy(cmd.getCreatedBy());

			total = tradeDetailsRows.stream().map(o -> o.getPrice()).reduce(0.0, (a, b) -> add(a, b));
			balance = payRepository.GetBalance(tradeRow.getWalletId());

			if (cmd.getCreateOrder().getPayType() == PayType.BALANCE) {
				if (total > balance) {
					return Pair.of(false, "余额不足");
				}
				row.setIsPayed(1);
				try (SqlSession session = sqlSessionManager.openSession()) {
					try {
						row.setTotalFee(total);

						// 创建订单记录
						long cid = tradeRepository.CreateOrder(row);

						row.setId(cid);
						for (TradeDetailsRow tradeDetailsRow : tradeDetailsRows) {
							tradeRepository.CreateOrderDetails(new OrderDetailsRow(null, cid,
									tradeDetailsRow.getProductId(), tradeDetailsRow.getUseNum(),
									tradeDetailsRow.getUsePeriod(), tradeDetailsRow.getPrice(),
									tradeDetailsRow.getApplicationcost(), tradeDetailsRow.getPersoncost()));
							cmdGw.sendAndWait(new CreateVoucherCommand(row, tradeDetailsRow.getUseNum(),
									tradeDetailsRow.getUsePeriod(), tradeDetailsRow.getProductId()));
						}

						// 应付
						cmdGw.sendAndWait(new Expensescommand(
								new Bill(cid, -total, cmd.getUserId(), null, "", cmd.getCreateOrder().getPayType(),
										cmd.getCreatedBy(), new Date(), tradeRow.getWalletId(), cmd.getUserName())));
						session.commit();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						session.rollback();
						return Pair.of(false, "操作失败");
					}
				}
			} else {
				row.setTotalFee(total);
				// 创建订单记录
				long cid = tradeRepository.CreateOrder(row);

				for (TradeDetailsRow tradeDetailsRow : tradeDetailsRows) {
					tradeRepository.CreateOrderDetails(new OrderDetailsRow(null, cid, tradeDetailsRow.getProductId(),
							tradeDetailsRow.getUseNum(), tradeDetailsRow.getUsePeriod(), tradeDetailsRow.getPrice(),
							tradeDetailsRow.getApplicationcost(), tradeDetailsRow.getPersoncost()));
				}

				// 应付
				cmdGw.sendAndWait(new Expensescommand(
						new Bill(cid, -total, cmd.getUserId(), null, null, cmd.getCreateOrder().getPayType(),
								cmd.getCreatedBy(), new Date(), tradeRow.getWalletId(), cmd.getUserName())));
				// 应收
				cmdGw.sendAndWait(new Rechargecommand(new Bill(row.getId(), total, cmd.getUserId(), financeAccountRow.getId(),
						cmd.getCreateOrder().getTradeType(), cmd.getCreateOrder().getPayType(), row.getCreatedBy(),
						new Date(), tradeRow.getWalletId(), cmd.getUserName())));
			}
		}

		return Pair.of(true, outTradeNo);
	}

	@CommandHandler
	public Pair<Boolean, String> JoinCart(JoinCartCommand cmd) {
		return null;
	}

	@CommandHandler
	public Pair<Boolean, String> CreateTrade(CreateTradeCommand cmd) {
		String tradeId = UUID.randomUUID().toString();

		List<Products> products = cmd.getTrade().getProducts();

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				if (tradeRepository.insertTrade(new TradeRow(tradeId, cmd.getWalletId(),
						cmd.getTrade().getBehaviorType(), cmd.getCreatedBy(), new Date(), null, null)) > 0) {
					for (Products productsView : products) {

						tradeRepository.insertTradeDetails(convert(tradeId, productsView));
					}
					return Pair.of(true, tradeId);
				}
				session.commit();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				logger.debug("CreateTrade Command error: ", e.getMessage());
				session.rollback();
			}
		}
		return Pair.of(false, "生成结算ID失败");
	}

	@CommandHandler
	public int createVoucher(CreateVoucherCommand cmd) {

		Date t = new Date();

		int day = converDate(Integer.parseInt(cmd.getUsePeriod()), DateType.MONTH);

		// 生成快照id
		try (SqlSession session = sqlSessionManager.openSession()) {

			try {
				long snapshotId = tradeRepository
						.insertSnapshot(new SnapshotRow(null, cmd.getOrder().getCreatedBy(), t, null, null));

				List<ApplicationRow> applications = applicationDao.findApplicationsByProductId(cmd.getProductId());
				// 循环所有产品下的应用
				for (ApplicationRow applicationRow : applications) {
					tradeRepository
							.insertSnapshotApplication(new SnapshotApplicationRow(applicationRow.getId(), snapshotId));
					List<ApplicationModuleRow> ApplicationModules = applicationModuleDao
							.findByApplicationModuleInApplicationModuleid(applicationRow.getId());
					// 循环所有应用下的模块
					for (ApplicationModuleRow applicationModuleRow : ApplicationModules) {
						tradeRepository.insertsnapshotApplicationModule(
								new SnapshotApplicationModuleRow(applicationModuleRow.getId(), applicationRow.getId(),
										snapshotId, applicationModuleRow.getModulename()));
						List<ModuleOperationRow> moduleOperations = applicationModuleDao
								.findOperationByAppid(applicationRow.getId());
						// 循环所有模块下的操作
						for (ModuleOperationRow moduleOperationRow : moduleOperations) {
							tradeRepository.insertSnapshotModuleOperation(new SnapshotModuleOperationRow(
									moduleOperationRow.getId(), applicationRow.getId(), applicationModuleRow.getId(),
									snapshotId, moduleOperationRow.getOperationname()));
						}
					}
				}

				VoucherRow row = new VoucherRow(null, getFixLenthString(12), cmd.getOrder().getId(), snapshotId,
						cmd.getUseNum(), cmd.getUsePeriod(), t, getNextDay(t, day), cmd.getOrder().getWalletId(),
						cmd.getOrder().getCreatedBy(), t, null, null, cmd.getProductId());
				// 生成凭证
				tradeRepository.insertVoucher(row);
				session.commit();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				session.rollback();
				return 0;
			}
			return 1;
		}
	}

	@CommandHandler
	public Pair<Boolean, String> DeleteOrder(DeleteOrderCommand cmd) {
		if (tradeRepository.deleteOrder(cmd.getOrderId()) > 0) {
			return Pair.of(true, "删除成功");
		}
		return Pair.of(false, "删除失败");
	}

	private TradeDetailsRow convert(String tradeId, Products productsView) {
		return new TradeDetailsRow(null, tradeId, productsView.getProductId(), productsView.getUseNum(),
				productsView.getUsePeriod(), productsView.getPrice(), productsView.getProductPrice(),
				productsView.getPeoplePrice());
	}

	private OrderRow convertOrderRow(String outTradeNo, String body, CreateOrder createOrder, TradeRow tradeRow) {
		return new OrderRow(null, outTradeNo, body, createOrder.getPayType(), 0.0, tradeRow.getWalletId(), 0,
				createOrder.getOrderType(), createOrder.getDateType(), null, null, null, null,
				createOrder.getServiceType(), tradeRow.getBehaviorType(), 0);
	}

	private String newOutTradeNo() {
		int random = (int) (Math.random() * 1000000);
		if (random < 1000000) {
			random = random + 1000000000;
		}
		// 17+10+5
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + random + "ybgdd";
	}

	/**
	 * 两个Double数相加
	 * 
	 * @param v1
	 * @param v2
	 * @return Double
	 */
	public Double add(Double v1, Double v2) {
		BigDecimal b1 = new BigDecimal(v1.toString());
		BigDecimal b2 = new BigDecimal(v2.toString());
		return b1.add(b2).doubleValue();
	}

	/**
	 * 两个Double数相减
	 * 
	 * @param v1
	 * @param v2
	 * @return Double
	 */
	public Double sub(Double v1, Double v2) {
		BigDecimal b1 = new BigDecimal(v1.toString());
		BigDecimal b2 = new BigDecimal(v2.toString());
		return b1.subtract(b2).doubleValue();
	}

	private String joinStr(OrderRow row, List<TradeDetailsRow> tradeDetailsRow) {

		String str = "";
		int i = 0;
		for (TradeDetailsRow orderDetailsRow : tradeDetailsRow) {
			String productName = productMapper.load(orderDetailsRow.getProductId()).getProductname();
			String date = "";
			if (row.getDateType() == DateType.DAY) {
				date = "日";
			}
			if (row.getDateType() == DateType.MONTH) {
				date = "月";
			}
			if (row.getDateType() == DateType.YEAR) {
				date = "年";
			}
			if (i == 0) {
				str = "购买" + productName + "开通" + orderDetailsRow.getUseNum() + date;
			} else {
				str += "," + productName + "开通" + orderDetailsRow.getUseNum() + date;
			}
		}
		return str;
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

	/*
	 * 返回长度为【strLength】的随机数，在前面补0
	 */
	private String getFixLenthString(int strLength) {

		Random rm = new Random();

		// 获得随机数
		double pross = (1 + rm.nextDouble()) * Math.pow(14, strLength);

		// 将获得的获得随机数转化为字符串
		String fixLenthString = String.valueOf(pross);

		// 返回固定的长度的随机数
		return fixLenthString.substring(2, strLength + 1);
	}

	private Date getNextDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, +day);// +1今天的时间加一天
		date = calendar.getTime();
		return date;
	}
}
