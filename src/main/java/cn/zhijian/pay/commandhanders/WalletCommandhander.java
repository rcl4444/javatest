package cn.zhijian.pay.commandhanders;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.domain.commandhandlers.CorporateRoleCommandHandler;
import cn.zhijian.pay.api.Bill;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.OrderInfo;
import cn.zhijian.pay.api.Pay.DateType;
import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.commands.ChargeCommand;
import cn.zhijian.pay.commands.CreateWalletCommand;
import cn.zhijian.pay.commands.CreateWxQRCommand;
import cn.zhijian.pay.commands.Expensescommand;
import cn.zhijian.pay.commands.Rechargecommand;
import cn.zhijian.pay.commands.WXnotifyCommand;
import cn.zhijian.pay.db.row.BillRow;
import cn.zhijian.pay.db.row.WalletRow;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.pay.sdk.WXPay;
import cn.zhijian.pay.sdk.WXPayUtil;
import cn.zhijian.trade.api.CreateOrder;
import cn.zhijian.trade.commands.CreateOrderCommand;
import cn.zhijian.trade.commands.CreateVoucherCommand;
import cn.zhijian.trade.db.row.OrderDetailsRow;
import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.reps.TradeRepository;

public class WalletCommandhander {
	private static Logger logger = LoggerFactory.getLogger(CorporateRoleCommandHandler.class);
	final WXPay wxpay;
	final CommandGateway cmdGw;
	final String siteUrl;
	final PayRepository payRepository;
	final SqlSessionManager sqlSessionManager;
	final TradeRepository tradeRepository;
	final ProductMapper productMapper;

	public WalletCommandhander(WXPay wxpay, String siteUrl, CommandGateway cmdGw, PayRepository payRepository,
			SqlSessionManager sqlSessionManager, TradeRepository tradeRepository, ProductMapper productMapper) {
		this.wxpay = wxpay;
		this.siteUrl = siteUrl;
		this.cmdGw = cmdGw;
		this.payRepository = payRepository;
		this.sqlSessionManager = sqlSessionManager;
		this.tradeRepository = tradeRepository;
		this.productMapper = productMapper;
	}

	@CommandHandler
	public Pair<Boolean, String> charge(ChargeCommand cmd) {
		Pair<Boolean, String> result = cmdGw
				.sendAndWait(new CreateOrderCommand(
						new CreateOrder(null, cmd.getCharge().getTradeType(), null, cmd.getCharge().getPayType(),
								cmd.getCharge().getOrderType(), null, null, cmd.getCharge().getPrice(),
								cmd.getCharge().getWalletId()),
						cmd.getUserName(), cmd.getCreatedBy(), cmd.getUserId()));
		return result;
	}

	@CommandHandler
	public Pair<Boolean, OrderInfo> CreateWxQR(CreateWxQRCommand cmd) throws Exception {

		OrderRow row = tradeRepository.findOrderbyOutTradeNo(cmd.getPay().getOutTradeNo());
		if (row == null) {
			return Pair.of(false, new OrderInfo(null, null, null, null, "该订单不存在"));
		}

		BillRow billRow = payRepository.findBillByOrderIdAndBillType(row.getId(), BillType.INCOME);

		return wxQR(row.getBody(), billRow.getBillNo(), row.getTotalFee(), billRow.getTradeType(),
				getIpAddress(cmd.getRequest()));
	}

