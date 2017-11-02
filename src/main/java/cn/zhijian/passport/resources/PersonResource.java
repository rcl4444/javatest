package cn.zhijian.passport.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.Login;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.api.Registration;
import cn.zhijian.passport.api.ResourceID;
import cn.zhijian.passport.commands.CorporateInviteAduitCommand;
import cn.zhijian.passport.commands.EmailBindingCommand;
import cn.zhijian.passport.commands.ModifyPersonAvatarCommand;
import cn.zhijian.passport.commands.ModifyPersonCommand;
import cn.zhijian.passport.commands.PasswordResetCommand;
import cn.zhijian.passport.commands.PersonCorporateApplyStatusCommand;
import cn.zhijian.passport.commands.RefreshLoginContextCommand;
import cn.zhijian.passport.commands.RegistrationCommand;
import cn.zhijian.passport.commands.RegistrationConfirmationCommand;
import cn.zhijian.passport.commands.SendEmailBindingCommand;
import cn.zhijian.passport.commands.ValidatePasswordResetInfoCommand;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.session.SessionStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "前台个人接口")
@ApiResponses({ @ApiResponse(code = 200, message = "操作成功"), @ApiResponse(code = 400, message = "错误的请求"),
		@ApiResponse(code = 401, message = "权限不足"), @ApiResponse(code = 422, message = "输入验证失败"),
		@ApiResponse(code = 500, message = "服务器内部异常") })
@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

	final static Logger logger = LoggerFactory.getLogger(PersonResource.class);

	ObjectMapper mapper = new ObjectMapper();

	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	final PersonDAO personDao;

	@Inject
	public PersonResource(SessionStore sessionStore, CommandGateway cmdGw, PersonDAO personDao) {
		this.cmdGw = cmdGw;
		this.sessionStore = sessionStore;
		this.personDao = personDao;
	}

	@GET
	@Path("/myself")
	public Response myself(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) throws Exception {
		LoginContext ctx = cmdGw.sendAndWait(new RefreshLoginContextCommand(sessionId));
		if (ctx != null) {
			return Response.ok(ctx.getPerson()).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Access Denied").build();
		}
	}

	@POST
	@Path("/myself/avatar")
	public Response changeAvatar(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, ResourceID rsc)
			throws Exception {
		if ((Boolean) cmdGw.sendAndWait(new ModifyPersonAvatarCommand(sessionId, rsc))) {
			return Response.ok().type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@ApiOperation("注册")
	@POST
	@Path("/registration")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(final Registration input) throws Exception {

		Pair<Boolean, String> result = cmdGw.sendAndWait(new RegistrationCommand(input));

		if (result.getLeft() == true) {
			return Response.ok(new GenericResult<String, String>(true, null, result.getRight(), null)).build();
		}
		return Response.ok(new GenericResult<String, String>(false, null, null, result.getRight())).build();

	}

	@POST
	@Path("/registration/{code}")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateRegistration(@PathParam("code") final String validationCode, Login loginObj) {
		try {
			cmdGw.sendAndWait(new RegistrationConfirmationCommand(validationCode, loginObj.getUsername(),
					loginObj.getPassword()));
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Registration Validation Failed", e);
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Registration Failed").build();
		}
	}

	@ApiOperation("验证用户名重复")
	@POST
	@Path("/verificationname")
	public Response userNameVerification(String name) throws Exception {
		if (personDao.findPersonByUsername(name).size() > 0) {
			return Response.ok(new GenericResult<>(false, null, null, "用户名重复")).build();
		}
		return Response.ok(new GenericResult<>(true, null, null, null)).build();
	}

	@ApiOperation("验证手机号重复")
	@POST
	@Path("/verificationmobile")
	public Response mobileVerification(String mobile) throws Exception {
		if (personDao.finPersonByMobile(mobile).size() > 0) {
			return Response.ok(new GenericResult<>(false, null, null, "手机号重复")).build();
		}
		return Response.ok(new GenericResult<>(true, null, null, null)).build();
	}

	@POST
	@Path("/modifyperson")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPersonInfo(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Person person)
			throws Exception {
		boolean isTrue = cmdGw.sendAndWait(new ModifyPersonCommand(sessionId, person));
		if (isTrue == true) {
			return Response.ok(isTrue).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("Session Expired").build();
		}
	}

	@POST
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validate(final Registration input) throws Exception {
		Pair<Boolean, String> result = cmdGw.sendAndWait(new ValidatePasswordResetInfoCommand(input));
		if (result.getLeft() == true) {
			return Response.ok(new GenericResult<String, String>(true, null, result.getRight(), null)).build();
		}
		return Response.ok(new GenericResult<String, String>(false, null, null, result.getRight())).build();
	}

	@POST
	@Path("/passwordreset")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response passwordReset(final Registration input) throws Exception {

		Pair<Boolean, String> result = cmdGw.sendAndWait(new PasswordResetCommand(input));
		if (result.getLeft() == true) {
			return Response.ok(new GenericResult<String, String>(true, null, result.getRight(), null)).build();
		}
		return Response.ok(new GenericResult<String, String>(false, null, null, result.getRight())).build();

	}

	@POST
	@Path("/sendemailbinding")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendemailBinding(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Person person)
			throws Exception {
		try {
			boolean ctx = cmdGw.sendAndWait(new SendEmailBindingCommand(sessionId, person.getEmail()));
			return Response.ok(ctx).build();
		} catch (Exception e) {
			// TODO: handle exception
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("发送失败").build();
		}
	}

	@POST
	@Path("/emailbinding")
	public Response emailbinding(String bindingCode) {
		try {
			boolean ctx = cmdGw.sendAndWait(new EmailBindingCommand(bindingCode));
			return Response.ok(ctx).build();
		} catch (Exception e) {
			// TODO: handle exception
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("绑定失效").build();
		}
	}

	@GET
	@Path("/corporatelist")
	public Response corporateList(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId)
			throws JsonProcessingException {

		Object[] li = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.personDao.getTheCorporateList(ctx.getPerson().getId()).stream().map(o -> {
				Map<String, Object> oi = new HashMap<String, Object>();
				oi.put("id", o.getId());
				oi.put("name", o.getName());
				oi.put("isPending", o.getIsPending().getDes());
				return oi;
			}).toArray();
		});
		return Response.ok(mapper.writeValueAsString(li)).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8")).build();
	}

	@GET
	@Path("/staff/refuse/{id}")
	public Response inviteApplyRefuse(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long invitationid) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw
					.sendAndWait(new CorporateInviteAduitCommand(invitationid, false, ctx.getPerson().getUsername()));
		});
		if (result.getLeft()) {
			return Response.ok().build();
		} else {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity(result.getRight()).build();
		}
	}

	@GET
	@Path("/staff/pass/{id}")
	public Response inviteApplyPass(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long invitationid) {
		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw
					.sendAndWait(new CorporateInviteAduitCommand(invitationid, true, ctx.getPerson().getUsername()));
		});
		if (result.getLeft()) {
			return Response.ok().build();
		} else {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity(result.getRight()).build();
		}
	}

	@GET
	@Path("/corporateapplystatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response showCorporateApplyStatus(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) {

		List<Object> response = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw.sendAndWait(new PersonCorporateApplyStatusCommand(ctx.getPerson().getId()));
		});
		return Response.ok().entity(new GenericResult<Object, String>(true, null, response, null)).build();
	}
}