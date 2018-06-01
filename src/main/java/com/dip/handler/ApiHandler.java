package com.dip.handler;

import com.dip.constant.CommonConstant;
import com.dip.exception.SystemServiceException;
import com.dip.exception.DipPortletException;
import com.dip.factory.HttpClientFactory;
import com.dip.util.CommonUtil;
import com.dip.util.ErrorCodes;
import com.dip.util.LoggingWrapper;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

/**
 * @author HCL
 *
 */
public class ApiHandler {

	private static final String SUCCESS = "true";
	private static final String STATUS = "success";
	private static Log log = LogFactoryUtil.getLog(ApiHandler.class);
	private String CLASS_NAME = ApiHandler.class.getName();
	public static final int MAX_TIMEOUT_DURATION_MULE_API = GetterUtil
			.getInteger(PropsUtil.get(CommonConstant.MAX_TIMEOUT_DURATION_MULE_API), 0);

	private static final String SSWS = "SSWS ";
	private static final String TIMEZONE_GMT = "GMT";
	private static final String CONNECTION_CLOSE = "close";

	/**
	 * 
	 * @param sslSettingEnable
	 * @return
	 * @throws DipPortletException
	 */
	private CloseableHttpClient getHttpClient(boolean sslSettingEnable) throws DipPortletException {

		String signature = CLASS_NAME + "#getHttpClient(boolean sslSettingEnable)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "sslSettingEnable" },
				new Object[] { sslSettingEnable }, null, null);
		CloseableHttpClient httpClient = null;
		HttpClientFactory hcf = new HttpClientFactory();
        httpClient = hcf.getHttpClient(sslSettingEnable);
		LoggingWrapper.logExit(log, signature, new Object[] { httpClient }, entranceTimestamp);
		return httpClient;
	}

	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object getDataFromNonMuleAPI(String apiURL, String action, String CESTag)
			throws DipPortletException, SystemServiceException {

		String signature = CLASS_NAME + "#getDataFromNonMuleAPI(String apiURL, String action, String CESTag)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "action", "CESTag" }, new Object[] { apiURL, action, CESTag }, null, null);
		Object response = null;
		HttpGet getMethod = null;
		if(apiURL.contains("okta")){
			getMethod = setHttpGetHeader(apiURL, action, CESTag);
		}else{
			getMethod = setHttpNonMuleGetHeader(apiURL, action, CESTag);
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);
			
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + ":" + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}finally{
			if(getMethod!=null)
				getMethod.releaseConnection();
			
			try{
				if(null != httpClient){
					if(log.isDebugEnabled()){
						log.debug("closing the httpclient");
					}
					//httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			}catch(Exception exception){
				log.error("closing the httpclient" + exception);
				
			}
		}
		if (response != null && response instanceof JSONObject) {
			JSONObject jsonResponse = (JSONObject) response;
			checkResponseStatus(signature, jsonResponse);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return response;
	}

	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object getDataFromAPI(String apiURL, String action, String CESTag)
			throws DipPortletException, SystemServiceException {

		String signature = CLASS_NAME + "#getDataFromAPI(String apiURL, String action, String CESTag)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "action", "CESTag" }, new Object[] { apiURL, action, CESTag }, null, null);

		Object response = null;
		HttpGet getMethod = setHttpGetHeader(apiURL, action, CESTag);
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + StringPool.COLON + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}finally{
			if(getMethod!=null)
				getMethod.releaseConnection();
			    
			
			try{
				if(null != httpClient){
					if(log.isDebugEnabled()){
						log.debug("closing the httpclient");
					}
					//httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			}catch(Exception exception){
				log.error("closing the httpclient" + exception);
				
			}
		}
		
		if (response != null && response instanceof JSONObject) {
			JSONObject jsonResponse = (JSONObject) response;
			checkResponseStatus(signature, jsonResponse);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return response;
	}
	
	public static String getUUID(){
		UUID guid = UUID.randomUUID();
		return guid.toString();
	}
	
	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 */
	private HttpGet setHttpGetHeaderDashboard(String apiURL, String action, String CESTag, String userEmail) {

		HttpGet getMethod = new HttpGet(apiURL);
		getMethod.setHeader("Content-Type", CommonConstant.APPLICATION_JSON);
		getMethod.setHeader("callId", getUUID());
		getMethod.setHeader("channel", CESTag);
		getMethod.setHeader("x-myDip-identity", userEmail);
		return getMethod;
	}
	
	
	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object getDataForDashboardAPI(String apiURL, String action, String CESTag, String userEmail)
			throws DipPortletException, SystemServiceException {

		String signature = CLASS_NAME + "#getDataFromNonMuleAPI(String apiURL, String action, String CESTag)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "action", "CESTag" }, new Object[] { apiURL, action, CESTag }, null, null);
		Object response = null;
		HttpGet getMethod = setHttpGetHeaderDashboard(apiURL, action, CESTag, userEmail);
		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);
			
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + ":" + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}finally{
			if(getMethod!=null)
				getMethod.releaseConnection();
			
			try{
				if(null != httpClient){
					if(log.isDebugEnabled()){
						log.debug("closing the httpclient");
					}
					//httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			}catch(Exception exception){
				log.error("closing the httpclient" + exception);
				
			}
		}
		if (response != null && response instanceof JSONObject) {
			JSONObject jsonResponse = (JSONObject) response;
			checkResponseStatus(signature, jsonResponse);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return response;
	}
	
	/**
	 * 
	 * @param apiURL
	 * @param requestArray
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject getPostDataFromNonMuleAPI(String apiURL, byte[]  requestArray, String action, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromNonMuleAPI(String apiURL, String request, String action, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "action", "CESTag" },
				new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		HttpPost postMethod = new HttpPost(apiURL);
		setHttpPostHeader(action, CESTag, postMethod);
		try {
			postMethod.setEntity(new ByteArrayEntity(requestArray));
			           

		} catch (Exception e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		
		
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	/**
	 * 
	 * @param apiURL
	 * @param request
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject getPostDataFromNonMuleAPI(String apiURL, String request, String action, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromNonMuleAPI(String apiURL, String request, String action, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "action", "test" },
				new Object[] { apiURL, request, action, CESTag }, null, null);
		// Check to prevent logging of user credentials
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, request, action, CESTag }, null, null);
		} else {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		}*/

		HttpPost postMethod = new HttpPost(apiURL);
		setHttpNonMulePostHeader(action, CESTag, postMethod);
		try {
			postMethod.setEntity(new StringEntity(request));
		} catch (UnsupportedEncodingException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			log.debug("Service body/post : " + request);
		}*/
		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		
		
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	/**
	 *
	 * @param apiURL
	 * @param request
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject getPostDataFromAPI(String apiURL, String request, String action, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromAPI(String apiURL, String request, String action, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "action", "CESTag" },
				new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		// Check to prevent logging of user credentials
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, request, action, CESTag }, null, null);
		} else {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		}*/

		HttpPost postMethod = new HttpPost(apiURL);
		setHttpPostHeader(action, CESTag, postMethod);
		try {
			postMethod.setEntity(new StringEntity(request));
		} catch (UnsupportedEncodingException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			log.debug("Service body/post : " + request);
		}*/

		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
	
	/**
	 * 
	 * @param apiURL
	 * @param request
	 * @param action
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject getPostDataFromAPI(String apiURL, byte[] request, String action, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromAPI(String apiURL, String request, String action, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "action", "CESTag" },
				new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		// Check to prevent logging of user credentials
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, request, action, CESTag }, null, null);
		} else {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "action", "CESTag" },
					new Object[] { apiURL, StringPool.BLANK, action, CESTag }, null, null);
		}*/

		HttpPost postMethod = new HttpPost(apiURL);
		setHttpPostHeader(action, CESTag, postMethod);
		try {
			postMethod.setEntity(new ByteArrayEntity(request));
		} catch (Exception e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		/*if (!action.equalsIgnoreCase(CommonConstant.LOGIN_CLICK)) {
			log.debug("Service body/post : " + request);
		}*/

		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
/**
 * 
 * @param apiURL
 * @param actionName
 * @param CESTag
 * @return
 * @throws DipPortletException
 * @throws SystemServiceException
 */
	public JSONObject deleteDataFromAPI(String apiURL, String actionName, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME + "#deleteDataFromAPI(String apiURL, String actionName, String CESTag)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "actionName", "CESTag" }, new Object[] { apiURL, actionName, CESTag }, null,
				null);

		HttpDelete deleteMethod = setHttpDeleteHeader(apiURL, actionName, CESTag);
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, deleteMethod, httpClient);

		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
/**
 * 
 * @param apiURL
 * @param actionName
 * @param CESTag
 * @return
 * @throws DipPortletException
 * @throws SystemServiceException
 */
	public JSONObject deleteDataFromNonMuleAPI(String apiURL, String actionName, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME + "#deleteDataFromNonMuleAPI(String apiURL, String actionName, String CESTag)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "actionName", "CESTag" }, new Object[] { apiURL, actionName, CESTag }, null,
				null);

		HttpDelete deleteMethod = setHttpDeleteHeader(apiURL, actionName, CESTag);
		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		JSONObject jsonResponse = execute(signature, apiURL, deleteMethod, httpClient);

		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	/**
	 * 
	 * @param apiURL
	 * @param request
	 * @param actionName
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject putDataToNonMuleAPI(String apiURL, String request, String actionName, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#putDataToNonMuleAPI(String apiURL, String request, String actionName, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "actionName", "CESTag" },
				new Object[] { apiURL, StringPool.BLANK, actionName, CESTag }, null, null);
		// Check to prevent logging of user credentials
		/*if (!actionName.equalsIgnoreCase(CommonConstant.UPDATE_PASSWORD_CLICK)) {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "actionName", "CESTag" },
					new Object[] { apiURL, request, actionName, CESTag }, null, null);
		} else {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "actionName", "CESTag" },
					new Object[] { apiURL, StringPool.BLANK, actionName, CESTag }, null, null);
		}*/
		HttpPut putMethod = setHttpPutHeader(apiURL, actionName, CESTag);
		try {
			putMethod.setEntity(new StringEntity(request));
		} catch (UnsupportedEncodingException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}

		// Check to prevent logging of user credentials
		/*if (!actionName.equalsIgnoreCase(CommonConstant.UPDATE_PASSWORD_CLICK)) {
			log.debug("service body/put : " + request);
		}*/

		CloseableHttpClient httpClient = getHttpClient(Boolean.FALSE);
		JSONObject jsonResponse = execute(signature, apiURL, putMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	/**
	 * 
	 * @param apiURL
	 * @param request
	 * @param actionName
	 * @param CESTag
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject putDataToAPI(String apiURL, String request, String actionName, String CESTag)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#putDataToAPI(String apiURL, String request, String actionName, String CESTag)";
		Date entranceTimestamp;
		entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "request", "action", "test" },
				new Object[] { apiURL, request, actionName, CESTag }, null, null);
		// Check to prevent logging of user credentials
		/*if (!actionName.equalsIgnoreCase(CommonConstant.UPDATE_PASSWORD_CLICK)) {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "actionName", "CESTag" },
					new Object[] { apiURL, request, actionName, CESTag }, null, null);
		} else {
			entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
					new String[] { "apiURL", "request", "actionName", "CESTag" },
					new Object[] { apiURL, StringPool.BLANK, actionName, CESTag }, null, null);
		}*/
		HttpPut putMethod = setHttpPutHeader(apiURL, actionName, CESTag);
		try {
			putMethod.setEntity(new StringEntity(request));
		} catch (UnsupportedEncodingException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}

		// Check to prevent logging of user credentials
		/*if (!actionName.equalsIgnoreCase(CommonConstant.UPDATE_PASSWORD_CLICK)) {
			log.debug("service body/put : " + request);
		}*/

		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, putMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	/**
	 * 
	 * @param apiURL
	 * @param httpClient
	 * @param httpMethod
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 */
	private Object executeGet(String apiURL, CloseableHttpClient httpClient, HttpRequestBase httpMethod)
			throws IOException, ClientProtocolException, JSONException {

		Object jsonResponse;
		log.debug("Service URL : " + apiURL);
		HttpCoreContext localContext = new HttpCoreContext();
		CloseableHttpResponse httpResponse = httpClient.execute(httpMethod, localContext);
		String response = EntityUtils.toString(httpResponse.getEntity());
		log.trace("REST Response from : " + apiURL.substring(apiURL.lastIndexOf(StringPool.FORWARD_SLASH)) + StringPool.SPACE + StringPool.COLON + StringPool.SPACE
				+ response);

		if (response.startsWith(StringPool.OPEN_BRACKET)) 
			jsonResponse = JSONFactoryUtil.createJSONArray(response);
		
		else  if(response.startsWith(StringPool.OPEN_CURLY_BRACE))
			jsonResponse = JSONFactoryUtil.createJSONObject(response);
		
		else {
			JSONObject obj=null;
			try{
			 obj = JSONFactoryUtil.createJSONObject();
			 obj.put("Success",response);
			System.out.println("obj "+obj.toJSONString());
			System.out.println("obj "+obj.toString());
			}catch(Exception e){
				
				e.printStackTrace();
			}
			
			
			jsonResponse = JSONFactoryUtil.createJSONObject(obj.toString());
			
			
		} 

		log.debug(" jsonResponse : " + jsonResponse.toString());
	
		try{
			
			if(log.isDebugEnabled()){
				log.debug("closing the httpresponse");
			}
			
			if(null!=httpResponse)
				httpResponse.close();
			
		}catch(Exception exception){
			log.warn("error closing response" + exception );
			
		}
		return jsonResponse;
	}

	private Object executeMethod(String signature, String apiURL, HttpRequestBase httpMethod,
			CloseableHttpClient httpClient) throws SystemServiceException, DipPortletException {

		Object jsonResponse = null;
		String response = StringPool.BLANK;
		log.debug("Service URL : " + apiURL);
		HttpCoreContext localContext = new HttpCoreContext();
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpMethod, localContext);
			System.out.println("HTTP Response" + httpResponse.getStatusLine().getStatusCode());
			HttpEntity httpEntity = httpResponse.getEntity();
			if (Validator.isNotNull(httpEntity)) {
				response = EntityUtils.toString(httpEntity);
			}
			log.trace("REST Response from : " + apiURL.substring(apiURL.lastIndexOf(StringPool.FORWARD_SLASH))
					+ StringPool.SPACE + StringPool.COLON + StringPool.SPACE + response);
			if (response.startsWith(StringPool.OPEN_BRACKET))
				jsonResponse = JSONFactoryUtil.createJSONArray(response);

			else if (response.startsWith(StringPool.OPEN_CURLY_BRACE))
				jsonResponse = JSONFactoryUtil.createJSONObject(response);

			else {
				JSONObject obj = null;
				try {
					obj = JSONFactoryUtil.createJSONObject();
					obj.put("Success", response);
					System.out.println("obj " + obj.toJSONString());
					System.out.println("obj " + obj.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				jsonResponse = JSONFactoryUtil.createJSONObject(obj.toString());
			}
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} finally {
			if (httpMethod != null)
				httpMethod.releaseConnection();
			try {
				if (null != httpResponse) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpresponse");
					}
					httpResponse.close();
				}
				if (null != httpClient) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpclient");
					}
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("closing the httpclient" + exception);
			}
		}
		return jsonResponse;
	}

	/**
	 * 
	 * @param signature
	 * @param apiURL
	 * @param httpMethod
	 * @param httpClient
	 * @return
	 * @throws SystemServiceException
	 * @throws DipPortletException
	 */
	private JSONObject execute(String signature, String apiURL, HttpRequestBase httpMethod,
			CloseableHttpClient httpClient) throws SystemServiceException, DipPortletException {
		Object jsonResponse;
		//JSONObject jsonResponse = null;
		String response = StringPool.BLANK;
		log.debug("Service URL : " + apiURL);
		HttpCoreContext localContext = new HttpCoreContext();
		CloseableHttpResponse httpResponse = null;
		try {
			 httpResponse = httpClient.execute(httpMethod, localContext);
			 System.out.println("HTTP Response"+httpResponse.getStatusLine().getStatusCode());
			HttpEntity httpEntity = httpResponse.getEntity();
			if (Validator.isNotNull(httpEntity)) {
				response = EntityUtils.toString(httpEntity);
			}
			log.trace("REST Response from : " + apiURL.substring(apiURL.lastIndexOf(StringPool.FORWARD_SLASH)) + StringPool.SPACE + StringPool.COLON + StringPool.SPACE
					+ response);
			if (response.startsWith(StringPool.OPEN_BRACKET))
				jsonResponse = JSONFactoryUtil.createJSONArray(response);

			else if (response.startsWith(StringPool.OPEN_CURLY_BRACE))
				jsonResponse = JSONFactoryUtil.createJSONObject(response);

			else {
				JSONObject obj = null;
				try {
					obj = JSONFactoryUtil.createJSONObject();
					obj.put("Success", response);
					System.out.println("obj " + obj.toJSONString());
					System.out.println("obj " + obj.toString());
				} catch (Exception e) {

					e.printStackTrace();
				}

				jsonResponse = JSONFactoryUtil.createJSONObject(obj.toString());

			}
			
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + StringPool.COLON + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}finally{
			if(httpMethod!=null)
				httpMethod.releaseConnection();
				
				try{
					
					if(null != httpResponse){
						if(log.isDebugEnabled()){
							log.debug("closing the httpresponse");
						}
						httpResponse.close();
					}
					
					if(null != httpClient){
						if(log.isDebugEnabled()){
							log.debug("closing the httpclient");
						}
						
						httpClient.getConnectionManager().closeExpiredConnections();
						httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
						

					}
					
				}catch(Exception exception){
					log.error("closing the httpclient" + exception);
					
				}
			}
		
		checkResponseStatus(signature, (JSONObject) jsonResponse);
		
		return (JSONObject) jsonResponse;
	}

	/**
	 * 
	 * @param apiURL
	 * @param actionName
	 * @param CESTag
	 * @return
	 */
	private HttpDelete setHttpDeleteHeader(String apiURL, String actionName, String CESTag) {

		HttpDelete deleteMethod = new HttpDelete(apiURL);
		deleteMethod.setHeader("Content-Type", CommonConstant.APPLICATION_JSON);
		deleteMethod.setHeader("DPM_CES_TAG", CESTag);
		deleteMethod.setHeader("TS_SENT",
				Long.toString(Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE_GMT)).getTimeInMillis()));
		deleteMethod.setHeader("SRC_ACTION", actionName);
		deleteMethod.addHeader("Connection", CONNECTION_CLOSE);
		
		return deleteMethod;
	}

	/**
	 * 
	 * @param apiURL
	 * @param actionName
	 * @param CESTag
	 * @return
	 */
	private HttpPut setHttpPutHeader(String apiURL, String actionName, String CESTag) {

		HttpPut putMethod = new HttpPut(apiURL);
		putMethod.setHeader("Content-Type", CommonConstant.APPLICATION_JSON);
		putMethod.setHeader("DPM_CES_TAG", CESTag);
		putMethod.setHeader("TS_SENT",
				Long.toString(Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE_GMT)).getTimeInMillis()));
		putMethod.setHeader("SRC_ACTION", actionName);
		putMethod.addHeader("Connection", CONNECTION_CLOSE);
		
		return putMethod;
	}

	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 */
	private HttpGet setHttpGetHeader(String apiURL, String action, String CESTag) {

		HttpGet getMethod = new HttpGet(apiURL);
		getMethod.addHeader("DPM_CES_TAG", CESTag);
		getMethod.addHeader("TS_SENT",
				Long.toString(Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE_GMT)).getTimeInMillis()));
		getMethod.addHeader("SRC_ACTION",action);
		
		return getMethod;
	}

	/**
	 * 
	 * @param actionName
	 * @param CESTag
	 * @param postMethod
	 */
	private void setHttpPostHeader(String actionName, String CESTag,
			HttpPost postMethod) {

		postMethod
				.setHeader("Content-Type", CommonConstant.APPLICATION_JSON);
		postMethod.setHeader("DPM_CES_TAG", CESTag);
		postMethod.setHeader("TS_SENT", Long.toString(Calendar.getInstance(
				TimeZone.getTimeZone(TIMEZONE_GMT)).getTimeInMillis()));
		postMethod.setHeader("SRC_ACTION", actionName);
		postMethod.addHeader("Connection", CONNECTION_CLOSE);
		
	}

	/**
	 * 
	 * @return
	 */
	private RequestConfig getRequestConfig() {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000)
				.setConnectionRequestTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000)
				.setSocketTimeout(MAX_TIMEOUT_DURATION_MULE_API * 1000).build();
		return config;
	}

	/**
	 * 
	 * @param signature
	 * @param jsonObject
	 * @throws DipPortletException
	 */
	private void checkResponseStatus(String signature, JSONObject jsonObject) throws DipPortletException {

		if (jsonObject != null && jsonObject.has(STATUS) && !jsonObject.getString(STATUS).equalsIgnoreCase(SUCCESS)) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00037,
					new Exception());
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00037));
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
	}
	
	/**
	 * 
	 * @param apiURL
	 * @param action
	 * @param CESTag
	 * @return
	 */
	private HttpGet setHttpNonMuleGetHeader(String apiURL, String action, String CESTag) {
		if(CESTag==null || "".equals(CESTag.trim()) || "false".equalsIgnoreCase(CESTag.trim()))
			CESTag="Anil Rautela";
			
		HttpGet getMethod = new HttpGet(apiURL);
		getMethod.addHeader("x-myDip-identity", CESTag);
		getMethod.addHeader("Content-Type", "application/json");
		return getMethod;
	}
	
	/**
	 * 
	 * @param actionName
	 * @param CESTag
	 * @param postMethod
	 */
	private void setHttpNonMulePostHeader(String actionName, String CESTag,
			HttpPost postMethod) {

		postMethod
				.setHeader("Content-Type", CommonConstant.APPLICATION_JSON);
		postMethod.setHeader("DPM_CES_TAG", CESTag);
		postMethod.setHeader("TS_SENT", Long.toString(Calendar.getInstance(
				TimeZone.getTimeZone(TIMEZONE_GMT)).getTimeInMillis()));
		postMethod.setHeader("SRC_ACTION", actionName);
		postMethod.addHeader("Connection", CONNECTION_CLOSE);
		
	}

	/**
	 * 
	 * @param apiURL
	 * @param postBody
	 * @param headerMap
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object postDataToMicroServices(String apiURL, String postBody, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromMicroServices(String apiURL, String postBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "postBody", "headerMap" }, new Object[] { apiURL, postBody, headerMap }, null,
				null);

		HttpPost postMethod = new HttpPost(apiURL);
		postMethod.setHeader("Accept", "application/json");
		postMethod.setHeader("Content-type", "application/json");
		CommonUtil.setHttpHeader(headerMap, postMethod);
		Header[] headerArr = postMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		log.info("--apiURL----" + apiURL);
		try {
			postMethod.setEntity(new StringEntity(postBody, "UTF-8"));
		} catch (Exception e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		log.info("Service body/post : " + postBody);

		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		Object jsonResponse = executeMethod(signature, apiURL, postMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
	
	public JSONObject postDataToMicroServices(String apiURL, byte[] postBody, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#getPostDataFromMicroServices(String apiURL, String postBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "postBody", "headerMap" }, new Object[] { apiURL, postBody, headerMap }, null,
				null);

		HttpPost postMethod = new HttpPost(apiURL);
		CommonUtil.setHttpHeader(headerMap, postMethod);
		Header[] headerArr = postMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		log.info("--apiURL----" + apiURL);
		try {
			postMethod.setEntity(new ByteArrayEntity(postBody));
		} catch (Exception e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		log.info("Service body/post : " + postBody);

		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}


	/**
	 * 
	 * @param apiURL
	 * @param postBody
	 * @param headerMap
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object putDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#putDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "putBody", "headerMap" }, new Object[] { apiURL, putBody, headerMap }, null,
				null);

		HttpPut putMethod = new HttpPut(apiURL);
		CommonUtil.setHttpHeader(headerMap, putMethod);
		Header[] headerArr = putMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		if (Validator.isNotNull(putBody)) {
			try {
				putMethod.setEntity(new ByteArrayEntity(putBody));
			} catch (Exception e) {
				DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
				portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
				LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
						portletException.getErrorKey(), portletException);
				throw portletException;
			}
			log.debug("Service body/put : " + putBody);
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		Object object = executeMethod(signature, apiURL, putMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { object }, entranceTimestamp);
		return object;
	}

	/**
	 * 
	 * @param apiURL
	 * @param headerMap
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public Object getDataFromMicroServices(String apiURL, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME + "#getDataFromMicroServices(String apiURL, Map<String, String> headerMap";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL", "headerMap" },
				new Object[] { apiURL, headerMap }, null, null);

		Object response = null;
		HttpGet getMethod = new HttpGet(apiURL);
		CommonUtil.setHttpHeader(headerMap, getMethod);
		Header[] headerArr = getMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);
			LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
			return response;
		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} finally {
			if (getMethod != null)
				getMethod.releaseConnection();
			try {
				if (null != httpClient) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpclient");
					}
					// httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("closing the httpclient" + exception);
			}
		}
	}
	
	
	/**
	 * @param apiURL
	 * @param headerMap
	 * @return JSONObject
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject deleteDataToMicroServices(String apiURL,Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#deleteDataToMicroServices(String apiURL,Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "headerMap" }, new Object[] { apiURL,headerMap }, null,
				null);
		HttpDelete deleteMethod = new HttpDelete(apiURL);
		deleteMethod.setHeader("Accept", "application/json");
		deleteMethod.setHeader("Content-type", "application/json");
		CommonUtil.setHttpHeader(headerMap, deleteMethod);
		Header[] headerArr = deleteMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, deleteMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
	
	public Object getDataForSearchAddressBook(String apiURL, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {

		String signature = CLASS_NAME + "#getDataForSearchAddressBook(String apiURL,Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL","headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		HttpGet getMethod = new HttpGet(apiURL);
		CommonUtil.setHttpHeader(headerMap, getMethod);
		Header[] headerArr = getMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);

		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + ":" + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} finally {
			if (getMethod != null)
				getMethod.releaseConnection();

			try {
				if (null != httpClient) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpclient");
					}
					// httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("closing the httpclient" + exception);

			}
		}
		if (response != null && response instanceof JSONObject) {
			JSONObject jsonResponse = (JSONObject) response;
			checkResponseStatus(signature, jsonResponse);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return response;
	}
	public Object getDataForSearchOnAdd(String apiURL, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {

		String signature = CLASS_NAME + "#getDataForSearchOnAdd(String apiURL,Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL","headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		HttpGet getMethod = new HttpGet(apiURL);
		CommonUtil.setHttpHeader(headerMap, getMethod);
		Header[] headerArr = getMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		try {
			response = executeGet(apiURL, httpClient, getMethod);

		} catch (IOException e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(
					ErrorCodes.DIP_ERR_00001 + ":" + ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} catch (JSONException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00002, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00002) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} finally {
			if (getMethod != null)
				getMethod.releaseConnection();

			try {
				if (null != httpClient) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpclient");
					}
					// httpClient.close();
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("closing the httpclient" + exception);

			}
		}
		if (response != null && response instanceof JSONObject) {
			JSONObject jsonResponse = (JSONObject) response;
			checkResponseStatus(signature, jsonResponse);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return response;
	}
	
	
	public JSONObject putAddDataToMicroServices(String apiURL, String putBody, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#putDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "putBody", "headerMap" }, new Object[] { apiURL, putBody, headerMap }, null,
				null);
		HttpPut putMethod = new HttpPut(apiURL);
		putMethod.setHeader("Accept", "application/json");
		putMethod.setHeader("Content-type", "application/json");
		CommonUtil.setHttpHeader(headerMap, putMethod);
		Header[] headerArr = putMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		if (Validator.isNotNull(putBody)) {
			try {
				putMethod.setEntity(new StringEntity(putBody));
			} catch (Exception e) {
				DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
				portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
				LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
						portletException.getErrorKey(), portletException);
				throw portletException;
			}
			log.debug("Service body/put : " + putBody);
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, putMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}

	public JSONObject deleteDataToMicroServices(String apiURL, byte[] delBody, Map<String, String> headerMap)
                 throws DipPortletException, SystemServiceException {
           String signature = CLASS_NAME
                       + "#putDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)";
           Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
                       new String[] { "apiURL", "delBody", "headerMap" }, new Object[] { apiURL, delBody, headerMap }, null,
                       null);

           MyDeleteHandler delMethod = new MyDeleteHandler(apiURL);
           CommonUtil.setHttpHeader(headerMap, delMethod);
           Header[] headerArr = delMethod.getAllHeaders();
           for (int i = 0; i < headerArr.length; i++) {
                 log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
           }
           if (Validator.isNotNull(delBody)) {
                 try {
                       delMethod.setEntity(new ByteArrayEntity(delBody));
                 } catch (Exception e) {
                       DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
                 portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
                       LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
                                   portletException.getErrorKey(), portletException);
                       throw portletException;
                 }
                 log.debug("Service body/put : " + delBody);
           }
           CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
           JSONObject jsonResponse = execute(signature, apiURL, delMethod, httpClient);
           LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
           return jsonResponse;
     }
	
	//This method is used in printer Management portlets.
	public JSONObject delete(String apiURL, byte[] delBody, Map<String, String> headerMap)
            throws DipPortletException, SystemServiceException {
      String signature = CLASS_NAME
                  + "#putDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)";
      Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
                  new String[] { "apiURL", "delBody", "headerMap" }, new Object[] { apiURL, delBody, headerMap }, null,
                  null);

      MyDeleteHandler delMethod = new MyDeleteHandler(apiURL);
      CommonUtil.setHttpHeader(headerMap, delMethod);
      Header[] headerArr = delMethod.getAllHeaders();
      for (int i = 0; i < headerArr.length; i++) {
            log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
      }
      if (Validator.isNotNull(delBody)) {
            try {
                  delMethod.setEntity(new ByteArrayEntity(delBody));
            } catch (Exception e) {
                  DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
            portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
                  LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
                              portletException.getErrorKey(), portletException);
                  throw portletException;
            }
            log.debug("Service body/put : " + delBody);
      }
      CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
      JSONObject jsonResponse = execute(signature, apiURL, delMethod, httpClient);
      LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
      return jsonResponse;
}


	// =======================================================================================
	// New methods implemented
	// =======================================================================================
	
	/**
	 * Wrapper to HTTPClient 
	 * 
	 */
	private CloseableHttpClient getHttpClient() throws SystemServiceException {
		boolean sslSettingEnable = true;
		String signature = CLASS_NAME + "#getHttpClient(boolean sslSettingEnable)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "sslSettingEnable" },
				new Object[] { sslSettingEnable }, null, null);
		CloseableHttpClient httpClient = null;
		HttpClientFactory hcf = new HttpClientFactory();
		httpClient = hcf.getHttpClient(sslSettingEnable);
		LoggingWrapper.logExit(log, signature, new Object[] { httpClient }, entranceTimestamp);
		return httpClient;
	}
	
	
	
	/**
	 * 
	 * @param signature
	 * @param apiURL
	 * @param httpMethod
	 * @param httpClient
	 * @return
	 * @throws SystemServiceException
	 */
	@SuppressWarnings("deprecation")
	private ApiHandlerResponse executeWrapper(String signature, String apiURL, HttpRequestBase httpMethod) throws SystemServiceException {
		CloseableHttpClient httpClient=null;
		try {
			httpClient = getHttpClient(Boolean.TRUE);
		} catch (DipPortletException tpe) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, tpe);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + tpe.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		}
		ApiHandlerResponse apiHandlerResponse = null;
		boolean isDebugEnabled = log.isDebugEnabled();
		byte[] response = null;
		if (isDebugEnabled) {
			log.debug("==========================Executing - " + apiURL + ":start=============================");
		}
		HttpCoreContext localContext = new HttpCoreContext();
		CloseableHttpResponse httpResponse = null;
		logHeaders(httpMethod);
		try {			
			httpResponse = httpClient.execute(httpMethod, localContext);
		} catch (Exception e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} finally {
			if (null != httpResponse) {
				HttpEntity httpEntity = httpResponse.getEntity();
				if (Validator.isNotNull(httpEntity)) {
					try {
						response = EntityUtils.toByteArray(httpEntity);
						log.debug("Response content from api " + ApiHandler.getByteArrayAsString(response));
					} catch (IOException e) {
						log.error("Exception occured while converting the respone to byte array - ", e);
					}
				}				
				apiHandlerResponse = new ApiHandlerResponse();
				apiHandlerResponse.setStatusCode(httpResponse.getStatusLine().getStatusCode());
				apiHandlerResponse.setResponse(response);
				Map<String,String> responseHeaders=new HashMap<>();
				for( Header header:httpResponse.getAllHeaders()){
					responseHeaders.put(header.getName(),header.getValue());
				}
				apiHandlerResponse.setHeaders(responseHeaders);
			} else {
				if(isDebugEnabled) {
					log.debug("Response received is null");
				}
				System.out.println(("Response received is null"));
			}
			
			if (httpMethod != null)
				httpMethod.releaseConnection();
			try {
				// Close HttpResponse
				if (null != httpResponse) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpresponse");
					}
					httpResponse.close();
				}

				if (null != httpClient) {
					if (isDebugEnabled) {
						log.debug("closing the httpclient");
					}

					// Close the connection
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("Exception ocurred while closing HTTPClient" + exception);

			}
		}
		if (isDebugEnabled) {
			log.debug("==========================Executing - " + apiURL + ":end=============================");
		}

		
		if (isDebugEnabled) {
			log.debug("Response from URL - " + httpResponse);
		}
		return apiHandlerResponse;
	}
	
	@SuppressWarnings("deprecation")
	private ApiHandlerResponse executeWrapperAWS(String signature, String apiURL, HttpRequestBase httpMethod) throws Exception {
		CloseableHttpClient httpClient=null;
		httpClient = CommonUtil.doTrustToCertificatesAndCreateHttpClient();
		log.error("httpClient got in aws _"+httpClient);
		ApiHandlerResponse apiHandlerResponse = null;
		boolean isDebugEnabled = log.isDebugEnabled();
		byte[] response = null;
		if (isDebugEnabled) {
			log.debug("==========================Executing - " + apiURL + ":start=============================");
		}
		HttpCoreContext localContext = new HttpCoreContext();
		CloseableHttpResponse httpResponse = null;
		logHeaders(httpMethod);
		try {			
			httpResponse = httpClient.execute(httpMethod, localContext);
		} catch (Exception e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		} finally {
			if (null != httpResponse) {
				HttpEntity httpEntity = httpResponse.getEntity();
				if (Validator.isNotNull(httpEntity)) {
					try {
						response = EntityUtils.toByteArray(httpEntity);
						log.debug("Response content from api " + ApiHandler.getByteArrayAsString(response));
					} catch (IOException e) {
						log.error("Exception occured while converting the respone to byte array - ", e);
					}
				}				
				apiHandlerResponse = new ApiHandlerResponse();
				apiHandlerResponse.setStatusCode(httpResponse.getStatusLine().getStatusCode());
				apiHandlerResponse.setResponse(response);
				Map<String,String> responseHeaders=new HashMap<>();
				for( Header header:httpResponse.getAllHeaders()){
					responseHeaders.put(header.getName(),header.getValue());
				}
				apiHandlerResponse.setHeaders(responseHeaders);
			} else {
				if(isDebugEnabled) {
					log.debug("Response received is null");
				}
				System.out.println(("Response received is null"));
			}
			
			if (httpMethod != null)
				httpMethod.releaseConnection();
			try {
				// Close HttpResponse
				if (null != httpResponse) {
					if (log.isDebugEnabled()) {
						log.debug("closing the httpresponse");
					}
					httpResponse.close();
				}

				if (null != httpClient) {
					if (isDebugEnabled) {
						log.debug("closing the httpclient");
					}

					// Close the connection
					httpClient.getConnectionManager().closeExpiredConnections();
					httpClient.getConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
				}
			} catch (Exception exception) {
				log.error("Exception ocurred while closing HTTPClient" + exception);

			}
		}
		if (isDebugEnabled) {
			log.debug("==========================Executing - " + apiURL + ":end=============================");
		}

		
		if (isDebugEnabled) {
			log.debug("Response from URL - " + httpResponse);
		}
		return apiHandlerResponse;
	}
	
	/**
	 * 
	 * @param httpMethod
	 */
	protected void logHeaders(HttpRequestBase httpMethod) {
		boolean isDebugEnabled = log.isDebugEnabled();
		Header[] headerArr = httpMethod.getAllHeaders();
		if (isDebugEnabled) {
			log.debug("============================Headers:start==================================");
			if (null != headerArr) {
				for (int i = 0; i < headerArr.length; i++) {
					log.debug(headerArr[i].getName() + " " + headerArr[i].getValue());
				}
			}
			log.debug("============================Headers:end====================================");
		}
	}	
	
	/**
	 * 
	 * @param apiURL
	 * @param postBody
	 * @param headerMap
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public ApiHandlerResponse post(String apiURL, byte[] postBody, Map<String, String> headerMap)
			throws SystemServiceException {
		boolean isDebugEnabled = log.isDebugEnabled();
		String signature = CLASS_NAME + "#post(String apiURL, byte[] postBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "postBody", "headerMap" }, new Object[] { apiURL, postBody, headerMap }, null,
				null);
		HttpPost postMethod = new HttpPost(apiURL);
		ApiHandler.setHttpHeader(headerMap, postMethod);
		try {
			postMethod.setEntity(new ByteArrayEntity(postBody));
		} catch (Exception e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		}
		if (isDebugEnabled) {
			log.debug("Post Body: " + postBody);
		}
		ApiHandlerResponse apiHandlerResponse = executeWrapper(signature, apiURL, postMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { apiHandlerResponse }, entranceTimestamp);
		return apiHandlerResponse;
	}
	
	public ApiHandlerResponse postAWS(String apiURL, byte[] postBody, Map<String, String> headerMap)
			throws Exception {
		boolean isDebugEnabled = log.isDebugEnabled();
		String signature = CLASS_NAME + "#post(String apiURL, byte[] postBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "postBody", "headerMap" }, new Object[] { apiURL, postBody, headerMap }, null,
				null);
		HttpPost postMethod = new HttpPost(apiURL);
		ApiHandler.setHttpHeader(headerMap, postMethod);
		try {
			postMethod.setEntity(new ByteArrayEntity(postBody));
		} catch (Exception e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		}
		if (isDebugEnabled) {
			log.debug("Post Body: " + postBody);
		}
		ApiHandlerResponse apiHandlerResponse = executeWrapperAWS(signature, apiURL, postMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { apiHandlerResponse }, entranceTimestamp);
		return apiHandlerResponse;
	}
	
	public ApiHandlerResponse put(String apiURL, byte[] putBody, Map<String, String> headerMap)
			throws SystemServiceException {
		boolean isDebugEnabled = log.isDebugEnabled();
		String signature = CLASS_NAME + "#put(String apiURL, byte[] putBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "putBody", "headerMap" }, new Object[] { apiURL, putBody, headerMap }, null,
				null);
		HttpPut putMethod = new HttpPut(apiURL);
		ApiHandler.setHttpHeader(headerMap, putMethod);
		try {
			putMethod.setEntity(new ByteArrayEntity(putBody));
		} catch (Exception e) {
			SystemServiceException serviceException = new SystemServiceException(ErrorCodes.DIP_ERR_00001, e);
			serviceException.setMessage(ErrorCodes.DIP_ERR_00001 + StringPool.COLON
					+ ErrorCodes.map.get(ErrorCodes.DIP_ERR_00001) + e.getMessage());
			LoggingWrapper.logException(log, signature, serviceException.getSecretCode(),
					serviceException.getErrorKey(), serviceException);
			throw serviceException;
		}
		if (isDebugEnabled) {
			log.debug("put Body: " + putBody);
		}
		ApiHandlerResponse apiHandlerResponse = executeWrapper(signature, apiURL, putMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { apiHandlerResponse }, entranceTimestamp);
		return apiHandlerResponse;
	}	
	
	/**
	 * Executes HTTP Get Method
	 * @param apiURL
	 * @param headerMap
	 * @return ApiHandlerResponse
	 * @throws SystemServiceException
	 */
	public ApiHandlerResponse get(String apiURL, Map<String, String> headerMap)
			throws SystemServiceException {
		String signature = CLASS_NAME + "#get(String apiURL, Map<String, String> headerMap";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL", "headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		HttpGet getMethod = new HttpGet(apiURL);
		ApiHandler.setHttpHeader(headerMap, getMethod);
		ApiHandlerResponse apiHandlerResponse = executeWrapper(signature, apiURL, getMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return apiHandlerResponse; 
	}
	
	public ApiHandlerResponse getAWS(String apiURL, Map<String, String> headerMap)
			throws Exception {
		String signature = CLASS_NAME + "#getAWS(String apiURL, Map<String, String> headerMap";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL", "headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		HttpGet getMethod = new HttpGet(apiURL);
		ApiHandler.setHttpHeader(headerMap, getMethod);
		ApiHandlerResponse apiHandlerResponse = executeWrapperAWS(signature, apiURL, getMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return apiHandlerResponse; 
	}
	
	public static void setHttpHeader(Map<String, String> headerMap, Object httpMethod) {
		if (httpMethod instanceof HttpPost) {
			HttpPost httpPost = (HttpPost) httpMethod;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}
		} else if (httpMethod instanceof HttpGet) {
			HttpGet httpGet = (HttpGet) httpMethod;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				httpGet.setHeader(entry.getKey(), entry.getValue());
			}
		} else if (httpMethod instanceof HttpPut) {
			HttpPut httpPut = (HttpPut) httpMethod;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				httpPut.setHeader(entry.getKey(), entry.getValue());

			}
		} else if (httpMethod instanceof HttpDelete) {
			HttpDelete httpDelete = (HttpDelete) httpMethod;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				httpDelete.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * Executes HTTP Delete method
	 * @param apiURL
	 * @param headerMap
	 * @return ApiHandlerResponse
	 * @throws SystemServiceException
	 */
	public ApiHandlerResponse delete(String apiURL, Map<String, String> headerMap)
			throws SystemServiceException {
		String signature = CLASS_NAME + "#delete(String apiURL, Map<String, String> headerMap";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL", "headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		HttpDelete deleteMethod = new HttpDelete(apiURL);
		ApiHandler.setHttpHeader(headerMap, deleteMethod);
		ApiHandlerResponse apiHandlerResponse = executeWrapper(signature, apiURL, deleteMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return apiHandlerResponse; 
	}
	
	/**
	 * Executes HTTP Delete method
	 * @param apiURL
	 * @param headerMap
	 * @return ApiHandlerResponse
	 * @throws SystemServiceException
	 * @throws DipPortletException 
	 */
	public ApiHandlerResponse deleteWithBody(String apiURL,byte []delBody ,Map<String, String> headerMap)
			throws SystemServiceException, DipPortletException {
		String signature = CLASS_NAME + "#deleteWithBody(String apiURL, byte []data, Map<String, String> headerMap";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "apiURL", "headerMap" },
				new Object[] { apiURL, headerMap }, null, null);
		Object response = null;
		MyDeleteHandler delMethod = new MyDeleteHandler(apiURL);
		ApiHandler.setHttpHeader(headerMap, delMethod);
		if (Validator.isNotNull(delBody)) {
            try {
                  delMethod.setEntity(new ByteArrayEntity(delBody));
            } catch (Exception e) {
                  DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
            portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
                  LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
                              portletException.getErrorKey(), portletException);
                  throw portletException;
            }
            log.debug("Service body/delete : " + delBody);
      }
		ApiHandlerResponse apiHandlerResponse = executeWrapper(signature, apiURL, delMethod);
		LoggingWrapper.logExit(log, signature, new Object[] { response }, entranceTimestamp);
		return apiHandlerResponse; 
	}
	

	public static String getByteArrayAsString(byte[] byteArr) {
		// New implementation
		return getByteArrayAsString(byteArr, null);		
		// Old implementation
		/*
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArr);
		InputStreamReader isr = new InputStreamReader(bais);
		final int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		StringBuffer strBuffer = new StringBuffer();
		*/
		/* read the base script into string buffer */
		/*
		try {
			while (true) {
				int read = isr.read(buffer, 0, bufferSize);
				if (read == -1) {
					break;
				}

				strBuffer.append(buffer, 0, read);
			}
		} catch (IOException e) {
			log.error("Exception occurred while converting byte array as string", e);
		}
		return strBuffer.toString();
		*/
	}
	
	
	public static String getByteArrayAsString(byte[] byteArr, Charset charset) {
		if (null != byteArr) {
            if (null == charset) {
                return new String(byteArr);
            } else {
                return new String(byteArr, charset);
            } 
        }		
		return "";
	}
	
	/**
	 * 
	 * @param apiURL
	 * @param postBody
	 * @param headerMap
	 * @return
	 * @throws DipPortletException
	 * @throws SystemServiceException
	 */
	public JSONObject saveDataToMicroServices(String apiURL, byte[] postBody, Map<String, String> headerMap)
			throws DipPortletException, SystemServiceException {
		String signature = CLASS_NAME
				+ "#saveDataToMicroServices(String apiURL, byte[] putBody, Map<String, String> headerMap)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "apiURL", "postBody", "headerMap" }, new Object[] { apiURL, postBody, headerMap }, null,
				null);

		HttpPost postMethod = new HttpPost(apiURL);
		CommonUtil.setHttpHeader(headerMap, postMethod);
		Header[] headerArr = postMethod.getAllHeaders();
		for (int i = 0; i < headerArr.length; i++) {
			log.info("--header----" + headerArr[i].getName() + " " + headerArr[i].getValue());
		}
		if (Validator.isNotNull(postBody)) {
			try {
				postMethod.setEntity(new ByteArrayEntity(postBody));
			} catch (Exception e) {
				DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00003, e);
				portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00003) + e.getMessage());
				LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
						portletException.getErrorKey(), portletException);
				throw portletException;
			}
			log.debug("Service body/post : " + postBody);
		}
		CloseableHttpClient httpClient = getHttpClient(Boolean.TRUE);
		JSONObject jsonResponse = execute(signature, apiURL, postMethod, httpClient);
		LoggingWrapper.logExit(log, signature, new Object[] { jsonResponse }, entranceTimestamp);
		return jsonResponse;
	}
	
	
	
	public static void main(String[] args) throws SystemServiceException  {
		ApiHandler apiHandler = new ApiHandler();
		String apiURL = "http://httpbin.org/delete";
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("callId", "testcallid");
		headerMap.put("channel", "channel");
		headerMap.put("x-myDip-identity", "principal");
		headerMap.put("Content-Type", "application/json");
		
		String str = new String("This is put body");
		ApiHandlerResponse apiHandlerResponse = apiHandler.delete(apiURL, headerMap);
		System.out.println("apiHandlerResponse - " + apiHandlerResponse);
		
		 
		if(apiHandlerResponse != null) {
			int statusCode = apiHandlerResponse.getStatusCode();
			System.out.println("Status Code - " + statusCode);
			
			HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
			
			switch (httpStatus.series()) {
				case CLIENT_ERROR: 
					System.out.println("client error");
					break;
				case SERVER_ERROR:
					System.out.println("server error");
					break;
				default: 
					System.out.println("other exception");
					break;
			}
			
			System.out.println("Response - " + apiHandler.getByteArrayAsString(apiHandlerResponse.getResponse()));
		}
	}	
}