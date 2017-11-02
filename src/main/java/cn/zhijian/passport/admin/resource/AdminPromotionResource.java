package cn.zhijian.passport.admin.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;

import cn.zhijian.passport.admin.commands.GiftPromotionModifyCommand;
import cn.zhijian.passport.admin.commands.PromotionChangeStateCommand;
import cn.zhijian.passport.admin.dto.GiftProductDetailDto;
import cn.zhijian.passport.admin.dto.GiftProductInputDto;
import cn.zhijian.passport.admin.dto.GiftPromotionListDto;
import cn.zhijian.passport.admin.dto.PromotionChangeStateDto;
import cn.zhijian.passport.admin.reps.AdminPromotionRepository;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.PromotionStatus;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api("后台促销接口")
@Path("/admin/promotion")
@Produces(MediaType.APPLICATION_JSON)
public class AdminPromotionResource {

	final AdminPromotionRepository repo;
	final CommandGateway cmdGw;
	
	public AdminPromotionResource(AdminPromotionRepository repo,CommandGateway cmdGw){
		
		this.repo = repo;
		this.cmdGw = cmdGw;
	}
	
    @ApiOperation("赠送促销列表")
	@POST
	@Path("/gift/list")
	@RolesAllowed("")
	public Response giftList(@ApiParam(hidden = true)@Auth JWTPrincipal user,PagingQuery query){
		
		PagedResult<GiftPromotionListDto> result = this.repo.filterGiftPromotion(query);
		return Response.ok(new GenericResult<PagedResult<GiftPromotionListDto>, String>(true, null, result, null)).build();
	}
    
    @ApiOperation("新增赠送促销")
	@GET
	@Path("/gift/info/{id}")
	@RolesAllowed("")
	public Response giftInfo(@ApiParam(hidden = true)@Auth JWTPrincipal user,@PathParam("id")Long id){
		
    	return Response.ok(new GenericResult<GiftProductDetailDto, String>(true, null, this.repo.getGiftProductDetail(id), null)).build();
	}

    @ApiOperation("修改赠送促销")
    @Path("/gift/edit")
	@POST
	@RolesAllowed("")
	public Response giftEdit(@ApiParam(hidden = true)@Auth JWTPrincipal user,GiftProductInputDto input){
		
    	Pair<Boolean,String> result = this.cmdGw.sendAndWait(new GiftPromotionModifyCommand(input,user.getUserId(),user.getName()));
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, String.format("%s成功", input.getId()==null?"新增":"修改"), null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).build();
	}
    
    @ApiOperation("促销上下线")
    @Path("/online")
	@POST
	@RolesAllowed("")
	public Response changePromotionState(@ApiParam(hidden = true)@Auth JWTPrincipal user,PromotionChangeStateDto input){
		
    	Pair<Boolean,String> result = this.cmdGw.sendAndWait(new PromotionChangeStateCommand(input.getId(),
    			CodeEnumUtil.codeOf(SalesPromotionStatus.class, input.getOnline()),user.getUserId(),user.getPersonName()));
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, "状态变更完毕", null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).build();
	}
    
    @ApiOperation("促销状态")
    @Path("/statemap")
	@GET
	@RolesAllowed("")
	public Response promotionSearchStatus(@ApiParam(hidden = true)@Auth JWTPrincipal user){
		
    	Map<Integer,String> response = new HashMap<>();
    	for(PromotionStatus item:PromotionStatus.values()){
    		response.put(item.getCode(), item.getDes());
    	}
    	return Response.ok(new GenericResult<Map<Integer,String>, String>(true, null, response, null)).build();
	}
    
    //事件+物品类型组合
    @ApiOperation("赠送优惠方式")
    @Path("/gift/waymap")
	@GET
	@RolesAllowed("")
	public Response giftWay(@ApiParam(hidden = true)@Auth JWTPrincipal user){
		
    	return Response.ok(new GenericResult<Map<String,String>, String>(true, null, this.repo.getGiftWayStatus(), null)).build();
	}
    
    //事件
    @ApiOperation("赠送优惠条件")
    @Path("/gift/event/{type}")
	@GET
	@RolesAllowed("")
	public Response giftEvent(@ApiParam(hidden = true)@Auth JWTPrincipal user,@PathParam("type")Integer type){
		return Response.ok(new GenericResult<Map<Long,String>, String>(true, null, this.repo.getEventByType(CodeEnumUtil.codeOf(BusinessType.class, type)), null))
				.build();
	}
    
    //赠送物品类型
    @ApiOperation("赠送优惠方式")
    @Path("/gift/method")
	@GET
	@RolesAllowed("")
	public Response giftMethod(@ApiParam(hidden = true)@Auth JWTPrincipal user){
		
    	return Response.ok(new GenericResult<Map<Integer,String>, String>(true, null, this.repo.getGiftType(), null)).build();
	}
    
    @ApiOperation("赠送产品列表")
    @Path("/gift/nopromotionlist/{type}")
	@GET
	@RolesAllowed("")
	public Response giftProduct(@PathParam("type")Integer type){
    	return Response.ok(new GenericResult<List<ProductRow>, String>(true, null, this.repo.getProduct(CodeEnumUtil.codeOf(BusinessType.class,type)), null))
    			.build();
	}
}