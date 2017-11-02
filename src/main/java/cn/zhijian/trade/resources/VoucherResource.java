package cn.zhijian.trade.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.trade.dto.VoucheDto;
import cn.zhijian.trade.dto.VoucherListDto;
import cn.zhijian.trade.reps.TradeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="前台凭证接口")
@Path("/voucher")
public class VoucherResource {
	final SessionStore sessionStore;
	final TradeRepository tradeRepository;
	
	public VoucherResource(SessionStore sessionStore, TradeRepository tradeRepository) {
		// TODO Auto-generated constructor stub
		this.sessionStore = sessionStore;
		this.tradeRepository = tradeRepository;
	}
	
	@ApiOperation(value="查询凭证列表")
	@POST
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getList(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, PagingQuery query) {
		
		PagedResult<VoucherListDto> voucherListDto =  sessionStore.doInSession(sessionId, ctx -> {
			  return tradeRepository.findCertById(query);
		});
		return Response.ok(new GenericResult<PagedResult<VoucherListDto>, String>(true, null, voucherListDto, null))
				.build();
	}
	
	@ApiOperation(value="凭证详情")
	@GET
	@Timed
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response voucherDetails(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, @PathParam("id") long voucherId) {
		
		VoucheDto voucheDto = sessionStore.doInSession(sessionId, ctx ->{
			return tradeRepository.loadVoucheDetails(voucherId);
		});
		
		if(voucheDto==null)
		{
			return Response.ok(new GenericResult<VoucheDto, String>(false, null, null, "凭证不存在"))
					.build();
		}
		return Response.ok(new GenericResult<VoucheDto, String>(true, null, voucheDto, null))
				.build();
	}
}
