package cn.zhijian.passport.admin.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
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

import cn.zhijian.passport.admin.commands.AdminCreateResourceCommand;
import cn.zhijian.passport.admin.reps.AdminResourceRepository;
import cn.zhijian.passport.api.Resource;
import cn.zhijian.passport.resourceauth.JWTPrincipal;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="后台资源接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/admin/resource")
public class AdminResourceResource {
	
	final static Logger logger = LoggerFactory.getLogger(AdminResourceResource.class);

	final CommandGateway cmdGw;
	final AdminResourceRepository repo;

	@Inject
	public AdminResourceResource(CommandGateway cmdGw, AdminResourceRepository repo) {
		this.cmdGw = cmdGw;
		this.repo = repo;
	}

	@GET
	@Path("{id}")
	public Response getRaw(@ApiParam(hidden = true)@Auth JWTPrincipal user,
			@PathParam("id") String resourceId) {
		return get(user, resourceId, null);
	}

	@GET
	@Path("{id}/{anyfilename}")
	public Response get(@ApiParam(hidden = true)@Auth JWTPrincipal user,
			@PathParam("id") String resourceId, @PathParam("anyfilename") String anyfilename) {
		Resource rsc = this.repo.load(resourceId);
		if (rsc != null) {
			return Response.ok(rsc.getContent()).type(rsc.getContentType()).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	public Response upload(@FormDataParam("file") FormDataBodyPart body) throws Exception {
		try {
			byte[] content = body.getEntityAs(byte[].class);
			String filename = body.getContentDisposition().getFileName();
			String type = body.getMediaType().toString();
			String uuid = cmdGw.sendAndWait(new AdminCreateResourceCommand(filename,type,content,null,null));
			if (uuid != null) {
				return Response.ok("{\"status\": \"done\", \"id\":\"" + uuid + "\"}").build();
			}
		} catch (Exception e) {
			logger.error("Upload Resource Error", e);
		}
		return Response.ok("{\"status\": \"error\"}").build();
	}
}