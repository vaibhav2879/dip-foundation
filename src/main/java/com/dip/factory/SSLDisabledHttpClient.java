package com.dip.factory;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.dip.constant.CommonConstant;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;


public final class SSLDisabledHttpClient {	
	public static final int MAX_TIMEOUT_DURATION_MULE_API = GetterUtil
			.getInteger(PropsUtil.get(CommonConstant.MAX_TIMEOUT_DURATION_MULE_API), 0);
	public static final String HOST_NAME = PropsUtil.get(CommonConstant.HOST_NAME);
	public static final int PORT_NUMBER = GetterUtil
			.getInteger(PropsUtil.get(CommonConstant.PORT_NUMBER), 0);
	public static final boolean FLAG_INCREASE_MAX_CONNECTION = GetterUtil.getBoolean(PropsUtil.get(CommonConstant.FLAG_INCREASE_MAX_CONNECTION_PER_ROUTE), false);
	public static final int MAX_TOTAL_CONNECTION =  GetterUtil
			.getInteger(PropsUtil.get(CommonConstant.HTTP_CLIENT_MAX_CONNECTION), 50);
	public static final int HTTP_CLIENT_DEFAULT_PER_ROUTE =  GetterUtil
			.getInteger(PropsUtil.get(CommonConstant.HTTP_CLIENT_DEFAULT_PER_ROUTE), 20);
	
	private static enum Singleton {
		Client;
		private final CloseableHttpClient closeableHttpClient;

		private Singleton() {
			RequestConfig config = RequestConfig.custom().setConnectTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000)
					.setConnectionRequestTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000)
					.setSocketTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000).build();

			SSLSocketFactory sf = buildSSLSocketFactory();

			final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					
					.register("http", new PlainConnectionSocketFactory())
					.register("https", sf)
					.build();

			final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);

			cm.setMaxTotal(MAX_TOTAL_CONNECTION);
			cm.setDefaultMaxPerRoute(HTTP_CLIENT_DEFAULT_PER_ROUTE);
			if(FLAG_INCREASE_MAX_CONNECTION){
				// Increase max connections for localhost:80 to 50
				HttpHost hostapp = new HttpHost(HOST_NAME, PORT_NUMBER);
				cm.setMaxPerRoute(new HttpRoute(hostapp), 50);
			}

			closeableHttpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).build();
		}
/**
 * 
 * @return
 */
		public CloseableHttpClient get() {
			return closeableHttpClient;
		}

	}
	
/**
 * 
 * @return
 */
	public static CloseableHttpClient getClient() {
		// The thread safe client is held by the singleton.
		return Singleton.Client.get();
	}
	
/**
 * 
 * @return
 */
	private static SSLSocketFactory buildSSLSocketFactory() {
		TrustStrategy ts = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true; // heck yea!
			}
		};

		SSLSocketFactory sf = null;

		try {
			/* build socket factory with hostname verification turned off. */
			sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sf;
	}
}
