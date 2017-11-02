package cn.zhijian.passport.admin.resource;

import java.text.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.api.FinanceAccount;
import cn.zhijian.passport.admin.api.FinanceBill;
import cn.zhijian.passport.admin.commands.CreateFinanceAccountCommand;
import cn.zhijian.passport.admin.dto.FinanceAccountListDto;
import cn.zhijian.passport.admin.dto.FinanceBillDto;
import cn.zhijian.passport.admin.reps.AdminFinanceRepository;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "后台财务接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/admin/finance")
@Produces(MediaType.APPLICATION_JSON)
public class AdminFinanceResource {
	final static Logger logger = LoggerFactory.getLogger(AdminFinanceResource.class);
	final CommandGateway cmdGw;
	final AdminFinanceRepository adminFinanceRepository;

	public AdminFinanceResource(CommandGateway cmdGw, AdminFinanceRepository adminFinanceRepository) {
		this.cmdGw = cmdGw;
		this.adminFinanceRepository = adminFinanceRepository;
	}

	@ApiOperation("财务账户")
	@POST
	@Path("/create")
	@RolesAllowed("")
	public Response createFinanceAccount(@ApiParam(hidden = true) @Auth JWTPrincipal user,
			FinanceAccount financeAccount) {

		Pair<Boolean, String> result = cmdGw
				.sendAndWait(new CreateFinanceAccountCommand(financeAccount, user.getName()));

		if (result.getLeft() == true) {
			return Response.ok(new GenericResult<>(true, null, result.getRight(), null)).build();
		}
		return Response.ok(new GenericResult<>(false, null, null, result.getRight())).build();
	}

	@ApiOperation("财务列表")
	@POST
	@Path("/list")
	public Response list(@ApiParam(hidden = true) @Auth JWTPrincipal user, PagingQuery query) {
		PagedResult<FinanceAccountListDto> result = adminFinanceRepository.financeAccountList(query);

		if (result == null) {
			return Response.ok(new GenericResult<>(false, null, null, "无数据")).build();
		}
		return Response.ok(new GenericResult<PagedResult<FinanceAccountListDto>, String>(true, null, result, null))
				.build();
	}

	@ApiOperation("流水账")
	@POST
	@Path("/bill")
	@RolesAllowed("")
	public Response bill(@ApiParam(hidden = true) @Auth JWTPrincipal user, FinanceBill bill) throws ParseException {
		PagedResult<FinanceBillDto> result = adminFinanceRepository.financeBillList(bill);
		if(result==null)
		{
			return Response.ok(new GenericResult<>(false, null, null, "无数据")).build();
		}
		return Response.ok(new GenericResult<PagedResult<FinanceBillDto>, String>(true, null , result, null)).build(); 
	}
}
