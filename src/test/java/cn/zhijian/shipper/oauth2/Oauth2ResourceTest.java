package cn.zhijian.shipper.oauth2;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import cn.zhijian.passport.Constants;
import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.passport.api.OAuthCodeResponse;
import cn.zhijian.passport.api.OAuthTokenResponse;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.commands.OAuthCodeCommand;
import cn.zhijian.passport.commands.AuthorizationCodeCommand;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CertificateRow;
import cn.zhijian.passport.resources.Oauth2Resource;
import cn.zhijian.passport.session.SessionStore;


public class Oauth2ResourceTest {

	@Mock
    private SessionStore sessionStoreMock;
    
    @Mock
    private CommandGateway commandGatewayMock;
    
    @Mock
    private ApplicationDao applicationDaoMock;
    
    @Mock 
    private CorporateMapper corporateMapper;
    
    @Rule 
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Rule
    public ExpectedException thrown= ExpectedException.none();
    
    @Captor 
    private ArgumentCaptor<LoginContext> captorLoginContext;
    
    @Captor
    private ArgumentCaptor<String> captorString;
    
    private Oauth2Resource getOauth2Resource(){
    	
    	return new Oauth2Resource(sessionStoreMock,commandGatewayMock,applicationDaoMock,corporateMapper,"http://localhost:8083/",null);
    }
    
    private String sessionId = UUID.randomUUID().toString();
    private String clientsecret = "test";
    private String clientid = "test";
    private String redirecturi = "http://localhost:3111/Account/ThirdParty";
    private String state = "123456";
    
    private String code = UUID.randomUUID().toString();
    private String token = UUID.randomUUID().toString();
    private String refreshtoke = UUID.randomUUID().toString();
    private LoginContext loginContext;
    
    @Before
    public void initialize() throws ParseException {
        
        Person person = new Person(1L,"赵日天","赵老汉","zhao@test.com","14532659874","","赵日天",1,new Date(),"断罪小学","","",0,null,null,null);
        loginContext = new LoginContext( sessionId, person, null, null,null,null);
        
    	when(sessionStoreMock.get(sessionId)).thenReturn(loginContext);
    	ApplicationRow applicationRow = new ApplicationRow();
    	applicationRow.setCallbackurl(redirecturi);
    	applicationRow.setId(1L);
    	applicationRow.setClientid("test");
    	applicationRow.setClientsecret("test");
    	applicationRow.setCreatedate(new Date());
    	applicationRow.setScope("api");
    	when(applicationDaoMock.findByClientid(clientid)).thenReturn(applicationRow);
    	when(commandGatewayMock.sendAndWait(any(OAuthCodeCommand.class))).then(answer->{
    		OAuthCodeCommand cmd = answer.getArgument(0);
    		if(cmd.getPerson() != person){
    			return null;
    		}
    		if( !redirecturi.equals(cmd.getOAuthCodeRequest().getRedirect_uri())){
    			return null;
    		}
    		return new OAuthCodeResponse(code,redirecturi,state);
    	});
    	when(commandGatewayMock.sendAndWait(any(AuthorizationCodeCommand.class))).then(answer->{
    		AuthorizationCodeCommand cmd = answer.getArgument(0);
    		if(cmd == null){
    			return null;
    		}
    		if(!clientid.equals(cmd.getOAuthTokenRequire().getClient_id())){
    			return null;
    		}
    		if(!clientsecret.equals(cmd.getOAuthTokenRequire().getClient_secret())){
    			return null;
    		}
    		if(!code.equals(cmd.getOAuthTokenRequire().getCode())){
    			return null;
    		}
    		if(!"authorization_code".equals(cmd.getOAuthTokenRequire().getGrant_type())){
    			return null;
    		}
    		return new OAuthTokenResponse(token,"bearer",1200L,refreshtoke);
    	});
    }
    
    private URI getLoginURI() throws URISyntaxException{
    	
    	return new URI("http://localhost:8083/");
    }
    
