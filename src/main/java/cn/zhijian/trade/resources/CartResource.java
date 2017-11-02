package cn.zhijian.trade.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import cn.zhijian.passport.Constants;
import io.swagger.annotations.Api;


@Path("/cart")
public class CartResource {
	
	public CartResource() {
		// TODO Auto-generated constructor stub
		
	}
	
	@GET
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getList(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) {
		

		return null;
	}
	
	@POST
	@Path("/join")
	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	public Response joinCart(@HeaderParam(Constants.SESSION_ID_HEADER_NAME) String sessionId) {
		return null;
	}
}
