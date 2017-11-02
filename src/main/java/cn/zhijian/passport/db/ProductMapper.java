package cn.zhijian.passport.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.ProductPriceRow;

public interface ProductMapper {
	@Select("select * from product where id = #{id}")
	ProductRow load(@Param("id") long id);
	
	@Select("select * from productprice where productid=#{productid}")
	List<ProductPriceRow> getProductPriceByProductId(long productid);
	
	@Select("<script><if test=\"ids != null and ids.size() > 0\">"
			+ "select * from product where id in "
			+ "<foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if></script>")
	List<ProductRow> findByIds(@Param("ids")List<Long> ids);
	
	@Select("<script>"
			+ "select count(*) from product where 1=1 "
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
	int getProductCount(@Param("q") PagingQuery query);
	
	@Select("<script>"
			+ "select * from product where 1=1 "
			+ "<if test=\"q.query!=null and q.query.size()>0\"> and"
			+ "<foreach collection=\"q.query\" index=\"index\" item=\"item\" separator=\" and\">"
			+ "<if test=\"item.getFilterRange()!=null and item.getFilterRange()!=''\">"
			+ "<choose>"
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
			+ "<if test=\"q.sort!=null and q.sort.keySet().size() > 0\">"
		    + " order by"
		    + "<foreach collection=\"q.sort.keySet()\" item=\"item\" separator=\",\">"
			+ "${item} ${q.sort.get(item)}"
		    + "</foreach>"
			+ "</if>"
			+ " limit ${q.pageSize} offset ${q.pageSize} * (${q.pageNo}-1)"
			+ "</script>")
	List<ProductRow> getProductList(@Param("q")PagingQuery query);
}
