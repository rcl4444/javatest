package cn.zhijian.passport.resourceauth;

import java.util.Optional;

import javax.inject.Inject;

import cn.zhijian.passport.api.AuthorizeInfo;
import cn.zhijian.passport.db.CertificateDao;
import cn.zhijian.passport.db.CorporateStaffMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.row.CertificateRow;
import cn.zhijian.passport.db.row.StaffRow;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class OAuth2Authenticator implements Authenticator<String,AuthorizeInfo> {
	
	private CertificateDao certificateDao;
	
	private CorporateStaffMapper corporateStaffMapper;
	
	
	@Inject
	public OAuth2Authenticator(CertificateDao certificateDao, CorporateStaffMapper corporateStaffMapper){
		this.certificateDao = certificateDao;
		this.corporateStaffMapper = corporateStaffMapper;
	}

	@Override
	public Optional<AuthorizeInfo> authenticate(String credentials) throws AuthenticationException {

		CertificateRow certificateRow = certificateDao.findByToke(credentials);
		if(certificateRow == null){
			return Optional.empty();
		}
		StaffRow row =  corporateStaffMapper.findStaffByPersonId(certificateRow.getPersonid(), certificateRow.getCorporateid());
		AuthorizeInfo info = new AuthorizeInfo(certificateRow.getPersonid(), row.getPersonname(), row.getPersonname(), 
				certificateRow.getCorporateid(), certificateRow.getTokenexpiresdate(),certificateRow.getApplicationid());
		Optional<AuthorizeInfo> result = Optional.of(info);
		return result;
	}
}
