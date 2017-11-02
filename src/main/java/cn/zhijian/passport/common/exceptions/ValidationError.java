package cn.zhijian.passport.common.exceptions;

import lombok.Data;

@Data
public class ValidationError {

	final String ref;

	/**
	 * i18n key
	 */
	final String boundKey;

	/**
	 * 描述
	 */
	final String message;
}
