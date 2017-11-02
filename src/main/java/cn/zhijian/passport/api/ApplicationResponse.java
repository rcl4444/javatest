package cn.zhijian.passport.api;

import java.util.List;

import lombok.Data;

@Data
public class ApplicationResponse {
	final List<Application> applications;
	final List<Application> other;
}
