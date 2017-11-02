package cn.zhijian.passport.admin.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.ProductChangeStatusCommand;
import cn.zhijian.passport.admin.commands.ProductCreateCommand;
import cn.zhijian.passport.admin.commands.ProductModifyCommand;
import cn.zhijian.passport.admin.commands.ProductPriceSetCommand;
import cn.zhijian.passport.admin.dto.ProductChangeStateDto;
import cn.zhijian.passport.admin.dto.ProductInputDto;
import cn.zhijian.passport.admin.dto.ProductPriceInputDto;
import cn.zhijian.passport.admin.reps.AdminProductRepository;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.PersonOption;
import cn.zhijian.passport.db.row.ProductRow.ProductStatus;
import cn.zhijian.passport.db.row.ProductRow.UseTimeOption;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="后台产品接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/admin/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminProductResource {
	
	final static Logger logger = LoggerFactory.getLogger(AdminProductResource.class);
	final CommandGateway cmdGw;
	final AdminProductRepository productRepository;
	
	public AdminProductResource(CommandGateway cmdGw,AdminProductRepository productRepository){
		
		this.cmdGw = cmdGw;
		this.productRepository = productRepository;
	}
	
    @ApiOperation("产品列表")
	@POST
	@Path("/list")
	@RolesAllowed("")
	public Response productList(@ApiParam(hidden = true)@Auth JWTPrincipal user,PagingQuery query){
		
		PagedResult<Object> result = productRepository.filterApplication(query).map(o -> {
			Map<String,Object> app = new HashMap<>();
			app.put("id", o.getId());
			app.put("productname", o.getProductname());
			app.put("createdAt",o.getCreatedt());
			app.put("state", o.getStatus().getDes());
			app.put("type", o.getType().getDes());
			return app;
		});
		Map<String,Object> page = new HashMap<>();
		page.put("list", result.getResult());
		page.put("totalCount",result.getTotalCount());
		page.put("pageNo",result.getPageNo());
		page.put("pageSize",result.getPageSize());
		return Response.ok(new GenericResult<Object, String>(true, null, page, null)).build();
	}
    
    @ApiOperation("修改产品详情")
	@GET
	@Path("/modifyinfo/{id}")
	@RolesAllowed("")
	public Response modifyProductInfo(@ApiParam(hidden = true)@Auth JWTPrincipal user,
			@DecimalMin(value = "1",message = "应用id必须是大于0的数值")
			@PathParam("id")Long productid){
		
		ProductRow pr = this.productRepository.findById(productid);
		if(pr == null){
			return Response.ok(new GenericResult<String, String>(false, "产品信息不存在", null, "产品信息不存在")).type(MediaType.APPLICATION_JSON).build();
		}
		Map<String,Object> response = new HashMap<>();
		response.put("id", pr.getId());
		response.put("productname", pr.getProductname());
		response.put("type",pr.getType().getCode());
	    response.put("remark", pr.getRemark());
	    response.put("description", pr.getDescription());
	    response.put("state", pr.getStatus().getCode());
	    response.put("activeid", pr.getAvatarresourceid());
	    response.put("powers", this.productRepository.getProductPower(productid));
		return Response.ok(new GenericResult<Object, String>(true, null, response, null)).build();
	}
    
    @ApiOperation("新增产品详情")
	@GET
	@Path("/addinfo/{type}")
	@RolesAllowed("")
	public Response addProductInfo(@ApiParam(hidden = true)@Auth JWTPrincipal user,
			@PathParam("type")Integer type){
		
		Map<String,Object> response = new HashMap<>();
		BusinessType btype = CodeEnumUtil.codeOf(BusinessType.class, type);
		response.put("id", "");
		response.put("productname", "");
		response.put("type",btype.getCode());
		response.put("remark", "");
	    response.put("description", "");
	    response.put("state", ProductStatus.PullOffShelves.getCode());
	    response.put("activeid", "");
	    response.put("powers", this.productRepository.getProductPower(btype));
		return Response.ok(new GenericResult<Object, String>(true, null, response, null)).build();
	}
    
    @ApiOperation("修改产品详情")
	@POST
	@Path("/edit")
	@RolesAllowed("")
    public Response editProduct(@ApiParam(hidden = true)@Auth JWTPrincipal user,@Valid ProductInputDto product){
    	
    	Pair<Boolean, String> result;
    	if(product.getId() == null){
    		result = this.cmdGw.sendAndWait(new ProductCreateCommand(product,user.getUserId(),user.getPersonName()));
    	}
    	else{
    		result = this.cmdGw.sendAndWait(new ProductModifyCommand(product,user.getUserId(),user.getPersonName()));
    	}
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, String.format("%s成功", product.getId()==null?"新增":"修改"), null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).build();
    }
    
    @ApiOperation("变更产品状态")
	@POST
	@Path("/changestate")
	@RolesAllowed("")
    public Response productChangeState(@ApiParam(hidden = true)@Auth JWTPrincipal user,@Valid ProductChangeStateDto dto){
    	
    	Pair<Boolean, String> result = this.cmdGw.sendAndWait(new ProductChangeStatusCommand(dto.getProductid(),dto.getState(),user.getUserId(),user.getPersonName()));
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, "修改状态成功", null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).build();
    }
    
    @ApiOperation("产品价格")
	@POST
	@Path("/price/{id}")
	@RolesAllowed("")
    public Response productPriceInfo(@ApiParam(hidden = true)@Auth JWTPrincipal user,@PathParam("id")Long productid){
    	
    	List<Object> response = this.productRepository.getProductPrice(productid).stream().map(o->{
    		Map<String,Object> row = new HashMap<>();
    		row.put("time", o.getUsetime().getMonth());
    		row.put("timeindex", o.getUsetime().getCode());
    		row.put("people",o.getPersonnum().getPersonNum());
    		row.put("peopleindex", o.getPersonnum().getCode());
    		row.put("timeprice",o.getApplicationcost());
    		row.put("peopleprice",o.getPersoncost());
    		return row;
    	}).collect(Collectors.toList());
		return Response.ok(new GenericResult<List<Object>, String>(true, null, response, null)).build();
    }
    
    @ApiOperation("设置产品价格")
	@POST
	@Path("/setprice")
	@RolesAllowed("")
    public Response createProductPrice(@ApiParam(hidden = true)@Auth JWTPrincipal user,@Valid ProductPriceInputDto input){
    	
    	Pair<Boolean, String> result = this.cmdGw.sendAndWait(new ProductPriceSetCommand(input,user.getUserId(),user.getPersonName()));
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, "設置成功", null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).build();
    }
    
    @ApiOperation("時間維度")
	@POST
	@Path("/timeoption")
	@RolesAllowed("")
    public Response getUseTimeOption(){
    
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (UseTimeOption item : ProductRow.UseTimeOption.values()) {
			result.put(item.getCode(), item.getDes());
		}
		return Response.ok(new GenericResult<Map<Integer, String>, String>(true, null, result, null)).build();
    }
    
    @ApiOperation("人數維度")
	@POST
	@Path("/personoption")
	@RolesAllowed("")
    public Response getPersonOption(){
    
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (PersonOption item : ProductRow.PersonOption.values()) {
			result.put(item.getCode(), item.getDes());
		}
		return Response.ok(new GenericResult<Map<Integer, String>, String>(true, null, result, null)).build();
    }
}