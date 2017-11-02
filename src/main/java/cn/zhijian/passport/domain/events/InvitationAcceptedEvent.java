package cn.zhijian.passport.domain.events;

import lombok.Data;

@Data
public class InvitationAcceptedEvent {

	final String invitationCode;
	final String validationCode;
	final String email;
	final long personId;
	final long inviterCorpId;
	final long inviterId;
	final String operator;

}
