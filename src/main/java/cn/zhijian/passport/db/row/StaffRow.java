package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffRow {

	Long id;
	long personId;
	long corporateId;
	String personname;
	String jobnum;//工号
	String residenceaddress;//户籍地址
	String email;//邮箱
	String mobile;//手机号码
	String schoolrecord;//学历
	String qualificationrecord;//资格证书
	String advantage;//能力优势
	String role;
	String createdBy;
	Date createdAt;
	String updatedBy;
	Date updatedAt;
	boolean blocked;
	Integer sex;
	
	public StaffRow(long personId, long corporateId, String role, String createdBy){
	
		Date currDate = new Date();
		this.personId = personId;
		this.corporateId = corporateId;
		this.role = role;
		this.createdBy = createdBy;
		this.createdAt = currDate;
	}
}
