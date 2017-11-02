package cn.zhijian.passport.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.commands.GainMessageCommand;
import cn.zhijian.passport.commands.ModityMessageCommand;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.row.MessageAfficheDetailRow;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台消息接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/message")
public class MessageResource {

	final static Logger logger = LoggerFactory.getLogger(MessageResource.class);
	ObjectMapper mapper = new ObjectMapper(); 
	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	final MessageMapper messageMapper;
	
	public MessageResource(CommandGateway cmdGw,SessionStore sessionStore,MessageMapper messageMapper){
	
		this.cmdGw = cmdGw;
		this.sessionStore = sessionStore;
		this.messageMapper = messageMapper;
	}
	
	@GET
	@Path("/person/{type}")
	public Response getPersonMessage(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,@PathParam("type") MessageType type) 
			throws JsonProcessingException{
		
		Object result = sessionStore.doInSession(sessionId, ctx->{
			return this.cmdGw.sendAndWait(new GainMessageCommand(type,
					MessageAccessType.Pull,
					MessageBelongType.Person,
					false,
					false,
					ctx.getPerson().getId(),
					null
			));
		});
		return Response.ok(mapper.writeValueAsString(result)).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8")).build();
	}
	
	@GET
	@Path("/corporate/{type}/{id}")
	public Response getCorporateMessage(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("type") MessageType type,@PathParam("id") Long id) throws JsonProcessingException{
		
		List<Object> result = sessionStore.doInSession(sessionId, ctx->{
			return this.cmdGw.sendAndWait(new GainMessageCommand(type,
					MessageAccessType.Pull,
					MessageBelongType.Corporate,
					false,
					false,
					ctx.getPerson().getId(),
					id
			));
		});
		return Response.ok(mapper.writeValueAsString(result)).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8")).build();
	}
	
	@GET
	@Path("/affichedetail/{id}")
	public Response getAfficheDetail(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,@PathParam("id") Long id){
		
		MessageAfficheDetailRow affiche = this.messageMapper.getAfficheDetail(id).get(0);
		Map<String,String> result = new HashMap<String,String>();
		result.put("content", affiche.getContent());
		return Response.ok(result).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8")).build();
	}
	
	@POST
	@Path("/read")
	public Response readMessage(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,List<Long> ids){
		
		Pair< Boolean, String> result = this.cmdGw.sendAndWait(new ModityMessageCommand(ids,true,null));
		Map<String,Object> response = new HashMap<String,Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
	}
	
	@POST
	@Path("/delete")
	public Response deleteMessage(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,List<Long> ids){
		
		Pair< Boolean, String> result = this.cmdGw.sendAndWait(new ModityMessageCommand(ids,null,true));
		Map<String,Object> response = new HashMap<String,Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok().type(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).entity(response).build();
	}
}
