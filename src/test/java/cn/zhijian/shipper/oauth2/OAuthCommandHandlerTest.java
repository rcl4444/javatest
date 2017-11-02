package cn.zhijian.shipper.oauth2;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import cn.zhijian.passport.commands.OAuthCodeCommand;
import cn.zhijian.passport.api.OAuthCodeRequest;
import cn.zhijian.passport.api.OAuthCodeResponse;
import cn.zhijian.passport.api.OAuthTokenRequire;
import cn.zhijian.passport.api.OAuthTokenResponse;
import cn.zhijian.passport.api.Person;
import cn.zhijian.passport.commands.AuthorizationCodeCommand;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CertificateDao;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CertificateRow;
import cn.zhijian.passport.domain.commandhandlers.OAuthCommandHandler;

public class OAuthCommandHandlerTest {

	@Mock
    private ApplicationDao applicationDaoMock;
    
    @Mock
    private CertificateDao certificateDaoMock;
    
    @Captor 
    private ArgumentCaptor<CertificateRow> captorCertificateRow;
    
    @Captor 
    private ArgumentCaptor<Long> captorLong;
    
    @Rule 
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Rule
    public ExpectedException thrown= ExpectedException.none();
    
    private ApplicationRow applicationRow;
    
    private CertificateRow currCertificateRow;
    
    @Before
    public void initialize() throws ParseException {
    	
    	applicationRow = new ApplicationRow();
    	applicationRow.setId(1L);
    	applicationRow.setAppname("myapp");
    	applicationRow.setClientid("test");
    	applicationRow.setClientsecret("test"); 
    	applicationRow.setScope("api");
    	applicationRow.setCallbackurl("http://localhost:3111/Account/ThirdParty");
    	applicationRow.setCreatedate(DateUtils.parseDateStrictly("2017-08-01 00:00","yyyy-MM-dd HH:mm"));
    	
    	Date currDate = new Date();
    	currCertificateRow = new CertificateRow();
    	currCertificateRow.setId(1L);
    	currCertificateRow.setPersonid(1L);
    	currCertificateRow.setApplicationid(1L);
    	currCertificateRow.setCode(UUID.randomUUID().toString());
    	currCertificateRow.setCodeexpiresdate(DateUtils.addMinutes(currDate, 10));
    	currCertificateRow.setCreatedate(currDate);
    	 
    	when(applicationDaoMock.findByClientid(eq(applicationRow.getClientid()))).thenReturn(applicationRow);
    	when(certificateDaoMock.findByCode(eq(currCertificateRow.getCode()))).thenReturn(currCertificateRow);
    }
    
    private OAuthCodeCommand getOAuthCodeCommand(){
    	
    	Person person = mock(Person.class);
    	when(person.getId()).thenReturn(10L);
    	when(person.getUsername()).thenReturn("test");
    	when(person.getName()).thenReturn("赵日天");
    	when(person.getEmail()).thenReturn("test@test.com");
    	when(person.getMobile()).thenReturn("17525462895");
    	when(person.getRealName()).thenReturn("夯大力");
    	when(person.getSex()).thenReturn(0);
    	when(person.getBirthday()).thenReturn(new Date());
    	when(person.getSchool()).thenReturn("断罪小学");
    	when(person.getQq()).thenReturn("");
    	when(person.getWx()).thenReturn("");
    	OAuthCodeRequest oAuthCodeRequest = mock(OAuthCodeRequest.class);
    	when(oAuthCodeRequest.getClient_id()).thenReturn(applicationRow.getClientid());
    	when(oAuthCodeRequest.getRedirect_uri()).thenReturn(applicationRow.getCallbackurl());
    	when(oAuthCodeRequest.getResponse_type()).thenReturn("code");
    	when(oAuthCodeRequest.getState()).thenReturn("123456");
    	OAuthCodeCommand cmd = mock(OAuthCodeCommand.class);
    	when(cmd.getPerson()).thenReturn(person);
    	when(cmd.getOAuthCodeRequest()).thenReturn(oAuthCodeRequest);
    	return cmd;
    }
    