    //正常逻辑
    @Test
    public void authorize_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.authorize(sessionId,clientid,redirecturi,"code",state);
    	assertEquals(response.getStatus(),Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(),new URI(redirecturi + "?code=" + code + "&state=" + state));
    }
    
    //未登陆
    @Test
    public void authorize_ClientIdUnFind_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.authorize(sessionId,"123",redirecturi,"code",state);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(), new URI(redirecturi + "#error=unauthorized_client&state=" + state));
    }
    
    //未登陆
    @Test
    public void authorize_UnLogin_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.authorize("123456789",clientid,redirecturi,"code",state);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(), this.getLoginURI());
    	
    	verify(sessionStoreMock).put(captorString.capture(), captorLoginContext.capture());
        String insertSessionId  = captorString.getValue();
        LoginContext insertLoginContext = captorLoginContext.getValue();
        assertNotEquals( insertSessionId, sessionId);
        assertEquals(insertLoginContext.getSessionId(), insertSessionId);
        assertEquals(insertLoginContext.getPerson(), null);
        assertEquals(insertLoginContext.getCorporates(), null);
        assertEquals(insertLoginContext.getCurrentCorporate(), null);
        assertEquals(insertLoginContext.getOauth().getClient_id(), clientid);
        assertEquals(insertLoginContext.getOauth().getRedirect_uri(), redirecturi);
        assertEquals(insertLoginContext.getOauth().getResponse_type(), "code");
        assertEquals(insertLoginContext.getOauth().getState(), state);
    	//assertEquals(response.getStringHeaders().getFirst("Set-Cookie"),Constants.SESSION_ID_HEADER_NAME + "="+ insertSessionId+"; path=/");
    }
    
    //验证输入回调地址
    @Test
    public void authorize_VerifyRedirectURI_Test() throws Exception{
    
    	Oauth2Resource resource = this.getOauth2Resource();
    	String[] urls = new String[]{
    			redirecturi,
    			"https:www.baidu.com",
    			"http://www.baidu.com?myname=123",
    			"www.baidu.com?myname=123"
    	};
    	String url;
    	Response response = null;
    	for(String item : urls){
    		url = item;
        	response = resource.authorize(sessionId, null, url, "code", state);
        	assertEquals(response.getStatus(),Status.FOUND.getStatusCode());
        	assertEquals(response.getLocation(), new URI(url + "#error=invalid_request&state=" + state));
    	}

    	url = "123456";
    	response = resource.authorize(sessionId, null, url, "code", state);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    }
    
    //回调地址正确,入参为空
    @Test
    public void authorize_ValiedRedirectURI_ParamEmpty_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.authorize(sessionId, null, redirecturi, "code", state);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(), new URI(redirecturi + "#error=invalid_request&state=" + state));
    	
    	response = resource.authorize(sessionId, clientid, redirecturi, null, state);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(), new URI(redirecturi + "#error=invalid_request&state=" + state));
    	
    	response = resource.authorize(sessionId, clientid, redirecturi, "code1", state);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(), new URI(redirecturi + "#error=unsupported_response_type&state=" + state));    	
    	
    	response = resource.authorize(sessionId, clientid, redirecturi, "code", null);
    	assertEquals(response.getStatus(), Status.FOUND.getStatusCode());
    	assertEquals(response.getLocation(),new URI(redirecturi + "?code=" + code));
    }
    
    //回调地址错误,入参为空
    @Test
    public void authorize_UnValiedRedirectURI_ParamEmpty_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.authorize(sessionId, clientid, null,"code",state);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    	
    	response = resource.authorize(sessionId, clientid, null, null, state);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}"); 
    	
    	response = resource.authorize(sessionId, null, null, "code", state);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    }
    
    //正常逻辑
    @Test 
    public void token_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.token(clientid, clientsecret, code, "authorization_code", redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.OK.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(((OAuthTokenResponse)response.getEntity()).getAccess_token(), token);
    }
    
    //入参为空
    @Test
    public void token_ParamEmpty_Test() throws Exception{
    	
    	Oauth2Resource resource = this.getOauth2Resource();
    	Response response = resource.token(null, clientsecret, code, "authorization_code", redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    	
    	response = resource.token(clientid, null, code, "authorization_code", redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    	
    	response = resource.token(clientid, clientsecret, null, "authorization_code", redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    	
    	response = resource.token(clientid, clientsecret, code, null, redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"invalid_request\"}");
    	
    	response = resource.token(clientid, clientsecret, code, "code123", redirecturi,null,null);
    	assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
    	assertEquals(response.getMediaType(),MediaType.APPLICATION_JSON_TYPE);
    	assertEquals(response.getEntity(), "{\"error\":\"unsupported_response_type\"}");
    }
    
    
}
