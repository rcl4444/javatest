package cn.zhijian.trade.reps;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.ApplicationModuleDao;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.repos.ProductRepository;
import cn.zhijian.trade.api.Order;
import cn.zhijian.trade.db.TradeMapper;
import cn.zhijian.trade.db.row.OrderDetailsRow;
import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.db.row.SnapshotApplicationModuleRow;
import cn.zhijian.trade.db.row.SnapshotApplicationRow;
import cn.zhijian.trade.db.row.SnapshotModuleOperationRow;
import cn.zhijian.trade.db.row.SnapshotRow;
import cn.zhijian.trade.db.row.TradeDetailsRow;
import cn.zhijian.trade.db.row.TradeRow;
import cn.zhijian.trade.db.row.VoucherRow;
import cn.zhijian.trade.dto.OrderDto;
import cn.zhijian.trade.dto.ProductsDto;
import cn.zhijian.trade.dto.VoucheDetailsDto;
import cn.zhijian.trade.dto.VoucheDto;
import cn.zhijian.trade.dto.VoucheModuleDto;
import cn.zhijian.trade.dto.VoucherListDto;

public class TradeRepository {

	final TradeMapper tradeMapper;
	final ApplicationDao applicationDao;
	final ApplicationModuleDao applicationModuleDao;
	final ProductMapper productMapper;
    final ProductRepository productRepository;

	public TradeRepository(TradeMapper tradeMapper, ApplicationDao applicationDao,
			ApplicationModuleDao applicationModuleDao, ProductMapper productMapper,ProductRepository productRepository) {
		this.tradeMapper = tradeMapper;
		this.applicationDao = applicationDao;
		this.applicationModuleDao = applicationModuleDao;
		this.productMapper = productMapper;
		this.productRepository =productRepository;
	}

	public PagedResult<Map<String, Object>> loadOrderList(Order order, String walletId, Integer isPayed)
			throws ParseException {
		int offset = (order.getPageNo() - 1) * order.getPageSize();

		int totalRows = tradeMapper.countbyOrder(isPayed, walletId,order.getOutTradeNo(), ConverDate(order.getSTime()),
				ConverDate(order.getETime()));
		int s = order.getPageSize() == null ? totalRows : order.getPageSize();

		List<OrderRow> Orders = tradeMapper.loadByOrder(isPayed, walletId, order.getOutTradeNo(),offset, s, ConverDate(order.getSTime()),
				ConverDate(order.getETime()));
		List<Map<String, Object>> orderDtos = new ArrayList<>();
		for (OrderRow row : Orders) {
			List<OrderDetailsRow> orderDetailsRow = tradeMapper.findOrderDetailsByOrderId(row.getId());
			List<ProductRow> products = this.productMapper
					.findByIds(orderDetailsRow.stream().map(o -> o.getProductId()).collect(Collectors.toList()));
			double total = orderDetailsRow.stream().map(o -> o.getPrice()).reduce(0.0, (a, b) -> add(a, b));
			Map<String, Object> orow = new HashMap<>();
			orow.put("orderId", row.getId());
			orow.put("outTradeNo", row.getOutTradeNo());
			orow.put("createdAt", row.getCreatedAt());
			orow.put("isPayed", row.getIsPayed());
			orow.put("total", total);
			orow.put("payType", row.getPayType());
			orow.put("products", orderDetailsRow.stream().map(o -> {
				Map<String, Object> rr = new HashMap<>();
				ProductRow product = products.stream().filter(oi -> oi.getId().equals(o.getProductId())).findFirst()
						.get();
				rr.put("productId", o.getProductId());
				rr.put("productName", product.getProductname());
				rr.put("productIntr", product.getDescription());
				rr.put("useNum", o.getUseNum());
				rr.put("usePeriod", o.getUsePeriod());
				rr.put("peoplePrice", o.getPersoncost());
				rr.put("productPrice", o.getApplicationcost());
				return rr;
			}));
			orderDtos.add(orow);
		}
		return new PagedResult<>(orderDtos, totalRows, order.getPageNo(), order.getPageSize());
	}

