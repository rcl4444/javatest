package cn.zhijian.passport.resources;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.converters.CorporateConverter;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.repos.HeadInfoRepository;
import cn.zhijian.passport.session.SessionStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="前台公共头接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 400, message = "错误的请求"),
    @ApiResponse(code = 401, message = "权限不足"),
    @ApiResponse(code = 422, message = "输入验证失败"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/foundation")
public class FoundationResource {

	final static Logger logger = LoggerFactory.getLogger(FoundationResource.class);
	
	ObjectMapper mapper = new ObjectMapper(); 

	final SessionStore sessionStore;
	final HeadInfoRepository hiRepository;
	final CorporateMapper corporateMapper;

	@Inject
	public FoundationResource(SessionStore sessionStore, HeadInfoRepository hiRepository,CorporateMapper corporateMapper) {
		this.sessionStore = sessionStore;
		this.hiRepository = hiRepository;
		this.corporateMapper = corporateMapper;
	}
	
	@GET
	@Path("/head")
	public Response getCorporateHeadInfo(@ApiParam(hidden = true)@CookieParam(Constants.Session_ID_Cookie_Name) String sessionId) throws JsonProcessingException{
		
		Object result = this.sessionStore.doInSession(sessionId, ctx->{
			Object r = this.hiRepository.loadCorporateHeadInfo(ctx.getPerson().getId(), ctx.getCurrentCorporate().getId());
			return r;
		});
		return Response.ok(mapper.writeValueAsString(result)).build();
	}
	
	@GET
	@Path("/myhead")
	public Response getPersonHeadInfo(@ApiParam(hidden = true)@CookieParam(Constants.Session_ID_Cookie_Name) String sessionId) throws JsonProcessingException{
		
		Object result = this.sessionStore.doInSession(sessionId, ctx->{
			return this.hiRepository.loadPersonHeadInfo(ctx.getPerson().getId());
		});
		return Response.ok(mapper.writeValueAsString(result)).build();
	}
	
	@GET
	@Path("/loginout")
	public Response loginOut(@ApiParam(hidden = true)@CookieParam(Constants.Session_ID_Cookie_Name) String sessionId){
		
		if(!StringUtils.isEmpty(sessionId)){
			this.sessionStore.remove(sessionId);	
		}
		return Response.ok().build();
	}
	
	private Corporate convertCorpRow(CorporateRow row) {
		return CorporateConverter.convertRow(row);
	}
}
