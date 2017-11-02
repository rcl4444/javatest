package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendBindingEmailRow {
	protected Long id;
	protected Long personId;
	protected String bindingCode;
	protected String email;
	protected String createdBy;
	protected Date createdAt;
	
}
