package cn.zhijian.passport.api.team;

import java.util.List;

import cn.zhijian.passport.api.Staff;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TeamStaffResponse {
	 List<TeamData> teams;
	 List<Staff> others;
}
