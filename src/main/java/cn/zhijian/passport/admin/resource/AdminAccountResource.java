package cn.zhijian.passport.admin.resource;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.admin.commands.AdminLoginCommand;
import cn.zhijian.passport.api.Login;
import cn.zhijian.passport.config.SiteConfig;
import cn.zhijian.passport.db.row.AdminPersonRow;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="后台用户接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/admin/account")
public class AdminAccountResource {
	
	final static Logger logger = LoggerFactory.getLogger(AdminAccountResource.class);
	final ObjectMapper mapper = new ObjectMapper(); 
	final CommandGateway cmdGw;
	final SiteConfig config;
	
	public AdminAccountResource(SiteConfig config, CommandGateway cmdGw){
		
		this.config = config;
		this.cmdGw = cmdGw;
	}
	
    @ApiOperation("用户登录")
	@POST
	@Path("/login")
	public Response login(Login login) throws UnsupportedEncodingException, JoseException, JsonProcessingException{
		
		Pair< String, AdminPersonRow> result = cmdGw.sendAndWait(new AdminLoginCommand(login.getUsername(),login.getPassword()));
		Map< String, Object> response = new HashMap();
		if(!StringUtils.isEmpty(result.getLeft())){
			response.put("success", false);
			response.put("message", result.getLeft());
			return Response.ok(response).build();
		}
		else{
			response.put("success", true);
	        JwtClaims claims = new JwtClaims();
	        claims.setExpirationTimeMinutesInTheFuture(20);
	        claims.setSubject(mapper.writeValueAsString(
	        		new JWTPrincipal(result.getRight().getId(),result.getRight().getUsername(),result.getRight().getName())
	        ));
	        JsonWebSignature jws = new JsonWebSignature();
	        jws.setPayload(claims.toJson());
	        jws.setAlgorithmHeaderValue(HMAC_SHA256);
	        jws.setKey(new HmacKey(config.getJwtTokenSecret()));
	        NewCookie c = new NewCookie(config.getJwtCookieName(),jws.getCompactSerialization(),"/admin",null,1,null,-1,null,false,false);
	        return Response.ok(response).cookie(c).build();
		}
	}
	
    @ApiOperation("用户登录状态验证")
	@POST
	@Path("/loginstate")
	public Response loginstate(@ApiParam(hidden = true)@Auth JWTPrincipal user){
		
		return Response.ok().build();
	}
}
