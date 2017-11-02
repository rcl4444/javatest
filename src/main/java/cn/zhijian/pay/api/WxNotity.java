package cn.zhijian.pay.api;

import lombok.Data;

@Data
public class WxNotity {
	final String appid;
	final String attach;
	final String bank_type;
	final String cash_fee;
	final String fee_type;
	final String is_subscribe;
	final String mch_id;
	final String nonce_str;
	final String openid;
	final String out_trade_no;
	final String result_code;
	final String return_code;
	final String sign;
	final String time_end;
	final String total_fee;
	final String trade_type;
	final String transaction_id;
}
