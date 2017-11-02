package cn.zhijian.passport.db.row;

import lombok.Data;

@Data
public class PersonCorporateView {

	long corporateid;
	String companyType;
	String name;
	String isPending;
	String hsCode;
	String corporateMark;
	String creditCode;
}