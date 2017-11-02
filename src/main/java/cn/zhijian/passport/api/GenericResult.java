package cn.zhijian.passport.api;

import lombok.Data;

@Data
public class GenericResult<V, T> {

	/**
	 * true if success, and message should be empty
	 */
	final boolean success;

	/**
	 * if success is ture, contains the error message
	 */
	final String message;

	/**
	 * if success, the corresponding result
	 */
	final V result;

	/**
	 * if not success, error response
	 */
	final T error;
}
