package cn.zhijian.passport.admin.reps;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cn.zhijian.passport.admin.api.FinanceBill;
import cn.zhijian.passport.admin.db.AdminFinanceMapper;
import cn.zhijian.passport.admin.dto.FinanceAccountListDto;
import cn.zhijian.passport.admin.dto.FinanceBillDto;
import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.pay.db.row.BillOrderRow;
import cn.zhijian.pay.db.row.BillRow;
import cn.zhijian.pay.dto.BillOrderDto;

public class AdminFinanceRepository {
	final AdminFinanceMapper adminFinanceMapper;

	public AdminFinanceRepository(AdminFinanceMapper adminFinanceMapper) {
		this.adminFinanceMapper = adminFinanceMapper;
	}

	public int insert(FinanceAccountRow row) {
		return adminFinanceMapper.insertFinanceAccount(row);
	}

	public PagedResult<FinanceAccountListDto> financeAccountList(PagingQuery query) {

		int totalRows = this.adminFinanceMapper.countFinanceAccount(query);
		query.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());

		return new PagedResult<>(
				this.adminFinanceMapper.findFinanceAccount(query).stream()
						.map(o -> new FinanceAccountListDto(o.getId(), o.getFinanceAccountName(),
								adminFinanceMapper.findBillByTarget(o.getId()).stream().map(c -> c.getMoney())
										.reduce(0.0, (a, b) -> add(a, b))))
						.collect(Collectors.toList()),
				totalRows, query.getPageNo(), query.getPageSize());
	}

	public PagedResult<FinanceBillDto> financeBillList(FinanceBill financeBill) throws ParseException {
		int offset = (financeBill.getPageNo() - 1) * financeBill.getPageSize();

		int totalRows = adminFinanceMapper.countfinanceBill(financeBill.getTarget(),
				financeBill.getSTime() == null ? null : ConverDate(financeBill.getSTime()),
				financeBill.getETime() == null ? null : ConverDate(financeBill.getETime()), financeBill.getFlowNo(),
				financeBill.getOppoSiteAccount());
		int s = financeBill.getPageSize() == null ? totalRows : financeBill.getPageSize();
		List<BillRow> sTimebillRows = adminFinanceMapper.sTimeBillList(financeBill.getTarget(),
				financeBill.getSTime() == null ? null : ConverDate(financeBill.getSTime()),
				financeBill.getETime() == null ? null : ConverDate(financeBill.getETime()), financeBill.getFlowNo(),
				financeBill.getOppoSiteAccount());
		Double total = sTimebillRows.stream().map(o -> o.getMoney()).reduce(0.0, (a, b) -> add(a, b));

		List<BillRow> billRows = adminFinanceMapper.findBillList(financeBill.getTarget(),
				financeBill.getSTime() == null ? null : ConverDate(financeBill.getSTime()),
				financeBill.getETime() == null ? null : ConverDate(financeBill.getETime()), financeBill.getFlowNo(),
				financeBill.getOppoSiteAccount(), s, offset);

		List<FinanceBillDto> FinanceBills = new ArrayList<>();
		for (BillRow billRow : billRows) {
			FinanceBillDto financeBillDto = new FinanceBillDto(billRow.getUpdatedAt(), billRow.getBillNo(), "",
					String.valueOf(billRow.getMoney()), "-", total,
					adminFinanceMapper.findFinanceAccountById(billRow.getTarget()).getFinanceAccountName(),
					billRow.getCreatedBy(), billRow.getUserName());
			FinanceBills.add(financeBillDto);
			total = sub(total, billRow.getMoney());
		}

		return new PagedResult<>(FinanceBills, totalRows, financeBill.getPageNo(), financeBill.getPageSize());
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

	public Date ConverDate(Date date) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.parse(format.format(date));
	}
}
