package cn.zhijian.pay.resources;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.dto.OrderInfoDto;
import cn.zhijian.passport.resources.LoginContextResource;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.pay.api.Bill.BillType;
import cn.zhijian.pay.api.Charge;
import cn.zhijian.pay.api.OrderInfo;
import cn.zhijian.pay.api.Pay;
import cn.zhijian.pay.api.Pay.OrderType;
import cn.zhijian.pay.api.PayDetails;
import cn.zhijian.pay.api.PayResponse;
import cn.zhijian.pay.api.WalletRequest;
import cn.zhijian.pay.commands.ChargeCommand;
import cn.zhijian.pay.commands.CreateWxQRCommand;
import cn.zhijian.pay.commands.WXnotifyCommand;
import cn.zhijian.pay.db.row.BillOrderRow;
import cn.zhijian.pay.dto.BillOrderDto;
import cn.zhijian.pay.dto.ChargeDto;
import cn.zhijian.pay.query.PayRepository;
import cn.zhijian.trade.db.row.OrderRow;
import cn.zhijian.trade.reps.TradeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "支付接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/pay")
public class PayResource {
	final static Logger logger = LoggerFactory.getLogger(LoginContextResource.class);

	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	final PayRepository reps;
	final ApplicationDao applicationDao;
	final TradeRepository tradeRepository;

	public PayResource(CommandGateway commandGateway, SessionStore sessionStore, PayRepository reps,
			ApplicationDao applicationDao,TradeRepository tradeRepository) {

		this.cmdGw = commandGateway;
		this.sessionStore = sessionStore;
		this.reps = reps;
		this.applicationDao = applicationDao;
		this.tradeRepository = tradeRepository;
	}

	@ApiOperation("充值")
	@POST
	@Path("/charge")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response charge(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Charge charge) {

		Pair<Boolean, String> result = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(
					new ChargeCommand(charge, ctx.getPerson().getUsername(), ctx.getPerson().getRealName() == null ? ctx.getPerson().getUsername()
							: ctx.getPerson().getRealName(),ctx.getPerson().getId()));
		}, null);

		if (result.getLeft() == false) {
			return Response.ok(new GenericResult<>(result.getLeft(), null, null, result.getRight())).build();
		}
		return Response.ok(new GenericResult<>(result.getLeft(), null, result.getRight(), null)).build();
	}

	@ApiOperation("微信二维码")
	@POST
	@Path("/wxqr")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response wxqr(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@Context HttpServletRequest request, Pay pay) throws Exception {
		Pair<Boolean, OrderInfo> orderInfo = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new CreateWxQRCommand(pay, request, ctx));
		}, null);

		OrderInfoDto dto = new OrderInfoDto(
				orderInfo.getRight().getOutTradeNo() == null ? null : orderInfo.getRight().getOutTradeNo(),
				orderInfo.getRight().getTotalFee() == null ? null : orderInfo.getRight().getTotalFee(),
				orderInfo.getRight().getBody() == null ? null : orderInfo.getRight().getBody(),
				orderInfo.getRight().getCode() == null ? null : orderInfo.getRight().getCode());

		if (orderInfo.getLeft() == false) {
			return Response.ok(new GenericResult<>(false, null, null, orderInfo.getRight().getMessage())).build();
		}
		return Response.ok(new GenericResult<OrderInfoDto, String>(true, null, dto, null))
				.type(MediaType.APPLICATION_JSON).build();
	}

	@ApiOperation("微信回调接口")
	@POST
	@Path("/wxnotify")
	public Response wxnotify(@Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
		try {
			cmdGw.sendAndWait(new WXnotifyCommand(request, response));
			return Response.ok().build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("wx notify fail error :", e);
		}
		return null;
	}

	@ApiOperation("获取状态接口")
	@POST
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response status(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Pay pay) throws Exception {
		try {
			int b = sessionStore.doInSession(sessionId, ctx -> {
				String outTradeNo = pay.getOutTradeNo();
				OrderRow row = tradeRepository.findOrderbyOutTradeNo(outTradeNo);
				return row.getIsPayed();
			}, null);

			if (b == 1) {
				return Response.ok(true).build();
			}
			return Response.ok(false).build();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("wx notify fail").build();
		}
	}

	@ApiOperation("余额")
	@POST
	@Path("/balance")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response balance(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Pay pay) throws Exception {
		try {
			double balance = sessionStore.doInSession(sessionId, ctx -> {
				String walletId = pay.getWalletId();

				return reps.GetBalance(walletId);
			}, null);
			return Response.ok(balance).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("balance achieve fail").build();
		}
	}

	@ApiOperation("充值记录")
	@POST
	@Path("/recharge")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recharge(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, WalletRequest request) {
		PagedResult<BillOrderDto> rows = sessionStore.doInSession(sessionId, ctx -> {
			try {
				return reps.loadBill(request, BillType.INCOME);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}, null);
		return Response.ok(rows).build();
	}

	@ApiOperation("消费记录")
	@POST
	@Path("/expenses")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response expenses(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, WalletRequest request) {
		PagedResult<BillOrderDto> rows = sessionStore.doInSession(sessionId, ctx -> {
			try {
				return reps.loadBill(request, BillType.EXPEND);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}, null);
		return Response.ok(rows).build();
	}

	@ApiOperation("消费结果")
	@POST
	@Path("/result")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response result(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Pay pay) throws Exception {
		try {
			OrderRow row = sessionStore.doInSession(sessionId, ctx -> {
				String OutTradeNo = pay.getOutTradeNo();
				return tradeRepository.findOrderbyOutTradeNo(OutTradeNo);
			}, null);

			PayDetails details = new PayDetails();
			PayResponse response = new PayResponse();
			response.setOutTradeNo(row.getOutTradeNo());
			response.setOrderType(row.getOrderType());
			response.setDate(row.getCreatedAt());
			if (row.getOrderType() != OrderType.RECHARGE) {
				details.setDateType(row.getDateType());
				response.setDetails(details);
			} else {
				response.setMoery(row.getTotalFee());
			}
			return Response.ok(response).build();
		} catch (Exception e) {
			logger.debug(e.getMessage());
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("result fail").build();
		}
	}
	
	@ApiOperation("充值结果")
	@GET
	@Path("/charge/{id}")
	public Response chargeResult(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, @PathParam("id") String id) {
		BillOrderRow row = reps.findBillRechargeRecords(id);
		
		if(row==null) {
			return Response.ok(new GenericResult<ChargeDto, String>(true, null, null, "流水号不存在")).build();
		}
		ChargeDto chargeDto = new ChargeDto(row.getUpdatedAt(), row.getMoney());
		
		return Response.ok(new GenericResult<ChargeDto, String>(true, null, chargeDto, null)).build();
	}
}
