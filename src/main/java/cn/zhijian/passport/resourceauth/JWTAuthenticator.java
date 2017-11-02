package cn.zhijian.passport.resourceauth;

import java.util.Optional;

import org.jose4j.jwt.consumer.JwtContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class JWTAuthenticator implements Authenticator<JwtContext, JWTPrincipal> {

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public Optional<JWTPrincipal> authenticate(JwtContext context) throws AuthenticationException {

    	try {
        	final String subject = context.getJwtClaims().getSubject();
        	JWTPrincipal principal = mapper.readValue(subject, JWTPrincipal.class);
        	if(1==1){
        		return Optional.of(principal);
        	}
        	return Optional.empty();
    	}
    	catch (Exception e) { 
    		return Optional.empty(); 
    	}
	}

}
