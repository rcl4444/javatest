package cn.zhijian.passport.api.team;

import lombok.Data;

@Data
public class TeamMember {

	final Long id;
	final long teamId;
	final long teamMemberId;
	final long personId;
	final String username;
	final String name;
	final String email;
	final String mobile;
	final String avatar;

}
