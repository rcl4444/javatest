package cn.zhijian.passport.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.Application;
import cn.zhijian.passport.api.ApplicationResponse;
import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.GenericResult;
import cn.zhijian.passport.api.JoinCorporateInfo;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.ResourceID;
import cn.zhijian.passport.api.Staff;
import cn.zhijian.passport.api.StaffList;
import cn.zhijian.passport.api.team.Team;
import cn.zhijian.passport.api.team.TeamStaffResponse;
import cn.zhijian.passport.commands.CancelJoinCorporateCommand;
import cn.zhijian.passport.commands.CorporateCancelCreateCommand;
import cn.zhijian.passport.commands.CorporateCertificationCommand;
import cn.zhijian.passport.commands.CreateCorporateCommand;
import cn.zhijian.passport.commands.CreateTeamCommand;
import cn.zhijian.passport.commands.DeleteTeamCommand;
import cn.zhijian.passport.commands.InviteCorporateStaffCommand;
import cn.zhijian.passport.commands.JoinCorporateAduitCommand;
import cn.zhijian.passport.commands.JoinCorporateStaffCommand;
import cn.zhijian.passport.commands.ModifyCorporateAvatarCommand;
import cn.zhijian.passport.commands.ModifyCorporateCommand;
import cn.zhijian.passport.commands.ModifyStaffCommand;
import cn.zhijian.passport.commands.ModityTeamCommand;
import cn.zhijian.passport.commands.TeamAddStaffCommand;
import cn.zhijian.passport.commands.UnbindStaffCommand;
import cn.zhijian.passport.commands.getTeamStaffCommand;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.StaffRow;
import cn.zhijian.passport.db.row.TeamRow;
import cn.zhijian.passport.dto.AEODto;
import cn.zhijian.passport.dto.AEOInfoDto;
import cn.zhijian.passport.dto.CorporateCertificationDto;
import cn.zhijian.passport.dto.CorporateHomeInfoDto;
import cn.zhijian.passport.dto.CorporateStaffDto;
import cn.zhijian.passport.dto.CorporateStructureInfoDto;
import cn.zhijian.passport.dto.InvitationStaffDto;
import cn.zhijian.passport.dto.WalletInfoDto;
import cn.zhijian.passport.repos.CorporateRepository;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.pay.db.PayMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台企业接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/corporate")
@Produces(MediaType.APPLICATION_JSON)
public class CorporateResource {

	final static Logger logger = LoggerFactory.getLogger(CorporateResource.class);
	ObjectMapper map = new ObjectMapper();
	final SessionStore sessionStore;
	final CommandGateway cmdGw;
	final CorporateRepository repo;
	final PersonDAO personDao;
	final CorporateRoleMapper corporateRoleMapper;
	final PayMapper payMapper;

	@Inject
	public CorporateResource(SessionStore sessionStore, CommandGateway cmdGw, CorporateRepository repo,
			PersonDAO personDao, CorporateRoleMapper corporateRoleMapper, PayMapper payMapper) {
		this.sessionStore = sessionStore;
		this.cmdGw = cmdGw;
		this.repo = repo;
		this.personDao = personDao;
		this.corporateRoleMapper = corporateRoleMapper;
		this.payMapper = payMapper;
	}

