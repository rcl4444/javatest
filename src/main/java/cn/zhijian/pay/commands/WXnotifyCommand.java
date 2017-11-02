package cn.zhijian.pay.commands;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;

@Data
public class WXnotifyCommand {
	
	final HttpServletRequest request;
	final HttpServletResponse response;
}
