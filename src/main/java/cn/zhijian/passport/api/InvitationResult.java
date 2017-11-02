package cn.zhijian.passport.api;

import java.util.List;

import lombok.Data;

@Data
public class InvitationResult {

	final List<GenericResult<InvitationResultDetail, String>> results;

}
