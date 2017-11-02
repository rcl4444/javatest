package cn.zhijian.passport.api;

import java.util.List;

import lombok.Data;

@Data
public class StaffInfo {

	final String personname;
	final String teamname;
	final List<String> rolenames;
}
