package cn.zhijian.passport.repos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.row.AdminResourceRow;
import cn.zhijian.passport.api.CorporateEnums;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.PersonDAO;
import cn.zhijian.passport.db.ResourceDAO;
import cn.zhijian.passport.db.row.ApplicationRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.db.row.ResourceRow;

public class HeadInfoRepository {
	final static Logger logger = LoggerFactory.getLogger(HeadInfoRepository.class);

	final PersonDAO personDAO;
	final CorporateMapper corporateMapper;
	final ResourceDAO resourceDAO;
	final CorporateEnums corporateEnums;

	@Inject
	public HeadInfoRepository(PersonDAO personDAO, CorporateMapper corporateMapper, ResourceDAO resourceDAO,
			CorporateEnums corporateEnums) {
		this.personDAO = personDAO;
		this.corporateMapper = corporateMapper;
		this.resourceDAO = resourceDAO;
		this.corporateEnums = corporateEnums;
	}

	private Object convert(PersonRow person, List<CorporateRow> corporates, List<ApplicationRow> applications) {

		Map<String, Object> result = new HashMap<String, Object>();
		ResourceRow resourceRow = resourceDAO.load(person.getAvatarResourceId());
		if(resourceRow!=null && resourceRow.getContent().length >0){
			Map<String,Object> avatarInfo = new HashMap<String,Object>();
			avatarInfo.put("url", resourceRow.getContent());
			avatarInfo.put("data", resourceRow.getContentType());
			result.put("profilePicture", avatarInfo);
		}
		List<Map<String, Object>> corporateInfo = new ArrayList<Map<String, Object>>();
		for (CorporateRow item : corporates) {
			Map<String, Object> corporate = new HashMap<String, Object>();
			corporate.put("name", item.getName());
			corporate.put("id", item.getId());
			corporateInfo.add(corporate);
		}
		result.put("companyList", corporateInfo);
		List<Map<String, Object>> appinfo = new ArrayList<Map<String, Object>>();
		for (ApplicationRow item : applications) {
			Map<String, Object> app = new HashMap<String, Object>();
			app.put("id", item.getId());
			app.put("name", item.getAppname());
			app.put("url", item.getMainurl());
			AdminResourceRow arr = this.resourceDAO.loadApplicationResource(item.getAvatarresourceid());
			if(arr==null || arr.getContent().length ==0)
			{
				app.put("imgdata", null);
			}
			else{
				app.put("imgdata", arr.getContent());
			}
			appinfo.add(app);
		}
		result.put("topApp", appinfo);
		result.put("downApp", null);
		return result;
	}

	public Object loadCorporateHeadInfo(Long personId, Long corporateId) {

		PersonRow person = this.personDAO.load(personId);
		List<CorporateRow> corporates = this.corporateMapper.findCorporatesByPersonId(personId, corporateEnums.getCorporateEnums());
		List<ApplicationRow> applications = this.corporateMapper.findCorporateApplication(corporateId);
		return convert(person, corporates, applications);
	}

	public Object loadPersonHeadInfo(Long personId) {

		PersonRow person = this.personDAO.load(personId);
		List<CorporateRow> corporates = this.corporateMapper.findCorporatesByPersonId(personId, corporateEnums.getCorporateEnums());
		List<ApplicationRow> applications = this.corporateMapper.findPersonApplication(personId);
		return convert(person, corporates, applications);
	}
}