    private AuthorizationCodeCommand getOAuthTokenCommand(){
    	
    	OAuthTokenRequire oAuthTokenRequire = mock(OAuthTokenRequire.class);
    	when(oAuthTokenRequire.getClient_id()).thenReturn(applicationRow.getClientid());
    	when(oAuthTokenRequire.getClient_secret()).thenReturn(applicationRow.getClientsecret());
    	when(oAuthTokenRequire.getCode()).thenReturn(currCertificateRow.getCode());
    	when(oAuthTokenRequire.getGrant_type()).thenReturn("authorization_code");
    	when(oAuthTokenRequire.getRedirect_uri()).thenReturn(applicationRow.getCallbackurl());
    	AuthorizationCodeCommand cmd = mock(AuthorizationCodeCommand.class);
    	when(cmd.getOAuthTokenRequire()).thenReturn(oAuthTokenRequire);
    	return cmd;
    }
    
    private OAuthCommandHandler getOAuthCommandHandler(){
    	
    	return new OAuthCommandHandler(applicationDaoMock, certificateDaoMock,null,null,null);
    }
    
    //正常逻辑
    @Test
    public void oAuthCode_Test()  {
    	
    	OAuthCodeCommand cmd = this.getOAuthCodeCommand();
    	OAuthCommandHandler handler  = this.getOAuthCommandHandler();
    	OAuthCodeResponse oAuthCodeResponse = handler.oauthCode(cmd);
    	assertNotNull(oAuthCodeResponse);
        assertTrue(!StringUtils.isEmpty(oAuthCodeResponse.getCode()));
        assertEquals(cmd.getOAuthCodeRequest().getState(),oAuthCodeResponse.getState());
        assertEquals(cmd.getOAuthCodeRequest().getRedirect_uri(),oAuthCodeResponse.getCallBackUrl());
        
        verify(certificateDaoMock).insert(captorCertificateRow.capture());
        CertificateRow insertRow  = captorCertificateRow.getValue();
        Long timespan = (insertRow.getCodeexpiresdate().getTime() - insertRow.getCreatedate().getTime())/60000;
        assertEquals(insertRow.getApplicationid(),applicationRow.getId());
        assertEquals(insertRow.getPersonid(),cmd.getPerson().getId());
        assertTrue(timespan.equals(10L));
        assertTrue(!StringUtils.isEmpty(insertRow.getCode()));
    }
    
    //入参为空
    @Test
    public void oAuthCode_NullParam_Test(){
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("cmd can not be null");
        handler.oauthCode(null);        
    }
    
    //入参person属性为空
    @Test
    public void oAuthCode_NullParam_person_Test(){
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("cmd property person can not be null");

    	OAuthCodeCommand cmd = this.getOAuthCodeCommand();
        handler.oauthCode(new OAuthCodeCommand(null,cmd.getOAuthCodeRequest(),1L));        
    }
    
    //入参授权码请求为空
    @Test
    public void oAuthCode_NullParam_oAuthCodeRequest_Test(){
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("cmd property oAuthCodeRequest can not be null");

    	OAuthCodeCommand cmd = this.getOAuthCodeCommand();
        handler.oauthCode(new OAuthCodeCommand(cmd.getPerson(),null,1L));        
    }

    //应用id不存在
    @Test
    public void oAuthCode_ClentUnFind_Test()  {
    	
    	OAuthCommandHandler handler  = this.getOAuthCommandHandler();
    	OAuthCodeCommand cmd = this.getOAuthCodeCommand();
    	when(cmd.getOAuthCodeRequest().getClient_id()).thenReturn("123");
    	OAuthCodeResponse oAuthCodeResponse = handler.oauthCode(cmd);
        assertTrue(oAuthCodeResponse == null);
    }
    
    //回调地址不
    @Test
    public void oAuthCode_RedirectUriUnMatch_Test()  {
    	
    	OAuthCommandHandler handler  = this.getOAuthCommandHandler();
    	OAuthCodeCommand cmd = this.getOAuthCodeCommand();
    	when(cmd.getOAuthCodeRequest().getRedirect_uri()).thenReturn("www.baidu.com");
    	OAuthCodeResponse oAuthCodeResponse = handler.oauthCode(cmd);
        assertTrue(oAuthCodeResponse == null);
    }
    
