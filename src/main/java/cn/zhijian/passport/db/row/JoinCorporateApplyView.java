package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.Data;

@Data
public class JoinCorporateApplyView {

	long corporateid;
	String corporatename;
	Boolean accepted;
	String hscode;
	String corporatemark;
	String creditcode;
	Date applydate;
}