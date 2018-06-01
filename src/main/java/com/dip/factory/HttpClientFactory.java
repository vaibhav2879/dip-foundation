package com.dip.factory;

import org.apache.http.impl.client.CloseableHttpClient;

public class HttpClientFactory {
/**
 * 
 * @param isSSLEnabled
 * @return
 */
	 public CloseableHttpClient  getHttpClient(boolean isSSLEnabled){
		 //if(isSSLEnabled)
			 return SSLDisabledHttpClient.getClient();
		 //else
			// return SSLDisabledHttpClient.getClient();
		 
	 }
}
