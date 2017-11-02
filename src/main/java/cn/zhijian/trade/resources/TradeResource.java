package cn.zhijian.trade.resources;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;

import com.codahale.metrics.annotation.Timed;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.pay.api.Pay.BehaviorType;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.trade.api.Trade;
import cn.zhijian.trade.commands.CreateTradeCommand;
import cn.zhijian.trade.reps.TradeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "前台结算接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/trade")
public class TradeResource {

	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final TradeRepository tradeRepository;
	final PayRepository payRepository;

	public TradeResource(SessionStore sessionStore, CommandGateway cmdGw, TradeRepository tradeRepository,
			PayRepository payRepository) {
		this.sessionStore = sessionStore;
		this.cmdGw = cmdGw;
		this.tradeRepository = tradeRepository;
		this.payRepository = payRepository;
	}

	@ApiOperation("创建交易ID")
	@Timed
	@POST
	public Response createTrade(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Trade trade) {
		Pair<Boolean, String> result = sessionStore.doInSession(sessionId, ctx -> {
			if (trade.getBehaviorType() == BehaviorType.PERSONAL) {
				return cmdGw.sendAndWait(new CreateTradeCommand(trade, ctx.getPerson().getWalletId(),
						ctx.getPerson().getRealName() == null ? ctx.getPerson().getUsername()
								: ctx.getPerson().getRealName()));
			} else {
				CorporateRow corporateRow = payRepository.findCorporateByCId(ctx.getCurrentCorporate().getId());
				if (corporateRow.getIsPending() != CorporateEnum.Authentication_Pass) {
					return Pair.of(false, "已认证才可购买产品");
				}
				return cmdGw.sendAndWait(new CreateTradeCommand(trade, ctx.getCurrentCorporate().getWalletId(),
						ctx.getPerson().getRealName()));
			}
		}, null);
		if (result.getLeft() == true) {
			return Response.ok(new GenericResult<String, String>(result.getLeft(), null, result.getRight(), null))
					.build();
		} else {
			return Response.ok(new GenericResult<String, String>(result.getLeft(), null, null, result.getRight()))
					.build();
		}
	}

	@ApiOperation("获取交易详情")
	@Timed
	@GET
	@Path("/{id}")
	public Response getTrade(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") String tradeId) {
		return Response.ok(new GenericResult<Object, String>(true, null,
				tradeRepository.findTradeByTradeId(tradeId), null)).build();
	}
}
