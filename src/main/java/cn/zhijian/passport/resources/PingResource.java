package cn.zhijian.passport.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cn.zhijian.passport.db.PingMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Test MyBatis Mapper Resource
 * 
 * @author kmtong
 *
 */
@Api(value="验证数据库接口")
@ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
    @ApiResponse(code = 500, message = "服务器内部异常")})
@Path("/ping")
public class PingResource {

	PingMapper mapper;

	public PingResource(PingMapper mapper) {
		this.mapper = mapper;
	}

	@GET
	public String ping() {
		return Integer.toString(mapper.ping());
	}
}
