package cn.zhijian.passport.admin.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.ProductAppModuleRow;
import cn.zhijian.passport.db.row.ProductRow.ProductPriceRow;
import cn.zhijian.passport.statustype.BusinessType;

public interface AdminProductMapper {

	@Select("select * from product where id = #{id}")
	ProductRow load(@Param("id") long id);
	
	@Insert("insert into product(productname,type,status,remark,createdt,avatarresourceid,description)"
			+ " values (#{productname},${type.getCode()},${status.getCode()},#{remark},#{createdt},#{avatarresourceid},#{description})")
	@Options(useGeneratedKeys = true)
	int insert(ProductRow row);
	
	@Update("update product set productname=#{productname},type=${type.getCode()},"
			+ "status=${status.getCode()},remark=#{remark},createdt=#{createdt},avatarresourceid=#{avatarresourceid},description=#{description} "
			+ "where id=#{id}")
	int update(ProductRow row);
	
	@Select("<script>"
			+ "select * from product where productname=#{productname} and type=${type.getCode()} "
			+ "<if test=\"ignoreids!=null and ignoreids.size()>0\">"
			+ "and id not in <foreach item=\"em\" collection=\"ignoreids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if>"
			+ "</script>")
	ProductRow getByName(@Param("productname")String productname,@Param("type")BusinessType type,@Param("ignoreids")List<Long> ignoreids);
	
	@Select("select * from productappmodule where productid=#{productid}")
	List<ProductAppModuleRow> getProductModuleByProductId(long productid);
	
	@Insert("insert into productappmodule(productid,applicationid,applicationmoduleid) values (#{productid},#{applicationid},#{applicationmoduleid})")
	@Options(useGeneratedKeys = true)
	int insertProductModule(ProductAppModuleRow row);
	
	@Delete("<script>delete productappmodule where "
			+ "id in <foreach item=\"em\" collection=\"ids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</script>")
	int deleteProductModule(@Param("ids")List<Long> ids);
	
	@Select("select * from productprice where productid=#{productid}")
	List<ProductPriceRow> getProductPriceByProductId(@Param("productid")long productid);
	
	@Insert("insert into productprice(productid,personnum,usetime,applicationcost,personcost)"
			+ " values (#{productid},${personnum.getCode()},${usetime.getCode()},#{applicationcost},#{personcost})")
	@Options(useGeneratedKeys = true)
	int insertProductPrice(ProductPriceRow row);
	
	@Update("update productprice set productid=#{productid},personnum=${personnum.getCode()},usetime=${usetime.getCode()},"
			+ "applicationcost=#{applicationcost},personcost=#{personcost} where id=#{id}")
	int updateProductPrice(ProductPriceRow row);
	
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
	
	@Select("<script>"
			+ "select * from product where type=${type.getCode()}<if test=\"ignoreids!=null and ignoreids.size()>0\">"
			+ " and id not in <foreach item=\"em\" collection=\"ignoreids\" separator=\",\" open=\"(\" close=\")\">#{em}</foreach>"
			+ "</if>"
			+ "</script>")
	List<ProductRow> getByType(@Param("type")BusinessType type,@Param("ignoreids")List<Long> ignoreids);
}