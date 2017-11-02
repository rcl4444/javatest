package cn.zhijian.passport.db.row;

import java.util.Date;

import cn.zhijian.passport.api.Person.passwordStrengthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonRow {

	protected Long id;
	protected String username;
	protected String password;
	protected String name;
	protected String email;
	protected String mobile;
	protected String avatarResourceId;
	protected String realName;
	protected Integer sex;
	protected Date birthday;
	protected String school;
	protected String qq;
	protected String wx;
	protected String createdBy;
	protected Date createdAt;
	protected String updatedBy;
	protected Date updatedAt;
	protected Integer isBindingEmail;
	protected passwordStrengthType passwordStrength;
	protected String infoCompletion;
	protected String walletId;
}
