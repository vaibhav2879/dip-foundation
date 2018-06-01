package com.dip.exception.resolver;

import java.util.Date;

public class ExceptionDetails {
	
	private Throwable exception;
	private Date timeStamp;
	private String userId;
	private String secretErrorCode;

	public ExceptionDetails(Throwable exception, Date timeStamp, String userId,
			String secretErrorCode) {
		this.exception = exception;
		this.timeStamp = timeStamp;
		this.userId = userId;
		this.secretErrorCode = secretErrorCode;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getSecretErrorCode() {
		return secretErrorCode;
	}

	public void setSecretErrorCode(String secretErrorCode) {
		this.secretErrorCode = secretErrorCode;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
}
