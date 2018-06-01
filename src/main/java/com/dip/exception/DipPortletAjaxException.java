package com.dip.exception;

public class DipPortletAjaxException extends BaseCheckedException {

	private static final long serialVersionUID = 1L;
	
	public DipPortletAjaxException() {
		super();
	}

	public DipPortletAjaxException(String errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	public DipPortletAjaxException(Throwable cause) {
		super(cause);
	}
	public DipPortletAjaxException(String errorKey, String message, Throwable cause) {
		super(errorKey, message, cause);
	}

}
