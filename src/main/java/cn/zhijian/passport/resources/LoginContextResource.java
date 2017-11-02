package cn.zhijian.passport.resources;


import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.Login;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.LoginResponse;
import cn.zhijian.passport.api.OAuthCodeResponse;
import cn.zhijian.passport.api.SwitchCorporate;
import cn.zhijian.passport.commands.LoginCommand;
import cn.zhijian.passport.commands.OAuthCodeCommand;
import cn.zhijian.passport.commands.RefreshLoginContextCommand;
import cn.zhijian.passport.commands.SendSMSCommand;
import cn.zhijian.passport.commands.SwitchCorporateCommand;
import cn.zhijian.passport.session.SessionStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台用户接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/context")
public class LoginContextResource {

	final static Logger logger = LoggerFactory.getLogger(LoginContextResource.class);

	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	
	@Context
	HttpServletRequest request;

	public LoginContextResource(CommandGateway commandGateway,SessionStore sessionStore) {
		
		this.cmdGw = commandGateway;
		this.sessionStore = sessionStore;
	}
	
    @ApiOperation("用户登录")
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,Login login) throws Exception {
		
		LoginContext ctx = cmdGw.sendAndWait(new LoginCommand(login,sessionId));
		LoginResponse data = null;
		URL url = new URL(request.getRequestURL().toString());
		if (ctx != null) {
			NewCookie lgc = new NewCookie(Constants.Session_ID_Cookie_Name,ctx.getSessionId(),"/",url.getHost(),1,null,-1,null,false,true);
			if(ctx.getOauth() != null && ctx.getCurrentCorporate() != null){
				OAuthCodeResponse ocr = cmdGw.sendAndWait(new OAuthCodeCommand( ctx.getPerson(), ctx.getOauth(),ctx.getCurrentCorporate().getId()));
				data = new LoginResponse(ctx.getSessionId(),ocr.getCallBackUrl() + "?code=" + ocr.getCode()+"&state="+ocr.getState());
				ctx.setOauth(null);
				this.sessionStore.put(ctx.getSessionId(), ctx);
				return Response.ok(new GenericResult<LoginResponse, String>(true, null, data, null)).cookie(lgc).build();
			}
			data = new LoginResponse(ctx.getSessionId(),"");
			return Response.ok(new GenericResult<LoginResponse, String>(true, null, data, null)).cookie(lgc).build();
		} else {
			return Response.ok(new GenericResult<>(false, null, null, "账号密码错误")).build();
		}
	}

    @ApiOperation("重载用户信息")
	@GET
	@Path("/reload")
	public Response reload(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) throws Exception {
		logger.debug("Reloading Session ID Data: {}", sessionId);
		try {
			LoginContext ctx = cmdGw.sendAndWait(new RefreshLoginContextCommand(sessionId));
			if (ctx != null) {
				return Response.ok(ctx).type(MediaType.APPLICATION_JSON).build();
			}
		} catch (Exception e) {
			logger.debug("Reload Error", e);
		}
		return Response.status(Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("Session Expired").build();
	}

	@POST
	@Path("/switch")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response switchCorp(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, //
			SwitchCorporate switchTo) throws Exception {
		LoginContext ctx = cmdGw.sendAndWait(new SwitchCorporateCommand(sessionId, switchTo));
		if (ctx != null) {
			return Response.ok(ctx).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("Session Expired").build();
		}

	}

	@POST
	@Path("/sendverificationcode")
	public Response sendVerificationCode(String mobile) throws Exception {
		boolean ctx = cmdGw.sendAndWait(new SendSMSCommand(mobile));
		if (ctx == true) {
			return Response.ok(new GenericResult<>(true, null, null, null)).build();
		} else {
			return Response.ok(new GenericResult<>(false, null, null, "发送验证码失败")).build();
		}
	}
}
