package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRow {

	Long id;
	long inviterCorpId;
	long inviterId;
	Long personId;
	String username;
	String personname;
	String jobnum;//工号
	String residenceaddress;//户籍地址
	String email;//邮箱
	String mobile;//手机号码
	String schoolrecord;//学历
	String qualificationrecord;//资格证书
	String advantage;//能力优势
	Integer invitationType;
	Boolean accepted;
	Date acceptedAt;
	String createdBy;
	Date createdAt;
	String remark;
	Boolean iscancel;//取消状态
}