	@POST
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Corporate input)
			throws Exception {

		Pair<String, Long> result = sessionStore.doInSession(sessionId, ctx -> {
			Pair<String, Long> r = cmdGw.sendAndWait(
					new CreateCorporateCommand(ctx.getPerson().getId(), ctx.getPerson().getUsername(), input));
			return r;
		});
		if (StringUtils.isEmpty(result.getLeft())) {
			Map<String, Object> map = new HashMap<>();
			map.put("ok", result.getRight());
			return Response.ok(map).build();
		} else {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(result.getLeft()).build();
		}
	}

	@GET
	@Path("{id}")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response load(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, @PathParam("id") long corpId)
			throws Exception {

		Corporate corp = repo.load(sessionId, corpId);
		if (corp != null) {
			return Response.ok(corp).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Corporate not found").build();
		}
	}

	@POST
	@Path("{id}")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Corporate input)
			throws Exception {

		Pair<String, Long> result = sessionStore.doInSession(sessionId, ctx -> {
			Pair<String, Long> r = cmdGw.sendAndWait(
					new ModifyCorporateCommand(ctx.getPerson().getId(), ctx.getPerson().getUsername(), input));
			return r;
		});
		if (StringUtils.isEmpty(result.getLeft())) {
			Map<String, Object> map = new HashMap<>();
			map.put("ok", result.getRight());
			return Response.ok(map).build();
		} else {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(result.getLeft()).build();
		}
	}

	@GET
	@Path("{id}/staff")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, //
			@PathParam("id") long corpId, //
			@QueryParam("accountStatus") Integer state, @QueryParam("role") Integer roleid,
			@QueryParam("q") String query, //
			@QueryParam("p") int pageNo, //
			@QueryParam("s") int pageSize //
	) throws Exception {

		PagedResult<CorporateStaffDto> staffList = this.sessionStore.doInSession(sessionId, ctx -> {
			return repo.loadStaff(ctx.getPerson().getId(), corpId, state, roleid, query, pageNo, pageSize)
					.map(o -> new CorporateStaffDto(o.getStaffid(), o.getStaffpersonname(), o.getSex(), o.getMobile(),
							o.getEmail(), o.getBlocked(),o.getTeamname(),o.getRolename()));
		});
		return Response.ok(staffList).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("{id}/staffInvite")
	public Response loadInviteStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, @QueryParam("q") String query, @QueryParam("status") Integer state,
			@QueryParam("p") int pageNo, @QueryParam("s") int pageSize) {

		PagedResult<InvitationStaffDto> staffList = this.sessionStore.doInSession(sessionId, ctx -> {
			return repo.loadInviteStaff(ctx.getPerson().getId(), corpId, state, query, pageNo, pageSize)
					.map(o -> new InvitationStaffDto(o.getInvitationid(), o.getUsername(), o.getPersonname(),
							o.getMobile(), o.getRemark(), o.getStatus()));
		});
		return Response.ok(staffList).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("{id}/staffApply")
	public Response loadJoinStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, @QueryParam("q") String query, @QueryParam("status") Integer state,
			@QueryParam("p") int pageNo, @QueryParam("s") int pageSize) {

		PagedResult<InvitationStaffDto> staffList = this.sessionStore.doInSession(sessionId, ctx -> {
			return repo.loadJoinStaff(ctx.getPerson().getId(), corpId, state, query, pageNo, pageSize)
					.map(o -> new InvitationStaffDto(o.getInvitationid(), o.getUsername(), o.getPersonname(),
							o.getMobile(), o.getRemark(), o.getStatus()));
		});
		return Response.ok(staffList).type(MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/join")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response corporateJoin(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			JoinCorporateInfo joininfo) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new JoinCorporateStaffCommand(ctx.getPerson(), joininfo.getName(),
					joininfo.getCorporateMark(), joininfo.getMark(), joininfo.getRelName()));
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/staff/edit")
	public Response staffEdit(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Staff staffinfo) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			if (staffinfo.getId() == null) {
				return this.cmdGw.sendAndWait(
						new InviteCorporateStaffCommand(ctx.getPerson().getId(), ctx.getPerson().getRealName(),
								ctx.getPerson().getUsername(), ctx.getCurrentCorporate().getId(), staffinfo));
			} else {
				return this.cmdGw.sendAndWait(new ModifyStaffCommand(staffinfo.getId(),
						ctx.getCurrentCorporate().getId(), staffinfo.getAccountNo(), staffinfo.getRelName(),
						staffinfo.getWorkNo(), staffinfo.getBirthOrigin(), staffinfo.getEmail(), staffinfo.getPhone(),
						staffinfo.getEduBg(), staffinfo.getQualifi(), staffinfo.getStrongPoint(),
						ctx.getPerson().getUsername()));
			}
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/staff/info/{id}")
	public Response getStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long id) {

		Staff staff = this.repo.getStaffById(id);
		if (staff == null) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
					.entity("员工信息不存在").build();
		} else {
			return Response.ok(staff).type(MediaType.APPLICATION_JSON).build();
		}
	}

	@POST
	@Path("/staff/unbind")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response unbindStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Long id) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw.sendAndWait(new UnbindStaffCommand(id));
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/staff/refuse/{id}")
	public Response staffApplyRefuse(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long invitationid) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw
					.sendAndWait(new JoinCorporateAduitCommand(invitationid, false, ctx.getPerson().getUsername()));
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/staff/pass/{id}")
	public Response staffApplyPass(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") Long invitationid) {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw
					.sendAndWait(new JoinCorporateAduitCommand(invitationid, true, ctx.getPerson().getUsername()));
		});
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", result.getLeft());
		response.put("message", result.getRight());
		return Response.ok(response).type(MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/avatar")
	public Response changeAvatar(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, ResourceID rsc) throws Exception {

		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			Pair<Boolean, String> r = cmdGw.sendAndWait(new ModifyCorporateAvatarCommand(rsc.getResourceId(), ctx.getCurrentCorporate().getId()));
			return r;
		});
		if (result.getLeft()) {
			return Response.ok().type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(result.getRight()).build();
		}
	}

	@GET
	@Path("{id}/team")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadTeam(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, //
			@PathParam("id") long corpId, //
			@QueryParam("q") String query, //
			@QueryParam("p") Integer pageNo, //
			@QueryParam("s") Integer pageSize //
	) throws Exception {
		PagedResult<Team> teamList = repo.loadTeam(sessionId, corpId, query, pageNo, pageSize);
		return Response.ok(teamList).type(MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("{id}/team")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTeam(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, //
			@PathParam("id") long corpId, Team team) throws Exception {
		Long teamId = cmdGw.sendAndWait(new CreateTeamCommand(sessionId, corpId, team));
		if (teamId == null) {
			return Response.status(Status.BAD_REQUEST).entity("Not Implemented").type(MediaType.TEXT_PLAIN).build();
		} else {
			return Response.ok().build();
		}
	}

	@POST
	@Path("{id}/team/modity")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTeam(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, Team team) throws Exception {
		int isModity = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new ModityTeamCommand(team, corpId));
		}, null);
		if (isModity != 1) {
			return Response.status(Status.BAD_REQUEST).entity("modity fail").type(MediaType.TEXT_PLAIN).build();
		} else {
			return Response.ok(true).build();
		}
	}

	@POST
	@Path("{id}/team/delete")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTeam(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, Team team) throws Exception {
		try {
			Integer isDelete = sessionStore.doInSession(sessionId, ctx -> {
				return cmdGw.sendAndWait(new DeleteTeamCommand(team, corpId));
			}, null);
			if (isDelete != 1) {
				return Response.status(Status.BAD_REQUEST).entity("该部门下设有人员，无法删除").type(MediaType.TEXT_PLAIN).build();
			} else {
				return Response.ok(true).build();
			}
		} catch (Exception e) {
			// TODO: handle exception
			return Response.status(Status.BAD_REQUEST).entity("delete fail").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@POST
	@Path("{id}/team/current")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response currentTeam(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, Team team) throws Exception {
		List<Staff> list = sessionStore.doInSession(sessionId, ctx -> {
			List<StaffRow> rows = repo.findStaffInnerJoinbyId(corpId, team.getId());
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

	@GET
	@Path("{id}/teamstall")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response teamstall(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId) {
		TeamStaffResponse response = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new getTeamStaffCommand(corpId));
		}, null);
		return Response.ok(response).build();
	}

	@POST
	@Path("{id}/team/{teamid}/add")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response teamAddStaff(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId, @PathParam("teamid") long teamid, StaffList list) {

		long isSuccess = sessionStore.doInSession(sessionId, ctx -> {
			return cmdGw.sendAndWait(new TeamAddStaffCommand(corpId, teamid, list, ctx.getName()));
		}, null);

		if (isSuccess != 0) {
			return Response.ok(true).build();
		}
		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE.withCharset("utf-8"))
				.entity("operation failed").build();
	}

	@GET
	@Path("{id}/application")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getApplication(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId) {
		ApplicationResponse response = sessionStore.doInSession(sessionId, ctx -> {
			List<ApplicationRow> corpOnApplication = repo.CorpOnApplication(corpId);
			List<ApplicationRow> corpNotApplication = repo.CorpNotApplication(corpId);
			List<Application> apps = corpOnApplication.stream()
					.map(_row -> new Application(_row.getId(), _row.getAppname())).collect(Collectors.toList());
			List<Application> other = corpNotApplication.stream()
					.map(_row -> new Application(_row.getId(), _row.getAppname())).collect(Collectors.toList());
			ApplicationResponse list = new ApplicationResponse(apps, other);
			return list;
		}, null);
		return Response.ok(response).build();
	}

	@POST
	@Path("{id}/certification")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response certification(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId, Corporate corp) {
		try {
			Pair<Boolean, String> p = sessionStore.doInSession(sessionId, ctx -> {
				return cmdGw.sendAndWait(
						new CorporateCertificationCommand(corp, ctx.getCurrentCorporate().getContactsName()));
			});
			if (p.getLeft() == true) {
				return Response.ok(new GenericResult<String, String>(p.getLeft(), null, p.getRight(), null)).build();
			} else {
				return Response.ok(new GenericResult<String, String>(p.getLeft(), null, null, p.getRight())).build();
			}
		} catch (Exception e) {
			// TODO: handle exception
			return Response.status(Status.BAD_REQUEST).entity("certification fail").type(MediaType.TEXT_PLAIN).build();
		}

	}

	@GET
	@Path("{id}/load/certification")
	public Response loadCertification(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,
			@PathParam("id") long corpId) {
		CorporateCertificationDto dto = sessionStore.doInSession(sessionId, ctx -> {
			Corporate c = repo.load(corpId);
			return new CorporateCertificationDto(c.getId(), c.getIndustryType(), c.getIndustry(), c.getNature(),
					c.getProvince(), c.getCity(), c.getBusinessLicense(), c.getAddress(), c.getCreditLevel(),
					c.getIsPending());
		});
		return Response.ok(new GenericResult<CorporateCertificationDto, String>(true, null, dto, null)).build();
	}

	@POST
	@Path("getinfo")
	public Response loadInfo(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) throws IOException {
		//获取AOE代码，先写死路径，暂不实现
//		List<ApplicationRow> row = repo.CorpOnApplication(corpId);
//		
//		if(row.size()!=0)
//		{
//			for (ApplicationRow applicationRow : row) {
//				applicationRow.getGetInfoUrl();
//			}
//		}
		
		CorporateHomeInfoDto CorporateHomeInfo = sessionStore.doInSession(sessionId, ctx -> {
			
			List<TeamRow> teamRows = repo.findTeambyId(ctx.getCurrentCorporate().getId());
			List<CorporateRoleRow> corporateRoleRows = repo.findCorporateRolebyCorpId(ctx.getCurrentCorporate().getId());
			Integer staffTotal = repo.countCorporateStaff(ctx.getCurrentCorporate().getId());
			
			CorporateStructureInfoDto corporateStructureInfo = new CorporateStructureInfoDto(teamRows.size(), corporateRoleRows.size(), staffTotal);
			
//			List<ExpensesInfoDto> expensesInfos = new ArrayList<>();
//			List<ExpensesRecordRow>  expensesRecordRows =payMapper.loadTop5ExpensesRecords(ctx.getCurrentCorporate().getWalletId());
//			
//			for (ExpensesRecordRow expensesRecordRow : expensesRecordRows) {
//				ExpensesInfoDto expensesInfo = new ExpensesInfoDto(expensesRecordRow.getCreatedAt(), expensesRecordRow.getBody(), expensesRecordRow.getTotalFee());
//				expensesInfos.add(expensesInfo);
//			}
			
//			ExpensesInfoDto expensesInfo1 = new ExpensesInfoDto(new Date(), "应用A开通12个月", 1200.0);
//			ExpensesInfoDto expensesInfo2 = new ExpensesInfoDto(new Date(), "应用b开通12个月", 1356.0);
//			expensesInfos.add(expensesInfo1);
//			expensesInfos.add(expensesInfo2);
			
			WalletInfoDto wallInfo = new WalletInfoDto(payMapper.findWalletbyId(ctx.getCurrentCorporate().getWalletId()).getBalance(),null);
			String dayData = "";
			String weekData = "";
			String monthData ="";
			try {
				dayData = get("http://192.168.53.3:82/api/public?organize_id="+ctx.getCurrentCorporate().getId()+"&day=1");
				weekData = get("http://192.168.53.3:82/api/public?organize_id="+ctx.getCurrentCorporate().getId()+"&day=7");
				monthData = get("http://192.168.53.3:82/api/public?organize_id="+ctx.getCurrentCorporate().getId()+"&day=30");
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			AEOInfoDto day =null;
			AEOInfoDto week = null;
			AEOInfoDto month = null;
			try {
				day = new ObjectMapper().readValue(dayData, AEOInfoDto.class);
				week = new ObjectMapper().readValue(weekData, AEOInfoDto.class);
				month = new ObjectMapper().readValue(monthData, AEOInfoDto.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AEODto aeoInfo = new AEODto(80,day, week, month);
			return new CorporateHomeInfoDto(corporateStructureInfo, wallInfo, aeoInfo);
		});
	
		return Response.ok(new GenericResult<CorporateHomeInfoDto, String>(true, null, CorporateHomeInfo, null)).build();
	}
	
	public String get(String url) throws IOException
	{
		{
	        String result = "";
	        BufferedReader in = null;
	        try {
	            String urlNameString = url;
	            URL realUrl = new URL(urlNameString);
	            // 打开和URL之间的连接
	            URLConnection connection = realUrl.openConnection();
	            // 设置通用的请求属性
	            connection.setRequestProperty("Authorization",
	                    "Basic 032C807784D74CA6BF50D994823B7B6C69FB1BD5F0724EB89C90DEBC140357EA");
	            // 建立实际的连接
	            connection.connect();
	            // 获取所有响应头字段
	            Map<String, List<String>> map = connection.getHeaderFields();
	            // 遍历所有的响应头字段
	            for (String key : map.keySet()) {
	                System.out.println(key + "--->" + map.get(key));
	            }
	            // 定义 BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream()));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("发送GET请求出现异常！" + e);
	            e.printStackTrace();
	        }
	        // 使用finally块来关闭输入流
	        finally {
	            try {
	                if (in != null) {
	                    in.close();
	                }
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
	        }
	        return result;
	    }
	}

	@GET
	@Path("/delcreate/{corporateid}")
	public Response cancelCreate(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId,@PathParam("corporateid")long corporateid){
		
		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw.sendAndWait(new CorporateCancelCreateCommand(corporateid));
		});
		GenericResult<String,String> responseData = result.getLeft() ? new GenericResult<String,String>(true,"取消完毕",null,null)
				: new GenericResult<String,String>(false,null,null,result.getRight());
		return Response.ok(responseData).type(MediaType.APPLICATION_JSON).build();
	}
	
	@GET
	@Path("/delapply")
	public Response cancelJoinApply(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId){
		
		Pair<Boolean, String> result = this.sessionStore.doInSession(sessionId, ctx -> {
			return this.cmdGw.sendAndWait(new CancelJoinCorporateCommand(ctx.getPerson().getId(),ctx.getPerson().getUsername()));
		});
		GenericResult<String,String> responseData = result.getLeft() ? new GenericResult<String,String>(true,"取消完毕",null,null)
				: new GenericResult<String,String>(false,null,null,result.getRight());
		return Response.ok(responseData).type(MediaType.APPLICATION_JSON).build();
	}
}