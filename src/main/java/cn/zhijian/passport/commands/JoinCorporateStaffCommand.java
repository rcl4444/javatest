package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.Corporate;
import cn.zhijian.passport.api.Person;
import lombok.Data;

@Data
public class JoinCorporateStaffCommand {
	final Person person;
	final String corporatename;
	final String corporatemark;
	final String remark;
	final String relName;
}
