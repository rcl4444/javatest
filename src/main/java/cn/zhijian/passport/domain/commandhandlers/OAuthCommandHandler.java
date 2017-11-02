package cn.zhijian.passport.domain.commandhandlers;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandHandler;

import cn.zhijian.passport.commands.OAuthCodeCommand;
import cn.zhijian.passport.commands.OAuthPasswordCommand;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.api.OAuthCodeResponse;
import cn.zhijian.passport.api.OAuthTokenResponse;
import cn.zhijian.passport.commands.AuthorizationCodeCommand;
import cn.zhijian.passport.commands.RefreshTokenCommand;
import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CertificateDao;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CertificateRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.domain.crypto.PasswordEncrypter;
import cn.zhijian.passport.statustype.CorporateEnum;

public class OAuthCommandHandler {
	
	final ApplicationDao applicationDao;
	final CertificateDao certificateDao;
	final CorporateMapper corporateMapper;
	final PersonDAO personDAO;
	final CorporateEnums corporateEnums;

	public OAuthCommandHandler(ApplicationDao applicationDao,CertificateDao certificateDao,
			CorporateMapper corporateMapper,PersonDAO personDAO,CorporateEnums corporateEnums) {
		
		this.applicationDao = applicationDao;
		this.certificateDao = certificateDao;
		this.corporateMapper = corporateMapper;
		this.personDAO = personDAO;
		this.corporateEnums = corporateEnums;
	}
	
	@CommandHandler
	public OAuthCodeResponse oauthCode(OAuthCodeCommand cmd) throws NullPointerException {

		if(cmd == null){
			throw new NullPointerException("cmd can not be null");
		}
		if(cmd.getPerson() == null){
			throw new NullPointerException("cmd property person can not be null");
		}
		if(cmd.getOAuthCodeRequest() == null){
			throw new NullPointerException("cmd property oAuthCodeRequest can not be null");
		}
		ApplicationRow row = applicationDao.findByClientid(cmd.getOAuthCodeRequest().getClient_id());
		if (row != null && row.getCallbackurl().equals(cmd.getOAuthCodeRequest().getRedirect_uri())) {
			String code = UUID.randomUUID().toString();
			long curren = System.currentTimeMillis();
	        Date da = new Date(curren + 10 * 60 * 1000);
			CertificateRow certificate = new CertificateRow();
			certificate.setCorporateid(cmd.getCorporateid());
			certificate.setApplicationid(row.getId());
			certificate.setPersonid(cmd.getPerson().getId());
			certificate.setCode(code);
			certificate.setCodeexpiresdate(da);
			certificate.setCreatedate(new Date(curren));
			certificateDao.insert(certificate);
			return new OAuthCodeResponse(code,row.getCallbackurl(),cmd.getOAuthCodeRequest().getState());
		}
		return null;
	}
	
	@CommandHandler
	public OAuthTokenResponse oauthToken(AuthorizationCodeCommand cmd) {

		if(cmd == null || cmd.getOAuthTokenRequire() == null){
			throw new NullPointerException("param object can not be null");
		}
		ApplicationRow application = applicationDao.findByClientid(cmd.getOAuthTokenRequire().getClient_id());
		if(application == null || !application.getClientsecret().equals(cmd.getOAuthTokenRequire().getClient_secret())){
			return null;
		}
		if(!application.getCallbackurl().equals(cmd.getOAuthTokenRequire().getRedirect_uri()))
		{
			return null;
		}
		Date currDate = new Date();
		CertificateRow row = certificateDao.findByCode(cmd.getOAuthTokenRequire().getCode());
		if (row == null || !StringUtils.isEmpty(row.getToken()) || (row.getCodeexpiresdate().getTime() < currDate.getTime())) {
			return null;
		}
		String token = UUID.randomUUID().toString();
		String refreshtoke = UUID.randomUUID().toString();
		long curren = System.currentTimeMillis();
        Date da = new Date(curren + 20 * 60 * 1000);
		row.setToken(token);
		row.setTokenexpiresdate(da);
		row.setRefreshtoken(refreshtoke);
		row.setIsrefresh(false);
		certificateDao.update(row,row.getId());
		return new OAuthTokenResponse(token,"bearer",1200L,refreshtoke);
	}
	
	@CommandHandler
	public OAuthTokenResponse clientLogin(OAuthPasswordCommand cmd){
		
		ApplicationRow application = applicationDao.findByClientid(cmd.getClientid());
		if(application == null || !application.getClientsecret().equals(cmd.getClientsecret())){
			return null;
		}
		PersonRow person = this.personDAO.findPersonByLogin(cmd.getUsername(), PasswordEncrypter.encrypt(cmd.getPassword()));
		if(person == null){
			return null;
		}
		
		List<CorporateRow> corporates = this.corporateMapper.findCorporatesByPersonId(person.getId(),corporateEnums.getCorporateEnums());
		Date currDate = new Date();
		CertificateRow newRow = new CertificateRow();
		long curren = System.currentTimeMillis();
        Date da = new Date(curren + 20 * 60 * 1000);
        newRow.setToken(UUID.randomUUID().toString());
        newRow.setRefreshtoken(UUID.randomUUID().toString());
        newRow.setIsrefresh(false);
        newRow.setTokenexpiresdate(da);
        newRow.setCreatedate(currDate);
        newRow.setPersonid(person.getId());
        newRow.setApplicationid(application.getId());
        if(corporates!=null && corporates.size() > 0){
            newRow.setCorporateid(corporates.get(0).getId());	
        }
		certificateDao.insert(newRow);
		return new OAuthTokenResponse(newRow.getToken(),"bearer",1200L,newRow.getRefreshtoken());
	}
	
	@CommandHandler
	public OAuthTokenResponse refreshToken(RefreshTokenCommand cmd) {
		
		if(cmd == null){
			throw new NullPointerException("param object can not be null");
		}
		Date currDate = new Date();
		CertificateRow row = certificateDao.findByRefreshToke(cmd.getRefreshtoken());
		if (row == null || row.getIsrefresh() ||!row.getPersonid().equals(cmd.getPersonID())) {
			return null;
		}
		row.setIsrefresh(true);
		certificateDao.update(row,row.getId());
		CertificateRow newRow = new CertificateRow();
		long curren = System.currentTimeMillis();
        Date da = new Date(curren + 20 * 60 * 1000);
        newRow.setToken(UUID.randomUUID().toString());
        newRow.setRefreshtoken(UUID.randomUUID().toString());
        newRow.setIsrefresh(false);
        newRow.setTokenexpiresdate(da);
        newRow.setCreatedate(currDate);
        newRow.setCorporateid(row.getCorporateid());
		certificateDao.insert(newRow);
		return new OAuthTokenResponse(newRow.getToken(),"bearer",1200L,newRow.getRefreshtoken());
	}
}
