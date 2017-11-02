package cn.zhijian.passport.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.CorporateRole;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.Staff;
import cn.zhijian.passport.api.StaffList;
import cn.zhijian.passport.commands.CreateCorporateRoleCommand;
import cn.zhijian.passport.commands.DeleteCorporateRoleCommand;
import cn.zhijian.passport.commands.ModityCorporateRoleCommand;
import cn.zhijian.passport.commands.RoleAddStaffCommand;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.repos.RoleRepository;
import cn.zhijian.passport.session.SessionStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台角色接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/corporate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

	final static Logger logger = LoggerFactory.getLogger(RoleResource.class);
	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final RoleRepository repo;
	final CorporateRoleMapper corporateRoleMapper;
	
	public RoleResource(SessionStore sessionStore,CommandGateway cmdGw,RoleRepository repo,
			CorporateRoleMapper corporateRoleMapper){
		
		this.sessionStore = sessionStore;
		this.cmdGw = cmdGw;
		this.repo = repo;
		this.corporateRoleMapper = corporateRoleMapper;
	}
	
    @ApiOperation("角色详情")
	@GET
	@Path("/role/{id}")
	public Response getRole(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long roleid) {

		Object response = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.repo.GetRoleInfo(ctx.getCurrentCorporate().getId(), roleid,ctx.getCurrentCorporate().getWalletId());
		});
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

    @ApiOperation("角色修改")
	@POST
	@Path("/role/modity")
	public Response roleModify(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, CorporateRole role) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			if (role.getId() == null || role.getId() == 0) {
				return cmdGw.sendAndWait(new CreateCorporateRoleCommand(ctx.getCurrentCorporate().getId(), ctx.getPerson().getUsername(),role));
			} else {
				return cmdGw.sendAndWait(new ModityCorporateRoleCommand(ctx.getCurrentCorporate().getId(), ctx.getPerson().getUsername(),role));
			}
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

    @ApiOperation("角色列表")
	@GET
	@Path("{id}/role/load")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadRole(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, @QueryParam("q") String query, @QueryParam("p") int pageNo,
			@QueryParam("s") int pageSize) {
		PagedResult<CorporateRole> corporateRole = sessionStore.doInSession(sessionId, ctx -> {
			return repo.loadCorporateRole(corpId, query, pageNo, pageSize);
		}, null);
		return Response.ok(corporateRole).type(MediaType.APPLICATION_JSON).build();
	}

    @ApiOperation("角色删除")
	@POST
	@Path("/role/delete/{roleid}")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("roleid") long roleid) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new DeleteCorporateRoleCommand(roleid, ctx.getCurrentCorporate().getId()));
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

    @ApiOperation("员工分配角色")
	@POST
	@Path("{id}/role/{roleId}/add")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response roleAddStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, @PathParam("roleId") long roleId, StaffList list) {
		int isSuccess = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new RoleAddStaffCommand(roleId, corpId, list, ctx.getName()));
		}, null);
		if (isSuccess != 0) {
			return Response.ok(true).build();
		}
		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("role add Staff Failed").build();
	}

    @ApiOperation("角色下员工")
	@POST
	@Path("{id}/role/current")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response currentRole(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, CorporateRole role) {
		List<Staff> list = sessionStore.doInSession(sessionId, ctx -> {
			List<StaffRow> rows = this.corporateRoleMapper.findStaffInnerJoinbyId(corpId, role.getId());
			// List<Staff> staffs = rows.stream().map(_row -> new Staff(_row.getId(), null,
			// personDao.load(_row.getId()).getName(), null, null, null, null, null,
			// null,null,null))
			// .collect(Collectors.toList());
			List<Staff> staffs = new ArrayList<>();
			for (StaffRow staffRow : rows) {
				String name = staffRow.getPersonname();
				Staff s = new Staff(staffRow.getId(), null, name, null, null, null, null, null, null, null, null);
				staffs.add(s);
			}
			return staffs;
		}, null);
		return Response.ok(list).build();
	}
}