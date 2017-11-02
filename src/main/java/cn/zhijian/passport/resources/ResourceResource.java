package cn.zhijian.passport.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.Resource;
import cn.zhijian.passport.commands.CreateResourceCommand;
import cn.zhijian.passport.repos.ResourceRepository;
import cn.zhijian.passport.session.SessionStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台资源接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/resource")
public class ResourceResource {

	final static Logger logger = LoggerFactory.getLogger(ResourceResource.class);

	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	final ResourceRepository repo;

	@Inject
	public ResourceResource(SessionStore sessionStore, CommandGateway cmdGw, ResourceRepository repo) {
		this.cmdGw = cmdGw;
		this.sessionStore = sessionStore;
		this.repo = repo;
	}

	@GET
	@Path("{id}")
	public Response getRaw(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") String resourceId) {
		return get(sessionId, resourceId, null);
	}

	@GET
	@Path("{id}/{anyfilename}")
	public Response get(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") String resourceId, @PathParam("anyfilename") String anyfilename) {
		logger.debug("Session ID in Download API: {}", sessionId);
		Resource rsc = repo.load(sessionId, resourceId);
		if (rsc != null) {
			// String filename = anyfilename != null ? anyfilename :
			// rsc.getName();
			return Response.ok(rsc.getContent()).type(rsc.getContentType()).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	public Response upload(@FormDataParam("sid") String sessionId, //
			@FormDataParam("file") FormDataBodyPart body) throws Exception {
		logger.debug("Session ID in Upload API: {}", sessionId);
		try {
			byte[] content = body.getEntityAs(byte[].class);
			String filename = body.getContentDisposition().getFileName();
			String type = body.getMediaType().toString();
			Resource rsc = new Resource(null, filename, type, content);
			String uuid = cmdGw.sendAndWait(new CreateResourceCommand(sessionId, rsc));
			if (uuid != null) {
				return Response.ok("{\"status\": \"done\", \"id\":\"" + uuid + "\"}").build();
			}
		} catch (Exception e) {
			logger.error("Upload Resource Error", e);
		}
		return Response.ok("{\"status\": \"error\"}").build();
	}

}