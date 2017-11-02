package cn.zhijian.passport.db.row;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrSessionRow {
	
	String sessionid;//会话标识
	Long personid;//会员ID
	byte[] content;//上下文信息
}