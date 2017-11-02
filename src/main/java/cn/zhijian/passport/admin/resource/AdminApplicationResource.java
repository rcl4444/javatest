package cn.zhijian.passport.admin.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.DecimalMin;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.api.ApplicationEditInfo;
import cn.zhijian.passport.admin.commands.ApplicationCreateCommand;
import cn.zhijian.passport.admin.commands.ApplicationModifyCommand;
import cn.zhijian.passport.admin.reps.AdminApplicationRepository;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import cn.zhijian.passport.statustype.BusinessType;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="后台应用接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/admin/app")
public class AdminApplicationResource {
	
	final static Logger logger = LoggerFactory.getLogger(AdminApplicationResource.class);
	final CommandGateway cmdGw;
	final AdminApplicationRepository appRepository;
	
	public AdminApplicationResource(CommandGateway cmdGw,AdminApplicationRepository appRepository){
		
		this.cmdGw = cmdGw;
		this.appRepository = appRepository;
	}
	
    @ApiOperation("应用列表")
	@POST
	@Path("/list")
	@RolesAllowed("")
	public Response applicationList(@ApiParam(hidden = true)@Auth JWTPrincipal user,PagingQuery query){
		
		PagedResult<Object> result = appRepository.filterApplication(query).map(o -> {
			Map<String,Object> app = new HashMap<>();
			app.put("id", o.getId());
			app.put("appname", o.getAppname());
			app.put("createdAt",o.getCreatedate());
			app.put("appid",o.getClientid());
			app.put("type", o.getType().getDes());
			return app;
		});
		Map<String,Object> page = new HashMap<>();
		page.put("list", result.getResult());
		page.put("totalCount",result.getTotalCount());
		page.put("pageNo",result.getPageNo());
		page.put("pageSize",result.getPageSize());
		return Response.ok(new GenericResult<Object, String>(true, null, page, null))
				.type(MediaType.APPLICATION_JSON).build();
	}
	
    @ApiOperation("应用详情")
	@GET
	@Path("/info/{appid}")
	@RolesAllowed("")
	public Response appInfo(@ApiParam(hidden = true)@Auth JWTPrincipal user,
			@DecimalMin(value = "1",message = "应用id必须是大于0的数值")
			@PathParam("appid")Long appid){
		
		ApplicationRow ar = this.appRepository.findById(appid);
		if(ar == null){
			return Response.ok(new GenericResult<String, String>(false, "应用信息不存在", null, "应用信息不存在")).type(MediaType.APPLICATION_JSON).build();
		}
		Map<String,Object> response = new HashMap<>();
		response.put("id", ar.getId());
		response.put("appname",ar.getAppname());
		response.put("websiteLink",ar.getMainurl());
		response.put("callbackLink",ar.getCallbackurl());
		response.put("exitLink",ar.getLoginouturl());
		response.put("dataLink", ar.getGetInfoUrl());
		response.put("createdAt",ar.getCreatedate());
		response.put("appid",ar.getClientid());
		response.put("appsecret",ar.getClientsecret());
		response.put("type", ar.getType().getCode());
		response.put("activeid", ar.getAvatarresourceid());
		return Response.ok(new GenericResult<Object, String>(true, null, response, null)).type(MediaType.APPLICATION_JSON).build();
	}
	
    @ApiOperation("修改应用")
	@POST
	@Path("/edit")
	@RolesAllowed("")
	public Response appEdit(@ApiParam(hidden = true)@Auth JWTPrincipal user,ApplicationEditInfo info){
		
		Pair<Boolean,String> result;
		if(info.getId() == null){
			result = cmdGw.sendAndWait(new ApplicationCreateCommand(info));
		}
		else{
			result = cmdGw.sendAndWait(new ApplicationModifyCommand(info));
		}
		GenericResult<String, String> response = result.getLeft()?
				new GenericResult<String, String>(true, String.format("%s成功", info.getId()==null?"新增":"修改"), null, null)
				: new GenericResult<String, String>(false, result.getRight(), null, result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}
    
    @ApiOperation("应用类型")
    @GET
    @Path("/apptype")
	@RolesAllowed("")
    public Response getAppType(){
    	
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (BusinessType item : BusinessType.values()) {
			result.put(item.getCode(), item.getDes());
		}
		return Response.ok(new GenericResult<Map<Integer, String>, String>(true, null, result, null))
				.type(MediaType.APPLICATION_JSON).build();
    }
}