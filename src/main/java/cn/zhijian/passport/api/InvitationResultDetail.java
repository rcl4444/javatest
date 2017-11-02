package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class InvitationResultDetail {

	final String invitationCode;
	final PersonType type;
	final String email;
	final Long personId;

	public static enum PersonType {
		INTERNAL, EXTERNAL
	}

}
