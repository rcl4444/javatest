package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.statustype.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRow {
	
	protected Long id;
	protected String appname;
	protected String clientid;
	protected String clientsecret;
	protected String scope;
	protected String callbackurl;
	protected String mainurl;
	protected String getInfoUrl;
	protected Date createdate;
	protected String loginouturl;
	protected String avatarresourceid;
	protected BusinessType type;
}
