package com.dip.handler;

import java.util.HashMap;
import java.util.Map;

public class ApiHandlerResponse {

	private Integer statusCode;

	private byte[] response;
	
	private Map<String,String> headers;

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public byte[] getResponse() {
		return response;
	}

	public void setResponse(byte[] response) {
		this.response = response;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers=headers;
	}
	public Map<String,String> getHeaders(){
		if(null==headers){
			headers=new HashMap<String, String>();
		}
		return headers;
	}
	public String getHeader(String headerName){
		if(null==headers){
			return null;
		}
		return headers.get(headerName);
	}

}