	public OrderDto loadOrderDetails(String outTradeNo) {
		OrderRow orderRow = tradeMapper.findOrderbyOutTradeNo(outTradeNo);
		List<OrderDetailsRow> orderDetailsRow = tradeMapper.findOrderDetailsByOrderId(orderRow.getId());

		List<ProductsDto> products = getProductsDtos(orderDetailsRow);

		double total = products.stream().map(o -> o.getPrice()).reduce(0.0, (a, b) -> add(a, b));
		double paymentDue = total;
		return new OrderDto(orderRow.getId(), orderRow.getOutTradeNo(), orderRow.getCreatedAt(), orderRow.getIsPayed(),
				total, paymentDue, orderRow.getPayType(), products);
	}

	public VoucheDto loadVoucheDetails(long id) {

		VoucherRow row = tradeMapper.findVoucherById(id);
		List<SnapshotApplicationRow> snapshotApplications = tradeMapper
				.findSnapshotApplicationBySnapshotId(row.getSnapshotId());

		List<SnapshotApplicationModuleRow> SnapshotApplicationModules = tradeMapper
				.findSnapshotApplicationModuleBySnapshotId(row.getSnapshotId());

		List<VoucheModuleDto> voucheModules = SnapshotApplicationModules.stream()
				.map(o -> new VoucheModuleDto(
						applicationModuleDao.findApplicationModuleById(o.getModuleId()).getModulename()))
				.collect(Collectors.toList());

		List<VoucheDetailsDto> voucheDetails = snapshotApplications.stream()
				.map(o -> new VoucheDetailsDto(applicationDao.findApplicationbyId(o.getApplicationId()).getAppname(),
						voucheModules))
				.collect(Collectors.toList());
		
		
		String resourceid = productMapper.load(row.getProductId()).getAvatarresourceid();
		AdminResourceRow  adminResourceRow = productRepository.findResourceById(resourceid);
		byte[] img = adminResourceRow.getContent();
		
		return new VoucheDto(row.getVoucherNo(), row.getUseNum(), row.getUsePeriod(), row.getStartTime(),
				row.getEndTime(), row.getCreatedAt(), voucheDetails,
				productMapper.load(row.getProductId()).getProductname(),
				productMapper.load(row.getProductId()).getDescription(),
				img);
	}

	public int insertTrade(TradeRow row) {
		return tradeMapper.insertTrade(row);
	}

	public int insertTradeDetails(TradeDetailsRow row) {
		return tradeMapper.insertTradeDetails(row);
	}

	public Map<String, Object> findTradeByTradeId(String tradeId) {

		Map<String, Object> result = new HashMap<>();
		List<TradeDetailsRow> tdetails = this.tradeMapper.findTradeDetailsByTradeId(tradeId);
		List<ProductRow> products = this.productMapper
				.findByIds(tdetails.stream().map(o -> o.getProductId()).collect(Collectors.toList()));
		result.put("products", tdetails.stream().map(o -> {
			Map<String, Object> row = new HashMap<>();
			ProductRow product = products.stream().filter(oi -> oi.getId().equals(o.getProductId())).findFirst().get();
			row.put("productId", o.getProductId());
			row.put("productName", product.getProductname());
			row.put("productIntr", product.getDescription());
			row.put("useNum", o.getUseNum());
			row.put("usePeriod", o.getUsePeriod());
			row.put("peoplePrice", o.getPersoncost());
			row.put("productPrice", o.getApplicationcost());
			return row;
		}));
		double productPrice = tdetails.stream().map(o -> new BigDecimal(o.getPrice()))
				.reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
		double sumPayPrice = productPrice;
		result.put("productPrice", productPrice);
		result.put("sumPayPrice", sumPayPrice);
		return result;
	}

	public List<OrderDetailsRow> findOrderDetailsByOrderId(long orderId) {
		return tradeMapper.findOrderDetailsByOrderId(orderId);
	}

