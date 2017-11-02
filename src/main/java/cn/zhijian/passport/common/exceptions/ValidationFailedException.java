package cn.zhijian.passport.common.exceptions;

import java.util.List;

/**
 * 
 * @author zouqingmin
 *
 */
public class ValidationFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961909267062907809L;

	public List<ValidationError> getErrors() {
		return errors;
	}

	final List<ValidationError> errors;

	public ValidationFailedException(String arg0, List<ValidationError> errors) {
		super(arg0);
		this.errors = errors;
	}

}
