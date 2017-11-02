package cn.zhijian.passport.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.AppAppendPowerInfo;
import cn.zhijian.passport.api.AuthorizeInfo;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.OAuthCodeRequest;
import cn.zhijian.passport.api.OAuthCodeResponse;
import cn.zhijian.passport.api.OAuthTokenRequire;
import cn.zhijian.passport.api.OAuthTokenResponse;
import cn.zhijian.passport.commands.AppAppendPowerCommand;
import cn.zhijian.passport.commands.AuthorizationCodeCommand;
import cn.zhijian.passport.commands.OAuthCodeCommand;
import cn.zhijian.passport.commands.OAuthPasswordCommand;
import cn.zhijian.passport.commands.RefreshTokenCommand;
import cn.zhijian.passport.commands.SearchAppPowerCommand;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonAppOperationRow;
import cn.zhijian.passport.session.SessionStore;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Oauth2接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/oauth")
public class Oauth2Resource 
{
	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final ApplicationDao applicationDao;
	final CorporateMapper corporateMapper;
	final String loginUrl;
	final CorporateRoleMapper roleMapper;

	public Oauth2Resource(SessionStore sessionStore, 
			CommandGateway cmdGw, 
			ApplicationDao applicationDao,
			CorporateMapper corporateMapper,
			String loginUrl,
			CorporateRoleMapper roleMapper) {
		this.sessionStore = sessionStore;
		this.cmdGw = cmdGw;
		this.applicationDao = applicationDao;
		this.corporateMapper = corporateMapper;
		this.loginUrl = loginUrl;
		this.roleMapper = roleMapper;
	}
	
