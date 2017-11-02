package cn.zhijian.trade.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.trade.db.row.VoucherApplicationModuleView;
import cn.zhijian.trade.db.row.VoucherApplicationView;
import cn.zhijian.trade.db.row.VoucherRow;

public interface VoucherMapper {

	@Select("select v.* from voucher v where v.walletid =#{walletId}")
	List<VoucherRow> findByWalletId(@Param("walletId")String walletId);
	
	@Select("<script>"
			+ "select v.id as voucherid,v.snapshotid,sa.applicationid,a.appname as applicationname "
			+ "from voucher v inner join snapshotapplication sa on v.snapshotid = sa.snapshotid "
			+ "inner join application a on sa.applicationid = a.id "
			+ "where v.id in <foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</script>")
	List<VoucherApplicationView> findAppByVoucherId(@Param("ids") List<Long> voucherIds);
	
	@Select("<script>"
			+ "select v.id as voucherid,v.snapshotid,sam.applicationid,"
			+ "sam.moduleid as applicationmoduleid,am.modulename as applicationmodulename "
			+ "from voucher v inner join snapshotapplicationmodule sam on v.snapshotid = sam.snapshotid "
			+ "inner join applicationmodule am on sam.moduleid = am.id "
			+ "where v.id in <foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</script>")
	List<VoucherApplicationModuleView> findAppModuleByVoucherId(@Param("ids") List<Long> voucherIds);
	
	@Select("<script><if test=\"ids!=null and ids.size()>0\">"
			+ "select * from voucher where id in <foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if></script>")
	List<VoucherRow> findByIds(@Param("ids")List<Long> ids);
}