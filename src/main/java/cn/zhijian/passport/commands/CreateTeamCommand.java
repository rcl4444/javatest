package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.team.Team;
import lombok.Data;

@Data
public class CreateTeamCommand {

	final String sessionId;
	final long corporateId;
	final Team team;

}
