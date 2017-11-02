package cn.zhijian.passport.commands;

import javax.annotation.Nullable;

import cn.zhijian.passport.api.OAuthCodeRequest;
import cn.zhijian.passport.api.Person;
import lombok.Data;

@Data
public class OAuthCodeCommand {
	final Person person;
	final OAuthCodeRequest oAuthCodeRequest;
	@Nullable
	final Long corporateid;
}
