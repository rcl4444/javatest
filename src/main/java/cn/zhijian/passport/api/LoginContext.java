package cn.zhijian.passport.api;

import java.security.Principal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class LoginContext implements Principal {

	final String sessionId;
	final Person person;
	final Corporate currentCorporate;
	final List<Corporate> corporates;
	final StaffInfo currStaff;
	public OAuthCodeRequest oauth;
	final List<String> modules;
	

	@JsonIgnore
	@Override
	public String getName() {
		return person != null ? person.getUsername() : null;
	}

}
