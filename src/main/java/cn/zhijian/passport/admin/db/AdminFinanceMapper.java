package cn.zhijian.passport.admin.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.admin.api.FinanceBill;
import cn.zhijian.passport.admin.row.FinanceAccountRow;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.db.row.BillRow;

public interface AdminFinanceMapper {

	@Select("insert into financeAccount (financeAccountName, financeAccountNo, flag, createdBy, createdAt) "
			+ "values(#{financeAccountName},#{financeAccountNo},#{flag},#{createdBy},#{createdAt})")
	int insertFinanceAccount(FinanceAccountRow row);

	@Select("<script>" + "select count(*) from financeAccount where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
			+ "<foreach collection=\"q.query\" index=\"index\" item=\"item\" separator=\" and\">"
			+ "<if test=\"item.getFilterRange()!=null and item.getFilterRange()!=''\">" 
			+ "<choose>"
			+ "<when test=\"item.getOperat()=='like'\">" 
			+ " ${item.getColumn()} like '${item.getFilterRange()}%'"
			+ "</when>" 
			+ "<otherwise>"
			+ " ${item.getColumn()}<![CDATA[${item.getOperat()}]]>'${item.getFilterRange()}'" 
			+ "</otherwise>"
			+ "</choose>" 
			+ "</if>" 
			+ "</foreach>" 
			+ "</if>" 
			+ "</script>")
	int countFinanceAccount(@Param("q") PagingQuery query);

	@Select("<script>select * from financeAccount where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
			+ "<foreach collection=\"q.query\" index=\"index\" item=\"item\" separator=\" and\">"
			+ "<if test=\"item.getFilterRange()!=null and item.getFilterRange()!=''\">" + "<choose>"
			+ "<when test=\"item.getOperat()=='like'\">" 
			+ " ${item.getColumn()} like '${item.getFilterRange()}%' "
			+ "</when>" 
			+ "<otherwise>"
			+ " ${item.getColumn()}<![CDATA[${item.getOperat()}]]>'${item.getFilterRange()}' " 
			+ "</otherwise>"
			+ "</choose>" 
			+ "</if>" 
			+ "</foreach>" 
			+ "</if>"
			+ "<if test=\"q.sort!=null and q.sort.keySet().size() > 0\">" + " order by"
			+ "<foreach collection=\"q.sort.keySet()\" item=\"item\" separator=\",\">" 
			+ "${item} ${q.sort.get(item)}"
			+ "</foreach>" 
			+ "</if>" 
			+ " limit ${q.pageSize} offset ${q.pageSize} * (${q.pageNo}-1)" + "</script>")
	List<FinanceAccountRow> findFinanceAccount(@Param("q") PagingQuery query);
	
	@Select("<script>"
			+ "select count(*) from bill b inner join orderData o on b.orderId = o.id where o.isPayed=1 and 1=1 and b.target=#{target} "
			+ "<if test=\"sTime!=null and eTime!=null \">and b.updatedat <![CDATA[>=]]> #{sTime} and b.updatedat <![CDATA[<=]]> #{eTime} </if>"
			+ "<if test=\"billNo!=null and billNo!=''\">and billNo like '#{billNo}%' </if>"
			+ "<if test=\"userName!=null and userName!=''\">and userName like '#{userName}%' </if>" + "</script>")
	int countfinanceBill(@Param("target") long target, @Param("sTime") Date sTime, @Param("eTime") Date eTime,
			@Param("billNo") String billNo, @Param("userName") String userName);

	@Select("<script>"
			+ "select b.* from bill b inner join orderData o on b.orderId = o.id where o.isPayed=1 and 1=1 and b.target=#{target} "
			+ "<if test=\"sTime!=null and eTime!=null \">and b.updatedat <![CDATA[>=]]> #{sTime} </if>"
			+ "<if test=\"billNo!=null and billNo!=''\">and billNo like '#{billNo}%'</if>"
			+ "<if test=\"userName!=null and userName!=''\">and userName like '#{userName}%' </if>" + "</script>")
	List<BillRow> sTimeBillList(@Param("target") long target, @Param("sTime") Date sTime, @Param("eTime") Date eTime,
			@Param("billNo") String billNo, @Param("userName") String userName);

	@Select("<script>"
			+ "select b.* from bill b inner join orderData o on b.orderId = o.id where o.isPayed=1 and 1=1 and b.target=#{target} "
			+ "<if test=\"sTime!=null and eTime!=null \">and b.updatedat <![CDATA[>=]]> #{sTime} and b.updatedat <![CDATA[<=]]> #{eTime} </if>"
			+ "<if test=\"billNo!=null and billNo!=''\">and billNo like '#{billNo}%' </if>"
			+ "<if test=\"userName!=null and userName!=''\">and userName like '#{userName}%'</if>"
			+ "order by b.updatedAt desc limit #{limit} offset #{offset}" + "</script>")
	List<BillRow> findBillList(@Param("target") long target, @Param("sTime") Date sTime, @Param("eTime") Date eTime,
			@Param("billNo") String billNo, @Param("userName") String userName, @Param("limit") int limit,
			@Param("offset") int offset);
	
	@Select("select * from financeAccount where id = #{id}")
	FinanceAccountRow findFinanceAccountById(@Param("id") long id);
	
	@Select("select b.* from bill b inner join orderData o on b.orderId = o.id where o.isPayed=1 and b.target=#{target}")
	List<BillRow> findBillByTarget(@Param("target") long target);
}
