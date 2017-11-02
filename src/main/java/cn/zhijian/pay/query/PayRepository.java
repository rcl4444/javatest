package cn.zhijian.pay.query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.row.CorporateApplicationRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonApplicationRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.pay.api.Bill;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.WalletRequest;
import cn.zhijian.pay.db.PayMapper;
import cn.zhijian.pay.db.row.BillOrderRow;
import cn.zhijian.pay.db.row.BillRow;
import cn.zhijian.pay.db.row.WalletRow;
import cn.zhijian.pay.dto.BillOrderDto;
import lombok.Data;

@Data
public class PayRepository {
	final PayMapper payMapper;
	final PersonDAO personDAO;
	final CorporateMapper corporateMapper;
	final ApplicationDao applicationDao;

	public PayRepository(PayMapper payMapper, CorporateMapper corporateMapper, ApplicationDao applicationDao,
			PersonDAO personDAO) {
		this.payMapper = payMapper;
		this.corporateMapper = corporateMapper;
		this.applicationDao = applicationDao;
		this.personDAO = personDAO;
	}

	public int CreateWallet(WalletRow row) {
		return payMapper.CreateWallet(row);
	}

	public int UpdateIsPay(String outTradeNo, int isPayed) {
		return payMapper.UpdateIsPayed(outTradeNo, isPayed);

	}

	public int ModityWalletBalance(String WalletId, double balance) {
		return payMapper.ModityWalletBalance(balance, WalletId);
	}

	public double GetBalance(String walletId) {
		return payMapper.GetBalance(1, walletId) == null ? 0 : payMapper.GetBalance(1, walletId).getBalance();
	}

	public WalletRow findWalletbyId(String WalletId) {
		return payMapper.findWalletbyId(WalletId);
	}

	public PagedResult<BillOrderDto> loadBill(WalletRequest recharge, BillType billType) throws ParseException {
		int offset = (recharge.getPageNo() - 1) * recharge.getPageSize();
		int totalRows = payMapper.countbyBill(1, billType, ConverDate(recharge.getSTime()),
				ConverDate(recharge.getETime()), recharge.getWalletId());
		int s = recharge.getPageSize() == null ? totalRows : recharge.getPageSize();

		List<BillOrderRow> BillOrders = payMapper.findBillFindByWalletId(1, billType, ConverDate(recharge.getSTime()),
				ConverDate(recharge.getETime()), recharge.getWalletId(), s, offset);
		return new PagedResult<>(BillOrders.stream()
				.map(o -> new BillOrderDto(o.getBody(), o.getMoney(), o.getUpdatedAt())).collect(Collectors.toList()),
				totalRows, recharge.getPageNo(), recharge.getPageSize());
	}

	public int updateIsUpgrade(int useNum, long id) {
		return payMapper.updateIsUpgrade(useNum, id);
	}

	public int createCorporateApplication(CorporateApplicationRow row) {
		return corporateMapper.createCorporateApplication(row);
	}

	public int createPersonApplication(PersonApplicationRow row) {
		return personDAO.createPersonApplication(row);
	}

	public CorporateRow findCorporateByWalletId(String walletId) {
		return corporateMapper.findCorporateByWalletId(walletId);
	}

	public CorporateRow findCorporateByCId(long cid) {
		return corporateMapper.findCorporateByCId(cid);
	}

	public PersonRow findPersonByWalletId(String walletId) {
		return personDAO.findPersonByWalletId(walletId);
	}

	public PersonRow findPersonByPId(long pid) {
		return personDAO.findPersonByPId(pid);
	}

	public CorporateApplicationRow findApplicationOnCorp(long applicationid, long corporateid) {
		return applicationDao.findCorporateApplicationByAppId(applicationid, corporateid);
	}

	public PersonApplicationRow findApplicationOnPerson(long applicationid, long pid) {
		return applicationDao.findPersonApplicationByAppId(applicationid, pid);
	}

	public BillOrderRow findBillRechargeRecords(String outTradeNo) {
		return payMapper.findBillRechargeRecords(1, BillType.INCOME, outTradeNo);
	}
	//
	// public int updateRechargeRecordsisSuccess(String serialNum, int isSuccess) {
	// return payMapper.updateRechargeRecordsisSuccess(serialNum, isSuccess);
	// }

	public int insertBill(String billNo, BillType billType, Bill bill) {
		return payMapper.insertBill(convertBill(billNo, billType, bill));
	}

	public BillRow findBillByOrderIdAndBillType(long orderId, BillType billType) {
		return payMapper.findBillByOrderIdAndBillType(orderId, billType);
	}

	public BillRow findBillByBillNoAndBillType(String BillNo, BillType billType) {
		return payMapper.findBillByBillNoAndBillType(BillNo, billType);
	}

	public int updateBillUpdatedAt(long orderId) {
		return payMapper.updateBillUpdatedAt(orderId, new Date());
	}

	private BillRow convertBill(String billNo, BillType billType, Bill bill) {
		return new BillRow(null, billNo, bill.getOrderId(), bill.getMoney(), bill.getSource(),
				bill.getTarget() == null ? null : bill.getTarget(), bill.getTradeType(), bill.getPayType(), billType,
				bill.getWalletId(), bill.getUserName(), bill.getCreatedBy(), bill.getCreatedAt(), null, null);
	}

	public Date ConverDate(Date date) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.parse(format.format(date));
	}
}
