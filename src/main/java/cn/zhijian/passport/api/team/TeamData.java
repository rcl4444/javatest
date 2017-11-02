package cn.zhijian.passport.api.team;

import java.util.List;

import cn.zhijian.passport.api.Staff;
import lombok.Data;

@Data
public class TeamData {
	
	 Team team;
	
	 List<Staff> staffs;
}