	public Pair<Boolean, OrderInfo> wxQR(String body, String out_trade_no, double TotalFee, String tradeType,
			String ipAddress) {

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("body", body);
		data.put("out_trade_no", out_trade_no);// 公司内部订单
		data.put("device_info", "");
		data.put("fee_type", "CNY");
		data.put("total_fee", String.valueOf(Math.round(TotalFee * 100)));
		// data.put("spbill_create_ip",getIpAddress(cmd.getRequest()));
		logger.debug("spbill_create_ip " + ipAddress);
		data.put("spbill_create_ip", "203.86.27.56");
		data.put("trade_type", tradeType);
		data.put("product_id", out_trade_no);
		String code_url = null;

		try {

			Map<String, String> r = wxpay.unifiedOrder(data);
			logger.debug("wx parameter :" + r);
			if ("SUCCESS".equals(r.get("return_code"))) {
				code_url = r.get("code_url");
				logger.debug(code_url);

				if (code_url == null) {
					throw new Exception("wxqr failed to obtain");
				}
				return Pair.of(true, new OrderInfo(out_trade_no, String.valueOf(TotalFee), body, code_url, null));
			} else {
				logger.debug("wx message :" + r.get("return_msg"));
				return Pair.of(false, new OrderInfo(null, null, null, null, r.get("return_msg")));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Pair.of(false, new OrderInfo(null, null, null, null, "生成二维码失败"));
		}
	}

	@CommandHandler
	public String createWallet(CreateWalletCommand cmd) throws Exception {
		String id = newId();
		WalletRow row = Convert(id, 0);
		if (payRepository.CreateWallet(row) < 0) {
			logger.debug("create Wallet fail");
			throw new Exception("create Wallet fail");
		}
		return id;
	}

	@CommandHandler
	public void WXnotify(WXnotifyCommand cmd) throws Exception {
		logger.debug("enter WXnotify");
		Map<String, String> m = Request(cmd.getRequest());
		try {
			if (WXPayUtil.isSignatureValid(m, "zzsdnbhdnbyyqljlzzsdnbhdnbyyqljl")) {
				if ("SUCCESS".equals(m.get("result_code"))) {
					logger.debug("wx call :" + m.get("out_trade_no"));

					logger.debug("enter WXnotify order");

					BillRow billRow = payRepository.findBillByBillNoAndBillType(m.get("out_trade_no"), BillType.INCOME);
					if (billRow == null) {
						String resXml = setWxXml("FAIL", "支付流水号不存在");
						Response(resXml, cmd.getResponse());
					}

					OrderRow row = tradeRepository.findOrderbyId(billRow.getId());

					if (row == null) {
						String resXml = setWxXml("FAIL", "订单号不存在");
						Response(resXml, cmd.getResponse());
					}

					if (row.getIsPayed() == 0) {
						try (SqlSession session = sqlSessionManager.openSession()) {
							try {
								if (row.getOrderType() == OrderType.PAYMENT) {
									List<OrderDetailsRow> orderDetails = tradeRepository
											.findOrderDetailsByOrderId(row.getId());
									for (OrderDetailsRow orderDetailsRow : orderDetails) {
										cmdGw.sendAndWait(new CreateVoucherCommand(row, orderDetailsRow.getUseNum(),
												orderDetailsRow.getUsePeriod(), orderDetailsRow.getProductId()));
									}
								}
								// 更新支付状态
								payRepository.UpdateIsPay(row.getOutTradeNo(), 1);
								// 更新流水账时间
								payRepository.updateBillUpdatedAt(row.getId());
								session.commit();
							} catch (Exception e) {
								// TODO: handle exception
								session.rollback();
								logger.debug(e.getMessage());
							}
						}
					}

					String resXml = setWxXml("SUCCESS", "OK");
					Response(resXml, cmd.getResponse());
				} else {
					logger.debug("wx error code des : {}", m.get("err_code"));
					String resXml = setWxXml("FAIL", "");
					Response(resXml, cmd.getResponse());
				}
			} else {
				logger.debug("sign verification failed : {}" + m.get("sign"));
				String resXml = setWxXml("FAIL", "签名不正确");
				Response(resXml, cmd.getResponse());
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("WXnotify" + e.getMessage());
		}
	}

	@CommandHandler
	public void expenses(Expensescommand cmd) {
		payRepository.insertBill(newOutTradeNo("ybgxf"), BillType.EXPEND, cmd.getBill());
	}

	@CommandHandler
	public void recharge(Rechargecommand cmd) {
		payRepository.insertBill(newOutTradeNo("ybgcz"), BillType.INCOME, cmd.getBill());
	}

	private String newId() {
		return UUID.randomUUID().toString();
	}

	private WalletRow Convert(String id, double balance) {
		return new WalletRow(id, balance, null, null, null, null);
	}

	private String setWxXml(String return_code, String return_msg) {
		return "<xml><return_code><![CDATA[" + return_code + "]]>" + "</return_code><return_msg><![CDATA[" + return_msg
				+ "]]></return_msg></xml>";
	}

	private Map<String, String> Request(HttpServletRequest request) throws Exception {

		InputStream inputStream;
		StringBuffer sb = new StringBuffer();
		inputStream = request.getInputStream();
		String s;
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		while ((s = in.readLine()) != null) {
			sb.append(s);
		}
		in.close();
		inputStream.close();

		// 解析xml成map
		Map<String, String> m = new HashMap<String, String>();
		m = WXPayUtil.xmlToMap(sb.toString());
		logger.debug("wx xml:" + m);
		return m;
	}

	private String newOutTradeNo(String flag) {
		int random = (int) (Math.random() * 1000000);
		if (random < 1000000) {
			random = random + 1000000000;
		}
		// 17+10+5
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + random + flag;
	}

	private void Response(String resXml, HttpServletResponse response) throws Exception {

		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(resXml.getBytes());
		out.flush();
		out.close();
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

	/**
	 * 获取ip
	 */
	public String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	private String joinStr(OrderRow row) {

		List<OrderDetailsRow> OrderDetails = tradeRepository.findOrderDetailsByOrderId(row.getId());
		String str = "";
		int i = 0;
		for (OrderDetailsRow orderDetailsRow : OrderDetails) {
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
				str = "产品" + productName + "开通" + orderDetailsRow.getUseNum() + date;
			} else {
				str += "," + productName + "开通" + orderDetailsRow.getUseNum() + date;
			}
		}
		return str;
	}
}
