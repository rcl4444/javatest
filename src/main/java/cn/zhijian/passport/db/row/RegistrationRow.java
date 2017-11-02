package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.api.Person.passwordStrengthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRow {

	protected String username;
	protected String password;
//	protected String name;
//	protected String email;
	protected String mobile;
	protected String validation_code;
	protected Long personId;
	protected String createdBy;
	protected Date createdAt;
	protected String updatedBy;
	protected Date updatedAt;
	protected passwordStrengthType passwordStrength;
	protected String infoCompletion;
}
