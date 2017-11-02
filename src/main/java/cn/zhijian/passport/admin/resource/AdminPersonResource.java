package cn.zhijian.passport.admin.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cn.zhijian.passport.admin.dto.PersonListDto;
import cn.zhijian.passport.admin.reps.AdminPersonRepository;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.WalletRequest;
import cn.zhijian.pay.dto.BillOrderDto;
import cn.zhijian.pay.query.PayRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="后台个人接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/admin/person")
public class AdminPersonResource {
	final AdminPersonRepository adminPersonRepository;
	final PayRepository payRepository;
	
	public AdminPersonResource(AdminPersonRepository adminPersonRepository,PayRepository payRepository)
	{
		this.adminPersonRepository = adminPersonRepository;
		this.payRepository = payRepository;
	}
	
	@POST
	@Path("/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response personlist(PagingQuery query) {
		PagedResult<PersonListDto> result = adminPersonRepository.loadPersonList(query).map(o -> {
			return new PersonListDto(o.getId(), o.getUsername(), o.getRealName(), o.getEmail(), o.getMobile(), o.getCreatedAt(),o.getWalletId());
		});
		return Response.ok(new GenericResult<PagedResult<PersonListDto>, String>(true, null, result, null))
				.type(MediaType.APPLICATION_JSON).build();
	}
	
	@POST
	@Path("/recharge")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recharge(WalletRequest request)
			throws Exception {
		PagedResult<BillOrderDto> rows = payRepository.loadBill(request, BillType.INCOME);
		return Response.ok(rows).build();
	}

	@POST
	@Path("/expenses")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response expenses(WalletRequest request)
			throws Exception {
		PagedResult<BillOrderDto> rows = payRepository.loadBill(request, BillType.EXPEND);
		return Response.ok(rows).build();
	}
}
