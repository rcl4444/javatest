package cn.zhijian.passport.domain.events;

import java.util.Date;

import lombok.Data;

@Data
public class PersonJoinCorporateEvent {

	final Long invitationid;
	final Long applypersonid;
	final Long corporateid;
	final String mobile;
	final String personname;
	final Date createtime;
}