	private boolean VerifyUrl(String url){
		
		String regex = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[wW][wW][wW]./*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$";
		return StringUtils.isEmpty(url) || !Pattern.matches(regex, url);
	}

    @ApiOperation("授权")
	@GET
	@Path("/authorize")
	public Response authorize(@CookieParam(Constants.Session_ID_Cookie_Name) String sessionId,
			@QueryParam("client_id")String clientid,
			@QueryParam("redirect_uri")String redirecturi,
			@QueryParam("response_type")String responsetype,
			@QueryParam("state")String state) throws Exception {
		
		if(this.VerifyUrl(redirecturi)){
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"invalid_request\"}").build();
		}
		if(StringUtils.isEmpty(clientid) || StringUtils.isEmpty(responsetype)){
			return Response.status(Status.FOUND).location(new URI(redirecturi + "#error=invalid_request" + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
		}
		if(!"code".equals(responsetype)){
			return Response.status(Status.FOUND).location(new URI(redirecturi + "#error=unsupported_response_type" + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
		}
		ApplicationRow applicationRow = applicationDao.findByClientid(clientid);
		if( applicationRow == null){
			return Response.status(Status.FOUND).location(new URI(redirecturi + "#error=unauthorized_client" + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
		}
		if( !applicationRow.getCallbackurl().equals(redirecturi)){
			return Response.status(Status.FOUND).location(new URI(redirecturi + "#error=unauthorized_client" + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
		}
		LoginContext ctx = this.sessionStore.get(sessionId);
		OAuthCodeRequest request = new OAuthCodeRequest(clientid, redirecturi, responsetype, (StringUtils.isEmpty(state) ? "": state));
		if(ctx == null){
			sessionId = UUID.randomUUID().toString();
			ctx = new LoginContext( sessionId, null, null, null, null,null);
			ctx.setOauth(request);
			this.sessionStore.put(sessionId, ctx);
			
			NewCookie lgc = new NewCookie(Constants.SESSION_ID_HEADER_NAME, sessionId,"/",null,1,null,-1,null,false,false);
			return Response.status(Status.FOUND).location(new URI(loginUrl)).cookie(lgc).build();
		}
		else{
			OAuthCodeResponse ocr = cmdGw.sendAndWait(new OAuthCodeCommand( ctx.getPerson(), request,ctx.getCurrentCorporate() == null ? null :ctx.getCurrentCorporate().getId()));
			if(ocr == null){
				return Response.status(Status.FOUND).location(new URI(redirecturi + "#error=unauthorized_client" + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
			}
			else{
				return Response.status(Status.FOUND).location(new URI(ocr.getCallBackUrl() + "?code=" + ocr.getCode() + (StringUtils.isEmpty(state) ? "":"&state=" + state))).build();
			}
		}
	}
	
    @ApiOperation("令牌")
	@POST
	@Path("/token")
	public Response token(@FormParam("client_id") String clientid,
			@FormParam("client_secret")String clientsecret,
			@FormParam("code")String code,
			@FormParam("grant_type")String granttype,
			@FormParam("redirect_uri")String redirecturi,
			@FormParam("username")String username,
			@FormParam("password")String password) throws Exception {
		
		if(StringUtils.isEmpty(granttype)){
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"invalid_request\"}").build();
		}
		if("authorization_code".equals(granttype))
		{
			if(StringUtils.isEmpty(clientid) || StringUtils.isEmpty(clientsecret)
					|| StringUtils.isEmpty(code) || StringUtils.isEmpty(redirecturi)){
				return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"invalid_request\"}").build();
			}
			OAuthTokenResponse otr = cmdGw.sendAndWait(new AuthorizationCodeCommand(new OAuthTokenRequire(clientid,clientsecret,code,granttype,redirecturi)));
			if(otr == null){
				return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"unauthorized_client\"}").build();
			}
			else{
				return Response.ok(otr).type(MediaType.APPLICATION_JSON).build();
			}
		}
		else if("password".equals(granttype)){
			if(StringUtils.isEmpty(clientid) || StringUtils.isEmpty(clientsecret) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
				return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"invalid_request\"}").build();
			}
			OAuthTokenResponse otr = cmdGw.sendAndWait(new OAuthPasswordCommand(clientid,clientsecret,username,password));
			if(otr == null){
				return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"unauthorized_client\"}").build();
			}
			else{
				return Response.ok(otr).type(MediaType.APPLICATION_JSON).build();
			}
		}
		else{
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"unsupported_response_type\"}").build();
		}
	}
	
    @ApiOperation("刷新令牌")
    @ApiImplicitParams({@ApiImplicitParam(name = "Authorization", value = "bearer token", required = true, dataType = "string", paramType = "header")})
	@POST
	@Path("/refresh")
	public Response refresh(@ApiParam(hidden = true)@Auth AuthorizeInfo authorizeInfo,
			@FormParam("grant_type") String granttype,
			@FormParam("refresh_token") String refreshtoken){
		
		if(StringUtils.isEmpty(granttype) || StringUtils.isEmpty(refreshtoken)){
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"invalid_request\"}").build();
		}
		if(!"refresh_token".equals(granttype)){
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"unsupported_grant_type\"}").build();
		}
		OAuthTokenResponse otr = cmdGw.sendAndWait(new RefreshTokenCommand(refreshtoken,authorizeInfo.getPersonid()));
		if(otr == null){
			return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity("{\"error\":\"unauthorized_client\"}").build();
		}
		else{
			return Response.ok(otr).type(MediaType.APPLICATION_JSON).build();
		}
	}
	
    @ApiOperation("令牌用户信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "Authorization", value = "bearer token", required = true, dataType = "string", paramType = "header")})
    @POST
	@Path("/logininfo")
	@RolesAllowed("")
	public Response logininfo(@ApiParam(hidden = true)@Auth AuthorizeInfo authorizeInfo){
		
		Map<String, Object> map = new HashMap<>();
		map.put("userid", authorizeInfo.getPersonid());
		map.put("username", authorizeInfo.getName());
		map.put("realname", authorizeInfo.getRealname());
		if(authorizeInfo.getCorporateid() != null){
			CorporateRow item = corporateMapper.load(authorizeInfo.getCorporateid());
			map.put("comapnyid", item.getId().toString());
			map.put("comapnyname", item.getName());
			map.put("uniqueidentification", item.getCorporateMark());
		}
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(map).build();
	}
	
    @ApiOperation("应用开关(操作)")
    @ApiImplicitParams({@ApiImplicitParam(name = "Authorization", value = "bearer token", required = true, dataType = "string", paramType = "header")})
	@GET
	@Path("/applicationoperation")
	public Response getOperation(@ApiParam(hidden = true)@Auth AuthorizeInfo authorizeInfo){
		
    	Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR_OF_DAY, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0); 
		List<PersonAppOperationRow> operations = this.roleMapper.getPersonAppOperation(authorizeInfo.getPersonid(), 
				authorizeInfo.getCorporateid() , authorizeInfo.getApplicationid(),todayStart.getTime());
		List<Map<String,Object>> response = new ArrayList<>();
		for(PersonAppOperationRow o : operations){
			Map<String,Object> operation = new HashMap<String,Object>();
			operation.put("module", o.getModulename());
			operation.put("operate", o.getOperationname());
			response.add(operation);
		}
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
	}
	
    @ApiOperation("应用开关添加开关(操作)")
	@POST
	@Path("/appendapppower")
	public Response appendAppPower(AppAppendPowerInfo appendinfo){
		
		GenericResult<String, String> response;
		ApplicationRow app = this.applicationDao.findAppByClient(appendinfo.getClientid(),appendinfo.getClientsecret());
		if(app == null){
			response = new GenericResult<String, String>(false,"应用信息不符",null,"应用信息不符");
		}
		else{
			Pair<Boolean,String> result = cmdGw.sendAndWait(new AppAppendPowerCommand(app.getId(),appendinfo.getModules()));
			response = result.getLeft() ? new GenericResult<String, String>(true,"添加完毕",null,null) : new GenericResult<String, String>(false,result.getRight(),null,result.getRight());
		}
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
	}
	
    @ApiOperation("应用开关信息")
	@GET
	@Path("/getallpower")
	public Response getAppAllPower(@QueryParam("clientid")String clientid,@QueryParam("clientsecret")String clientsecret){
		
		GenericResult<List<Object>,String> response;
		ApplicationRow app = this.applicationDao.findAppByClient(clientid,clientsecret);
		if(app == null){
			response = new GenericResult<List<Object>,String>(false,"应用信息不符",new ArrayList<Object>(),"应用信息不符");
		}
		else{
			List<Object> result = cmdGw.sendAndWait(new SearchAppPowerCommand(app.getId()));
			response = new GenericResult<List<Object>,String>(true,null,result,null);
		}
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
	}
}
