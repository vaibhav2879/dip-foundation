package com.dip.exception;

public class SystemServiceException extends BaseCheckedException {

	private static final long serialVersionUID = 1L;

	public SystemServiceException() {
		super();
	}

	public SystemServiceException(String errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	public SystemServiceException(Throwable cause) {
		super(cause);
	}

	public SystemServiceException(String errorKey, String message, Throwable cause) {
		super(errorKey, message, cause);
	}

}
