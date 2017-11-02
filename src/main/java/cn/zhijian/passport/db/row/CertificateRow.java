package cn.zhijian.passport.db.row;

import java.util.Date;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRow {

	protected Long id;
	protected Long personid;
	protected Long applicationid;
	@Nullable
	protected Long corporateid;
	protected String code;
	protected Date codeexpiresdate;
	protected String token;
	protected Date tokenexpiresdate;
	protected String refreshtoken;
	protected Boolean isrefresh;
	protected Date createdate;
	
}
