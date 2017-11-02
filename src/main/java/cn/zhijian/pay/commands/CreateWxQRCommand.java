package cn.zhijian.pay.commands;

import javax.servlet.http.HttpServletRequest;

import cn.zhijian.passport.api.LoginContext;
import cn.zhijian.pay.api.Pay;
import lombok.Data;

@Data
public class CreateWxQRCommand {
	final Pay pay;
	final HttpServletRequest request;
	final LoginContext context;
}
