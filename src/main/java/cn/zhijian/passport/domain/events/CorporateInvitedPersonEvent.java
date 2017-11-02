package cn.zhijian.passport.domain.events;

import java.util.Date;

import lombok.Data;

@Data
public class CorporateInvitedPersonEvent {
	
	final Long Invitedpersonid;
	final Long corporateid;
	final String corporatename;
	final String Invitedpersonname;
	final Date createtime;
	final long beInvitationpersonid;
	final long invitationid;
}
