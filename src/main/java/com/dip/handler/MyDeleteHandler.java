/**
 * 
 */
package com.dip.handler;

import org.apache.http.client.methods.HttpPost;

public class MyDeleteHandler extends HttpPost {

	public MyDeleteHandler(String url) {
		super(url);
	}

	@Override
	public String getMethod() {
		return "DELETE";
	}

}
