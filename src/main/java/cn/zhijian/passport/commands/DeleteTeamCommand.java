package cn.zhijian.passport.commands;

import cn.zhijian.passport.api.team.Team;
import lombok.Data;

@Data
public class DeleteTeamCommand {
	final Team team;
	final long corpId;
}
