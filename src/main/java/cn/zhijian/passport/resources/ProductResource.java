package cn.zhijian.passport.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.api.QueryInfo;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.ProductStatus;
import cn.zhijian.passport.repos.ProductRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;

@Api(value="前台产品接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

	final static Logger logger = LoggerFactory.getLogger(ProductResource.class);
	
	final ProductRepository repos;
	
	public ProductResource(ProductRepository repos){
		
		this.repos = repos;
	}
	
    @ApiOperation("产品列表")
	@POST
	@Path("/list")
	public Response productList(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,PagingQuery query){
		
    	QueryInfo statusQuery = new QueryInfo();
    	statusQuery.setColumn("status");
    	statusQuery.setFilterRange(ProductStatus.PutOnShelves.getCode().toString());
    	statusQuery.setOperat("=");
    	query.getQuery().add(statusQuery);
    	PagedResult<ProductRow> pageData = this.repos.filterProduct(query);
    	List<AdminResourceRow> resources = this.repos.findResourceByIds(pageData.getResult().stream().filter(o->!StringUtils.isEmpty(o.getAvatarresourceid()))
    			.map(o->o.getAvatarresourceid()).collect(Collectors.toList()));
    	PagedResult<Object> result = pageData.map(o->{
    		Map<String,Object> row = new HashMap<>();
    		row.put("productId", o.getId());
    		Optional<AdminResourceRow> resource = resources.stream().filter(oi->oi.getId().equals(o.getAvatarresourceid())).findFirst();
    		if(resource.isPresent()){
        		row.put("productIntrImg", resource.get().getContent());	
    		}
    		else{
    			row.put("productIntrImg", "");	
    		}
    		row.put("productName", o.getProductname());
    		row.put("productIntr", o.getDescription());
    		return row;
    	});
    	return Response.ok(new GenericResult<PagedResult<Object>, String>(true, null, result, null)).build();
	}
	
    @ApiOperation("产品价格")
	@POST
	@Path("/price")
	public Response productPrice(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,ProductID productId){
	
    	return Response.ok(new GenericResult<Object, String>(true, null, this.repos.getProductPrice(productId.getProductId()), null)).build();
	}
    
    @Data
    public static class ProductID{
    	Long productId;
    }
}