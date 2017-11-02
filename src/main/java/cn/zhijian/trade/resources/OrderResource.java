package cn.zhijian.trade.resources;

import java.text.ParseException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;

import com.codahale.metrics.annotation.Timed;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.pay.api.Pay.BehaviorType;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.trade.api.CreateOrder;
import cn.zhijian.trade.api.DelOrder;
import cn.zhijian.trade.api.Order;
import cn.zhijian.trade.commands.CreateOrderCommand;
import cn.zhijian.trade.commands.DeleteOrderCommand;
import cn.zhijian.trade.dto.OrderDto;
import cn.zhijian.trade.reps.TradeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "前台订单接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/order")
public class OrderResource {

	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final PayRepository payRepository;
	final TradeRepository tradeRepository;

	public OrderResource(CommandGateway cmdGw, SessionStore sessionStore, PayRepository payRepository,
			TradeRepository tradeRepository) {
		// TODO Auto-generated constructor stub
		this.cmdGw = cmdGw;
		this.sessionStore = sessionStore;
		this.payRepository = payRepository;
		this.tradeRepository = tradeRepository;
	}

	@ApiOperation(value = "创建订单")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response generate(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, CreateOrder createOrder)
			throws Exception {
		Pair<Boolean, String> str = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new CreateOrderCommand(createOrder, ctx.getPerson().getUsername(),
					ctx.getPerson().getRealName() == null ? ctx.getPerson().getUsername()
							: ctx.getPerson().getRealName(),ctx.getPerson().getId()));
		}, null);

		if (str.getLeft() != true) {
			return Response.ok(new GenericResult<>(str.getLeft(), null, null, str.getRight())).build();
		}
		return Response.ok(new GenericResult<>(str.getLeft(), null, str.getRight(), null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("全部订单")
	@POST
	@Path("/all")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAll(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Order order) {

		PagedResult<Map<String,Object>> orderDto = sessionStore.doInSession(sessionId, ctx -> {
			if (order.getBehaviorType() == BehaviorType.PERSONAL) {

				PersonRow personRow = payRepository.findPersonByPId(ctx.getPerson().getId());
				try {
					return tradeRepository.loadOrderList(order, personRow.getWalletId(), null);
				} catch (ParseException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				CorporateRow corporateRow = payRepository.findCorporateByCId(ctx.getCurrentCorporate().getId());
				try {
					return tradeRepository.loadOrderList(order, corporateRow.getWalletId(), null);
				} catch (ParseException e) {
					e.printStackTrace();
					return null;
				}
			}
		}, null);

		if (orderDto == null) {
			return Response.ok(new GenericResult<PagedResult<OrderDto>, String>(false, null, null, "操作失败"))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok(new GenericResult<PagedResult<Map<String,Object>>, String>(true, null, orderDto, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("未支付")
	@POST
	@Timed
	@Path("/notpay")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getNotPayed(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Order order) {

		PagedResult<Map<String,Object>> orderDto = sessionStore.doInSession(sessionId, ctx -> {
			if (order.getBehaviorType() == BehaviorType.PERSONAL) {

				PersonRow personRow = payRepository.findPersonByPId(ctx.getPerson().getId());
				try {
					return tradeRepository.loadOrderList(order, personRow.getWalletId(), 0);
				} catch (ParseException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				CorporateRow corporateRow = payRepository.findCorporateByCId(ctx.getCurrentCorporate().getId());
				try {
					return tradeRepository.loadOrderList(order, corporateRow.getWalletId(), 0);
				} catch (ParseException e) {
					e.printStackTrace();
					return null;
				}
			}
		}, null);

		if (orderDto == null) {
			return Response.ok(new GenericResult<PagedResult<OrderDto>, String>(false, null, null, "操作失败"))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok(new GenericResult<PagedResult<Map<String,Object>>, String>(true, null, orderDto, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("订单详情")
	@GET
	@Path("/{id}")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getDetails(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, @PathParam("id") String outTradeNo) {
		OrderDto orderDto = sessionStore.doInSession(sessionId, ctx -> {
			return tradeRepository.loadOrderDetails(outTradeNo);
		});
		
		if (orderDto == null) {
			return Response.ok(new GenericResult<OrderDto, String>(false, null, null, "操作失败"))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok(new GenericResult<OrderDto, String>(true, null, orderDto, null))
				.type(MediaType.APPLICATION_JSON).build();
	}
	
	@ApiOperation("订单删除")
	@POST
	@Path("/del")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, DelOrder DelOrder) {
		Pair<Boolean, String> result = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new DeleteOrderCommand(DelOrder.getOrderId()));
		});
		
		if(result.getLeft()==true) {
			return Response.ok(new GenericResult<>(true, null, result.getRight(), null)).build();
		}
		return Response.ok(new GenericResult<>(false, null, null, result.getRight())).build();
	}
}