	public List<TradeDetailsRow> findTradeDetailsByTradeId(String tradeId) {
		return tradeMapper.findTradeDetailsByTradeId(tradeId);
	}

	public TradeRow findTradeById(String tradeId) {
		return tradeMapper.findTradeById(tradeId);
	}

	public long CreateOrder(OrderRow row) {
		tradeMapper.CreateOrder(row);
		return row.getId();
	}

	public int CreateOrderDetails(OrderDetailsRow row) {
		return tradeMapper.insertOrderDetails(row);
	}

	public int insertVoucher(VoucherRow row) {
		return tradeMapper.insertVoucher(row);
	}

	public long insertSnapshot(SnapshotRow row) {
		tradeMapper.insertSnapshot(row);
		return row.getId();
	}

	public int insertSnapshotApplication(SnapshotApplicationRow row) {
		return tradeMapper.insertSnapshotApplication(row);
	}

	public int insertsnapshotApplicationModule(SnapshotApplicationModuleRow row) {
		return tradeMapper.insertsnapshotApplicationModule(row);
	}

	public int insertSnapshotModuleOperation(SnapshotModuleOperationRow row) {
		return tradeMapper.insertSnapshotModuleOperation(row);
	}

	public PagedResult<VoucherListDto> findCertById(PagingQuery query) {
		int totalRows = this.tradeMapper.getCertCount(query);
		query.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());

		Date t = new Date();
		List<VoucherListDto> Certs = this.tradeMapper.getCertList(query).stream()
				.map(o -> new VoucherListDto(o.getId(), o.getVoucherNo(), o.getUseNum(), o.getUsePeriod(),
						o.getStartTime(), o.getEndTime(), o.getCreatedAt(),
						productMapper.load(o.getProductId()).getProductname(),
						productMapper.load(o.getProductId()).getDescription(), compare_date(t, o.getEndTime())))
				.collect(Collectors.toList());
		return new PagedResult<>(Certs, totalRows, query.getPageNo(), query.getPageSize());
	}

	public OrderRow findOrderbyOutTradeNo(String OutTradeNo) {
		return tradeMapper.findOrderbyOutTradeNo(OutTradeNo);
	}
	
	public OrderRow findOrderbyId(long id) {
		return tradeMapper.findOrderbyId(id);
	}

	public int deleteOrder(long id) {
		return tradeMapper.deleteOrder(id);
	}

	public Date ConverDate(Date date) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.parse(format.format(date));
	}
	
	public FinanceAccountRow findFinanceAccount(String flag) {
		return tradeMapper.findFinanceAccount(flag);
	}

	private List<ProductsDto> getProductsDtos(List<OrderDetailsRow> orderDetailsRow) {
		List<ProductsDto> products = new ArrayList<>();

		for (OrderDetailsRow orderDetails : orderDetailsRow) {
			ProductsDto productsDto = convertProductsDto(orderDetails.getProductId(), orderDetails.getUseNum(),
					orderDetails.getUsePeriod(), orderDetails.getPrice(),
					productMapper.load(orderDetails.getProductId()).getDescription(), orderDetails.getPersoncost(),
					orderDetails.getApplicationcost());
			products.add(productsDto);
		}
		return products;
	}

	private ProductsDto convertProductsDto(long productId, int useNum, String usePeriod, double price,
			String productIntr, double peoplePrice, double productPrice) {
		return new ProductsDto(productId, applicationDao.findProductById(productId).getProductname(), useNum, usePeriod,
				price, productIntr, peoplePrice, productPrice);
	}

	/**
	 * 两个Double数相加
	 * 
	 * @param v1
	 * @param v2
	 * @return Double
	 */
	private Double add(Double v1, Double v2) {
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
	private Double sub(Double v1, Double v2) {
		BigDecimal b1 = new BigDecimal(v1.toString());
		BigDecimal b2 = new BigDecimal(v2.toString());
		return b1.subtract(b2).doubleValue();
	}

	private int compare_date(Date t1, Date t2) {
		if (t1.getTime() > t2.getTime()) {
			return 1;
		}
		return 0;
	}
}
