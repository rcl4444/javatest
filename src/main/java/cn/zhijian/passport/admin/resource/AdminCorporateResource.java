package cn.zhijian.passport.admin.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.CorporateAuditCommand;
import cn.zhijian.passport.admin.commands.CorporateCertificationCommand;
import cn.zhijian.passport.admin.db.AdminCorporateMapper;
import cn.zhijian.passport.admin.dto.CorporateAuditDto;
import cn.zhijian.passport.admin.dto.CorporateCertDto;
import cn.zhijian.passport.admin.dto.CorporateListModel;
import cn.zhijian.passport.api.CorporateAudit;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.api.Resource;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.ResourceRow;
import cn.zhijian.passport.repos.CorporateRepository;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.WalletRequest;
import cn.zhijian.pay.dto.BillOrderDto;
import cn.zhijian.pay.query.PayRepository;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "后台企业接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/admin/corporate")
@Produces(MediaType.APPLICATION_JSON)
public class AdminCorporateResource {

	final static Logger logger = LoggerFactory.getLogger(AdminCorporateResource.class);
	final CommandGateway cmdGw;
	final CorporateRepository repo;
	final CorporateMapper corporateMapper;
	final AdminCorporateMapper adminCorporateMapper;
	final PayRepository payRepository;

	public AdminCorporateResource(CommandGateway cmdGw, CorporateRepository repo, CorporateMapper corporateMapper,
			AdminCorporateMapper adminCorporateMapper, PayRepository payRepository) {

		this.cmdGw = cmdGw;
		this.repo = repo;
		this.corporateMapper = corporateMapper;
		this.adminCorporateMapper = adminCorporateMapper;
		this.payRepository = payRepository;
	}

	@ApiOperation("公司列表")
	@POST
	@Path("/list")
	@RolesAllowed("")
	public Response corporateList(@ApiParam(hidden = true) @Auth JWTPrincipal user, PagingQuery query) {

		PagedResult<CorporateListModel> result = repo.filterCorporate(query).map(o -> {
			return new CorporateListModel(o.getId(), o.getName(), o.getCreditCode(), o.getHsCode(),
					o.getIsPending().getCode(), o.getCreatedAt(), o.getWalletId());
		});
		return Response.ok(new GenericResult<PagedResult<CorporateListModel>, String>(true, null, result, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("公司详情")
	@GET
	@Path("/audit/{id}")
	@RolesAllowed("")
	public Response corporateDetail(@ApiParam(hidden = true) @Auth JWTPrincipal user, @PathParam("id") Long id) {

		CorporateRow row = corporateMapper.load(id);
		if (row == null) {
			return Response.ok(new GenericResult<>(false, "该数据不存在", null, null)).type(MediaType.APPLICATION_JSON)
					.build();
		} else {
			CorporateAuditDto c = new CorporateAuditDto(row.getId(), row.getName(), row.getCreditCode(),
					row.getHsCode(), row.getIsPending().getCode(), row.getCreatedAt());
			return Response.ok(new GenericResult<CorporateAuditDto, String>(true, null, c, null))
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@ApiOperation("公司状态枚举")
	@GET
	@Path("/corporatestatus")
	public Response getCorporateEnum() {

		Map<Integer, String> result = new HashMap<Integer, String>();
		for (CorporateEnum item : CorporateEnum.values()) {
			result.put(item.getCode(), item.getDes());
		}
		return Response.ok(new GenericResult<Map<Integer, String>, String>(true, null, result, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("公司审核")
	@POST
	@Path("/audit")
	public Response corporateAudit(@ApiParam(hidden = true) @Auth JWTPrincipal user, CorporateAudit audit) {

		Pair<Boolean, String> result = this.cmdGw.sendAndWait(new CorporateAuditCommand(audit.getId(),
				audit.isStatus() ? CorporateEnum.Audit_Pass : CorporateEnum.Audit_Reject, audit.getReason()));
		return Response.ok(new GenericResult<String, String>(result.getLeft(), result.getRight(), null, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("公司认证详情")
	@GET
	@Path("/cert/{id}")
	@RolesAllowed("")
	public Response corporateCertDetail(@ApiParam(hidden = true) @Auth JWTPrincipal user, @PathParam("id") Long id) {

		CorporateRow row = corporateMapper.load(id);
		if (row == null) {
			return Response.ok(new GenericResult<>(false, "该数据不存在", null, null)).type(MediaType.APPLICATION_JSON)
					.build();
		} else {
			CorporateCertDto c = new CorporateCertDto(row.getId(), row.getIsPending().getCode(), row.getIndustryType(),
					row.getIndustry(), row.getNature(), row.getProvince(), row.getCity(), row.getBusinessLicense());
			return Response.ok(new GenericResult<CorporateCertDto, String>(true, null, c, null))
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@ApiOperation("公司认证")
	@POST
	@Path("/cert")
	public Response corporateCertification(@ApiParam(hidden = true) @Auth JWTPrincipal user, CorporateAudit audit) {
		Pair<Boolean, String> result = this.cmdGw.sendAndWait(new CorporateCertificationCommand(audit.getId(),
				audit.isStatus() ? CorporateEnum.Authentication_Pass : CorporateEnum.Authentication_Reject,
				audit.getReason()));
		return Response.ok(new GenericResult<String, String>(result.getLeft(), result.getRight(), null, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("公司头像")
	@GET
	@Path("resource/{id}")
	public Response get(@ApiParam(hidden = true) @Auth JWTPrincipal user, @PathParam("id") String resourceId) {

		ResourceRow rsc = adminCorporateMapper.loadResource(resourceId);

		Resource r = new Resource(rsc.getId(), rsc.getName(), rsc.getContentType(), rsc.getContent());
		if (r != null) {
			return Response.ok(r.getContent()).type(r.getContentType()).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@ApiOperation("充值记录")
	@POST
	@Path("/recharge")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recharge(@ApiParam(hidden = true) @Auth JWTPrincipal user, WalletRequest request) throws Exception {

		PagedResult<BillOrderDto> rows = payRepository.loadBill(request, BillType.INCOME);
		return Response.ok(rows).build();
	}

	@ApiOperation("消费记录")
	@POST
	@Path("/expenses")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response expenses(@ApiParam(hidden = true) @Auth JWTPrincipal user, WalletRequest request) throws Exception {
		PagedResult<BillOrderDto> rows = payRepository.loadBill(request, BillType.EXPEND);
		return Response.ok(rows).build();
	}
}