    //正常逻辑
    @Test 
    public void oAuthToken_Test(){
    	
    	Date currDate = new Date();
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(this.getOAuthTokenCommand());
    	assertNotNull(oAuthTokenResponse);
    	assertEquals(oAuthTokenResponse.getToken_type(),"bearer");
    	assertTrue(oAuthTokenResponse.getExpires_in().equals(1200L));
    	assertTrue(!StringUtils.isEmpty(oAuthTokenResponse.getRefresh_token()));

        verify(certificateDaoMock).update(captorCertificateRow.capture(),captorLong.capture());
        CertificateRow updateRow  = captorCertificateRow.getValue();
        Long id = captorLong.getValue();
        assertTrue(id.equals(currCertificateRow.getId()));
        assertTrue(!StringUtils.isEmpty(updateRow.getToken()));
        assertEquals(updateRow.getApplicationid(),currCertificateRow.getPersonid());
        assertEquals(updateRow.getPersonid(),currCertificateRow.getPersonid());
        assertEquals(updateRow.getCode(),currCertificateRow.getCode());
        assertEquals(updateRow.getCodeexpiresdate(),currCertificateRow.getCodeexpiresdate());
        assertEquals(updateRow.getCreatedate(),currCertificateRow.getCreatedate());
        assertFalse(updateRow.getIsrefresh());
        assertTrue(!StringUtils.isEmpty(updateRow.getRefreshtoken()));
        Long timespan = (updateRow.getTokenexpiresdate().getTime() - currDate.getTime())/60000;
        assertTrue(timespan >= 20L);
    }
    
    //入参为空
    @Test
    public void oAuthToken_NullParam_Test(){
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("param object can not be null");
        handler.oauthToken(null);   
    }
    
    //应用ID不存在
    @Test
    public void oAuthToken_ClientUnFind_Test(){
    	
    	AuthorizationCodeCommand cmd = this.getOAuthTokenCommand();
    	when(cmd.getOAuthTokenRequire().getClient_id()).thenReturn("123");
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(cmd);
    	assertNull(oAuthTokenResponse);
    }
    
    //应用密钥不匹配
    @Test
    public void oAuthToken_ClientSecretUnMatch_Test(){
    	
    	AuthorizationCodeCommand cmd = this.getOAuthTokenCommand();
    	when(cmd.getOAuthTokenRequire().getClient_secret()).thenReturn("123");
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(cmd);
    	assertNull(oAuthTokenResponse);
    }
    
    //回调地址与注册的回调地址不符
    @Test
    public void oAuthToken_RedirectUriUnMatch_Test(){
    	
    	AuthorizationCodeCommand cmd = this.getOAuthTokenCommand();
    	when(cmd.getOAuthTokenRequire().getRedirect_uri()).thenReturn("www.baidu.com");
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse = handler.oauthToken(cmd);
    	assertNull(oAuthTokenResponse);
    }
    
    //授权码不匹配
    @Test
    public void oAuthToken_CodeUnMatch_Test(){
    	
    	AuthorizationCodeCommand cmd = this.getOAuthTokenCommand();
    	when(cmd.getOAuthTokenRequire().getCode()).thenReturn("123");
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(cmd);
    	assertNull(oAuthTokenResponse);
    }

    //令牌已生成测试
    @Test
    public void oAuthToken_TokenCreated_Test(){
    
    	currCertificateRow.setToken(UUID.randomUUID().toString());
    	currCertificateRow.setTokenexpiresdate(DateUtils.addMinutes(currCertificateRow.getCreatedate(), 20));
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(this.getOAuthTokenCommand());
    	assertNull(oAuthTokenResponse);
    }
    
    //授权码过期测试
    @Test
    public void oAuthToken_CodeExpire_Test() throws ParseException{
    	
    	currCertificateRow.setCodeexpiresdate(DateUtils.parseDateStrictly("2017-08-01 00:00","yyyy-MM-dd HH:mm"));
    	
    	OAuthCommandHandler handler = this.getOAuthCommandHandler();
    	OAuthTokenResponse oAuthTokenResponse =  handler.oauthToken(this.getOAuthTokenCommand());
    	assertNull(oAuthTokenResponse);
    }
}
