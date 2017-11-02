package cn.zhijian.passport.repos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.db.AdminResourceDAO;
import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.ProductMapper;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.PersonOption;
import cn.zhijian.passport.db.row.ProductRow.ProductPriceRow;
import cn.zhijian.passport.db.row.ProductRow.UseTimeOption;

public class ProductRepository {

	final static Logger logger = LoggerFactory.getLogger(ProductRepository.class);
	
	final ProductMapper productMapper;
	
	final AdminResourceDAO adminResourceMapper;
	
	public ProductRepository(ProductMapper productMapper,AdminResourceDAO adminResourceMapper){
		
		this.productMapper = productMapper;
		this.adminResourceMapper = adminResourceMapper;
	}
	
	public PagedResult<ProductRow> filterProduct(PagingQuery query){
		
		int totalRows = this.productMapper.getProductCount(query);
		query.setPageSize(query.getPageSize() == null? 10 : query.getPageSize());
		return new PagedResult<>(this.productMapper.getProductList(query),totalRows,query.getPageNo(),query.getPageSize());
	}
	
	public Object getProductPrice(Long productid){
		
		ProductRow pr = this.productMapper.load(productid);
		List<ProductPriceRow> prices = this.productMapper.getProductPriceByProductId(productid);
		Map<String,Object> result = new HashMap<>();
		result.put("productId", pr.getId());
		result.put("productName", pr.getProductname());
		List<Map<String,Object>> pss = new ArrayList<>();
		if(prices.size() > 0)
		{
			for(PersonOption person: PersonOption.values()){
				Map<String,Object> ps = new HashMap<>();
				ps.put("useNum",person.getPersonNum());
				List<Map<String,Object>> uts = new ArrayList<>();
				for(UseTimeOption time: UseTimeOption.values()){
					Map<String,Object> ut = new HashMap<>();
					ut.put("usePeriod",time.getMonth());
					Optional<ProductPriceRow> price = prices.stream().filter(o->o.getPersonnum().equals(person)&& o.getUsetime().equals(time)).findFirst();
					if(price.isPresent()){
						ut.put("productPrice",price.get().getApplicationcost());
						ut.put("peoplePrice",price.get().getPersoncost());
					}
					uts.add(ut);
				}
				ps.put("prices", uts);
				pss.add(ps);
			}
		}
		result.put("productsPriceSet",pss);
		return result;
	}
	
	public List<AdminResourceRow> findResourceByIds(List<String> ids){
		return this.adminResourceMapper.findByIds(ids);
	}
	
	public AdminResourceRow findResourceById(String id) {
		return this.adminResourceMapper.load(id);
	}
}