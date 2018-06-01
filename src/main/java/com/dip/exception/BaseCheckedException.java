package com.dip.exception;

import com.dip.exception.resolver.ExceptionUtil;

public class BaseCheckedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String errorKey;
	
	private String message;
	
	private String secretCode;

	public String getErrorKey() {
		return errorKey;
	}

	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getSecretCode() {
		return secretCode;
	}

	public void setSecretCode(String secretCode) {
		this.secretCode = secretCode;
	}

	public BaseCheckedException() {
		super();
		this.secretCode = ExceptionUtil.generateSecretErrorCode();
	}

	public BaseCheckedException(Throwable cause) {
		super(cause);
		this.secretCode = ExceptionUtil.generateSecretErrorCode();
	}

	public BaseCheckedException(String errorKey, Throwable cause) {
		super(cause);
		this.errorKey = errorKey;
		this.secretCode = ExceptionUtil.generateSecretErrorCode();
	}

	public BaseCheckedException(String errorKey, String message, Throwable cause) {
		super(cause);
		this.errorKey = errorKey;
		this.message = message;
		this.secretCode = ExceptionUtil.generateSecretErrorCode();
	}
	public BaseCheckedException(String errorKey, String message) {
		super();
		this.errorKey = errorKey;
		this.message = message;
	}
}
