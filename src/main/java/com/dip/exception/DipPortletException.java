package com.dip.exception;

/**
 * @author HCL
 *
 */
public class DipPortletException extends BaseCheckedException {

	private static final long serialVersionUID = 1L;

	public DipPortletException() {
		super();
	}

	public DipPortletException(String errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	public DipPortletException(Throwable cause) {
		super(cause);
	}

	public DipPortletException(String errorKey, String message, Throwable cause) {
		super(errorKey, message, cause);
	}
	
}
