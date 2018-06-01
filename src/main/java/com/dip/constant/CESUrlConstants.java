package com.dip.constant;

public enum CESUrlConstants {

	PURL_KEY_ACTION("action"),
	PURL_KEY_STATE("p_p_state"),
	PURL_KEY_MODE("p_p_mode"),
	PURL_KEY_VALUE("view");
	
	
	private CESUrlConstants(String key) {
		this.key = key;
	}

	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
