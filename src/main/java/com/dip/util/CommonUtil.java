package com.dip.util;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.dip.constant.CESUrlConstants;
import com.dip.constant.CommonConstant;
import com.dip.exception.DipPortletException;
import com.dip.model.HeaderLog;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;;


public class CommonUtil {

	private static Log log = LogFactoryUtil.getLog(CommonUtil.class);
	private static final String IGNORE_LAST_PATH = PropsUtil.get("ignore.last.path");
	private static final String OKTA_APP_SAML_SSO_URL = PropsUtil.get("okta.app.saml.sso.url");
	private static String CLASS_NAME = CommonUtil.class.getName();
	private static String RELAY_STATE = "RELAY_STATE";
	private static String ONE_TIME_TOKEN = "ONE_TIME_TOKEN";
	private static final String USER_ACC_VALIDATION_MAIL_SUBJECT = "user.acc.validation.mail.subject";
	private static final String USER_ACC_VALIDATION_MAIL_BODY_TITLE = "user.acc.validation.mail.body.title";
	private static final String USER_ACC_REGISTRATION_PUBLIC_PAGE = "user.acc.registration.Public.Page";
	private static final String ID = "id";
	private static final String UPS_USER_ID = "upsuserid";
	private static final String TIMESTAMP = "timestamp";
	private static final String EMAIL = "email";
	private static final String UPS_SERVICE_CHANNEL_NAME = PropsUtil.get("ups.service.channel.name");
	public static final String DEF = "DEF";	
	public static final String ACT = "ACT";
	public static final String QLD = "QLD";
	public static final String NT = "NT";
	public static final String NSW = "NSW";
	public static final String SA = "SA" ;
	public static final String TAS = "TAS";
	public static final String VIC = "VIC";
	public static final String WA = "WA";
	public static final String NZ = "NZ";

	/**
	 * 
	 * @param inputDate
	 * @param currentDateFormat
	 * @param requiredDateFormat
	 * @return
	 * @throws DipPortletException
	 */
	public static String getCustomDate(String inputDate, String currentDateFormat, String requiredDateFormat)
			throws DipPortletException {
		String signature = CLASS_NAME
				+ "#getCustomDate(String inputDate, String currentDateFormat, String requiredDateFormat)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "inputDate", "currentDateFormat", "requiredDateFormat" },
				new Object[] { inputDate, currentDateFormat, requiredDateFormat }, null, null);

		if (Validator.isNull(inputDate) || Validator.isNull(currentDateFormat)
				|| Validator.isNull(requiredDateFormat)) {
			return inputDate;
		}
		Date curretnDate = null;
		String outputDate = StringPool.BLANK;
		SimpleDateFormat requiredSimpleDateformat = new SimpleDateFormat(requiredDateFormat);
		SimpleDateFormat currentSimpleDateformat = new SimpleDateFormat(currentDateFormat);
		try {
			curretnDate = currentSimpleDateformat.parse(inputDate);
		} catch (ParseException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00042, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00042) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		outputDate = requiredSimpleDateformat.format(curretnDate);
		LoggingWrapper.logExit(log, signature, new Object[] { outputDate }, entranceTimestamp);
		return outputDate;
	}

	/**
	 * Tag used for monitoring the request
	 */
	public static String getCESTag(Object object) {

		Cookie[] cookieArr = null;
		ThemeDisplay themeDisplay = null;
		Map<String, String> cesParamMap = null;
		String emailAddress = CommonConstant.UNKNOWN;
		String customerID = CommonConstant.UNKNOWN;
		String jsessionID = CommonConstant.UNKNOWN;
		String companyID = CommonConstant.UNKNOWN;
		StringBuilder cesTag = new StringBuilder(StringPool.BLANK);

		if (object instanceof ActionRequest) {
			ActionRequest ActionRequest = (ActionRequest) object;
			themeDisplay = (ThemeDisplay) ActionRequest.getAttribute(WebKeys.THEME_DISPLAY);
			cookieArr = ActionRequest.getCookies();
		} else if (object instanceof RenderRequest) {
			RenderRequest renderRequest = (RenderRequest) object;
			themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
			cookieArr = renderRequest.getCookies();
		} else if (object instanceof ResourceRequest) {
			ResourceRequest resourceRequest = (ResourceRequest) object;
			themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
			cookieArr = resourceRequest.getCookies();
		} else if (object instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) object;
			themeDisplay = (ThemeDisplay) httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY);
			cookieArr = httpServletRequest.getCookies();
		} else if (object instanceof Map<?, ?>) {
			cesParamMap = (Map<String, String>) object;
		}
		if (Validator.isNotNull(cesParamMap)) {
			emailAddress = cesParamMap.get(CommonConstant.USERNAME);
			companyID = cesParamMap.get(CommonConstant.COMPANY_ID);
			customerID = cesParamMap.get(CommonConstant.CUSTOMER_ID);
			jsessionID = cesParamMap.get(CommonConstant.JSESSION_ID);
		} else {
			companyID = Long.toString(themeDisplay.getCompanyId());
			emailAddress = themeDisplay.getUser().getEmailAddress();
			User defaultUser = UserUtil.getDefaultUser(themeDisplay);
			if (Validator.isNull(emailAddress) || (Validator.isNotNull(defaultUser)
					&& emailAddress.equalsIgnoreCase(defaultUser.getEmailAddress()))) {
				emailAddress = CommonConstant.UNKNOWN;
			}
			/*if (themeDisplay.getUser().getExpandoBridge()
					.hasAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID))) {
				if (Validator.isNotNull(themeDisplay.getUser().getExpandoBridge()
						.getAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID)))) {
					customerID = (String) themeDisplay.getUser().getExpandoBridge()
							.getAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID));
				}
			}*/
			customerID = (Validator.isNull(customerID)) ? CommonConstant.UNKNOWN : customerID;
			if (cookieArr != null && cookieArr.length > 0) {
				for (int i = 0; i < cookieArr.length; i++) {
					if (cookieArr[i].getName().equalsIgnoreCase(CommonConstant.JSESSION_ID)) {
						jsessionID = cookieArr[i].getValue();
						break;
					}
				}
			}
		}
		cesTag.append(CommonConstant.USERNAME);
		cesTag.append(StringPool.EQUAL);
		cesTag.append(emailAddress);
		cesTag.append(StringPool.COMMA);
		cesTag.append(CommonConstant.COMPANY_ID);
		cesTag.append(StringPool.EQUAL);
		cesTag.append(companyID);
		cesTag.append(StringPool.COMMA);
		cesTag.append(CommonConstant.CUSTOMER_ID);
		cesTag.append(StringPool.EQUAL);
		cesTag.append(customerID);
		cesTag.append(StringPool.COMMA);
		cesTag.append(CommonConstant.JSESSION_ID);
		cesTag.append(StringPool.EQUAL);
		cesTag.append(jsessionID);

		return cesTag.toString();
	}


	/**
	 * 
	 * @param str
	 * @param entitySeperator
	 * @param pairSeperator
	 * @return
	 */
	public static Map<String, String> getMap(String str, String entitySeperator, String pairSeperator) {
		String signature = CLASS_NAME + "#getMap(String str, String entitySeperator, String pairSeperator)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "str", "entitySeperator", "pairSeperator" },
				new Object[] { str, entitySeperator, pairSeperator }, null, null);

		Map<String, String> resultMap = new HashMap<String, String>();
		String[] entityArr = str.split(entitySeperator);
		for (int i = 0; i < entityArr.length; i++) {
			String entityStr = entityArr[i].trim();
			String[] pairArr = entityStr.split(pairSeperator);
			if (pairArr.length == 2) {
				resultMap.put(pairArr[0], pairArr[1]);
			} else {
				resultMap.put(pairArr[0], StringPool.BLANK);
			}
		}
		LoggingWrapper.logExit(log, signature, new Object[] { resultMap }, entranceTimestamp);
		return resultMap;
	}

	/**
	 * 
	 * @param fromAddress
	 * @param toAddress
	 * @param ccAddress
	 * @param bccAddress
	 * @param mailSubject
	 * @param mailBody
	 * @param htmlFormat
	 * @return
	 */
	public static boolean sendMail(InternetAddress fromAddress, InternetAddress[] toAddress,
			InternetAddress[] ccAddress, InternetAddress[] bccAddress, String mailSubject, String mailBody,
			Boolean htmlFormat) {
		String signature = CLASS_NAME
				+ "#sendMail(InternetAddress fromAddress, InternetAddress[] toAddress, InternetAddress[] ccAddress, InternetAddress[] bccAddress, String mailSubject, String mailBody, Boolean htmlFormat)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "fromAddress", "toAddress", "ccAddress", "bccAddress", "mailSubject", "mailBody",
						"htmlFormat" },
				new Object[] { fromAddress, toAddress, ccAddress, bccAddress, mailSubject, mailBody, htmlFormat }, null,
				null);

		MailMessage mailMessage = new MailMessage(fromAddress, mailSubject, mailBody, htmlFormat);
		mailMessage.setTo(toAddress);
		if (ArrayUtil.isNotEmpty(ccAddress)) {
			mailMessage.setCC(ccAddress);
		}
		if (ArrayUtil.isNotEmpty(bccAddress)) {
			mailMessage.setBCC(bccAddress);
		}
		MailServiceUtil.sendEmail(mailMessage);
		log.info("mail sent");
		LoggingWrapper.logExit(log, signature, new Object[] { Boolean.TRUE }, entranceTimestamp);
		return Boolean.TRUE;
	}

	/**
	 * Utility method to send share shipment email with CC
	 * 
	 * @param from
	 * @param toAddress
	 * @param cc
	 * @param subject
	 * @param body
	 * @param htmlFormat
	 * @return true on success
	 */
	public static boolean sendShareShipmentMail(InternetAddress from, InternetAddress[] toAddress, InternetAddress[] cc,
			String subject, String body, Boolean htmlFormat) {

		try {
			// MailEngine.send(from, toAddress, cc, subject, body, true);
			MailMessage mailMessage = new MailMessage(from, subject, body, true);
			// mailMessage.setTo(toAddress);
			if (ArrayUtil.isNotEmpty(toAddress)) {
				mailMessage.setBCC(toAddress);
			}
			if (ArrayUtil.isNotEmpty(cc)) {
				mailMessage.setCC(cc);
			}
			MailServiceUtil.sendEmail(mailMessage);
		} catch (Exception e) {
			return Boolean.FALSE;
		}
		log.info("Shipment shared via mail");
		return Boolean.TRUE;
	}

	/**
	 * Overridden Utility method to send share shipment email without CC
	 * 
	 * @param from
	 * @param toAddress
	 * @param subject
	 * @param body
	 * @param htmlFormat
	 * @return true on success
	 */
	public static boolean sendShareShipmentMail(InternetAddress from, InternetAddress[] toAddress, String subject,
			String body, Boolean htmlFormat) {

		try {
			MailMessage mailMessage = new MailMessage(from, subject, body, true);
			if (ArrayUtil.isNotEmpty(toAddress)) {
				mailMessage.setBCC(toAddress);
			}
			MailServiceUtil.sendEmail(mailMessage);
		} catch (Exception e) {
			return Boolean.FALSE;
		}
		log.info("Shipment shared via mail");
		return Boolean.TRUE;
	}

	/**
	 * converts the input string in title case
	 * 
	 * @param message
	 * @return string in title case
	 */
	public static String convertToTitleCase(String message) {
		if (Validator.isNotNull(message)) {
			if (!message.contains(StringPool.SPACE)) {
				return StringUtil.upperCaseFirstLetter((StringUtil.lowerCase(message)));
			} else {
				String[] messageTerms = message.split(StringPool.SPACE);
				StringBuffer parsedMessage = new StringBuffer();
				for (String messageTerm : messageTerms) {
					if (Validator.isNotNull(messageTerm)) {
						parsedMessage.append(StringUtil.upperCaseFirstLetter((StringUtil.lowerCase(messageTerm))));
						parsedMessage.append(StringPool.SPACE);
					}
				}
				return parsedMessage.toString().trim();
			}
		} else {
			return StringPool.BLANK;
		}
	}

	/**
	 * 
	 * @param emailAddress
	 * @param portletRequest
	 * @return
	 */
	public static Map<String, String> getCesParamMapUnauthUser(String emailAddress, PortletRequest portletRequest) {
		String signature = CLASS_NAME + "#getCesMapUnauthUser(String emailAddress, PortletRequest portletRequest)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "emailAddress", "portletRequest" }, new Object[] { emailAddress, portletRequest }, null,
				null);

		User user = null;
		String customerID = StringPool.BLANK;
		String jsessionID = StringPool.BLANK;
		Map<String, String> paramMap = new HashMap<>();
		PermissionChecker permissionChecker = null;
		try {
			user = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), emailAddress);
		} catch (PortalException | SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00016, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00016) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), e);
		}
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(user);
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		} catch (Exception e) {
			LoggingWrapper.logException(log, signature, null, null, e);
		}
		/*if (Validator.isNotNull(permissionChecker)
				&& user.getExpandoBridge().hasAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID))) {
			customerID = (String) user.getExpandoBridge()
					.getAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID));
		}*/
		Cookie[] cookieArr = portletRequest.getCookies();
		for (int i = 0; i < cookieArr.length; i++) {
			if (cookieArr[i].getName().equalsIgnoreCase(CommonConstant.JSESSION_ID)) {
				jsessionID = cookieArr[i].getValue();
				break;
			}
		}
		customerID = (Validator.isNull(customerID)) ? CommonConstant.UNKNOWN : customerID;
		emailAddress = (Validator.isNull(emailAddress)) ? CommonConstant.UNKNOWN : emailAddress;
		jsessionID = (Validator.isNull(jsessionID)) ? CommonConstant.UNKNOWN : jsessionID;

		paramMap.put(CommonConstant.USERNAME, emailAddress);
		paramMap.put(CommonConstant.COMPANY_ID, Long.toString(PortalUtil.getDefaultCompanyId()));
		paramMap.put(CommonConstant.CUSTOMER_ID, customerID);
		paramMap.put(CommonConstant.JSESSION_ID, jsessionID);
		LoggingWrapper.logExit(log, signature, new Object[] { paramMap }, entranceTimestamp);
		log.debug(" paramMap " + paramMap);
		return paramMap;
	}

	/**
	 * 
	 * @param emailAddress
	 * @param portletRequest
	 * @return
	 * @throws DipPortletException
	 */
	public static boolean sendAccValidationMail(String emailAddress, String firstName, String oktaUserId,
			String upsUserId, long groupId, String portalURL, String themeURL) throws DipPortletException {
		String signature = CLASS_NAME + "#sendWatchVerifcationMail(String emailAddress)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "emailAddress" },
				new Object[] { emailAddress }, null, null);

		InternetAddress toAddress = null;
		InternetAddress fromAddress = null;
		String mailSubject = PropsUtil.get(USER_ACC_VALIDATION_MAIL_SUBJECT);
		try {
			toAddress = new InternetAddress(emailAddress);
			fromAddress = new InternetAddress(PropsUtil.get(CommonConstant.ADMIN_EMAIL_ADDRESS),
					PropsUtil.get(CommonConstant.ADMIN_MAIL_FROM_NAME));
		
		} catch (AddressException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00028,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00028), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} catch (Exception e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00028,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00028), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}

		JournalArticle journalArticle = null;
		String journalContent = "";
		try {
			journalArticle = JournalArticleLocalServiceUtil.getArticleByUrlTitle(groupId,
					PropsUtil.get(USER_ACC_VALIDATION_MAIL_BODY_TITLE));
			journalContent = JournalArticleLocalServiceUtil.getArticleContent(journalArticle,
					journalArticle.getDDMTemplateKey(), StringPool.BLANK, StringPool.BLANK, null, null);
		} catch (PortalException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00030,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00030), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} catch (SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00031,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00031), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}

		StringBuilder validationURL = new StringBuilder(portalURL);
		validationURL.append(StringPool.FORWARD_SLASH);
		validationURL.append(PropsUtil.get(USER_ACC_REGISTRATION_PUBLIC_PAGE));
		validationURL.append(StringPool.QUESTION);
		validationURL.append(ID);
		validationURL.append(StringPool.EQUAL);
		validationURL.append(oktaUserId);
		validationURL.append(StringPool.AMPERSAND);
		validationURL.append(UPS_USER_ID);
		validationURL.append(StringPool.EQUAL);
		validationURL.append(upsUserId);
		validationURL.append(StringPool.AMPERSAND);
		validationURL.append(TIMESTAMP);
		validationURL.append(StringPool.EQUAL);
		validationURL.append(Long.toString(System.currentTimeMillis()));

		journalContent = StringUtil.replace(journalContent, "[$PORTAL_URL$]", portalURL);
		journalContent = StringUtil.replace(journalContent, "[$PORTAL_THEME_URL$]", themeURL);
		journalContent = StringUtil.replace(journalContent, "[$USER_NAME$]", firstName);
		journalContent = StringUtil.replace(journalContent, "[$VALIDATE_ACC_URL$]", validationURL.toString());

		Boolean mailStatus = sendMail(fromAddress, new InternetAddress[] { toAddress }, null, null, mailSubject,
				journalContent, Boolean.TRUE);
		LoggingWrapper.logExit(log, signature, new Object[] { mailStatus }, entranceTimestamp);
		return mailStatus;
	}

	/**
	 * 
	 * @param webContentTitle
	 * @param groupID
	 * @return
	 * @throws DipPortletException
	 */
	public static String getWebContent(String webContentTitle, long groupID) throws DipPortletException {
		String signature = CLASS_NAME + "#getWebContent(String webContentTitle, long groupID)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "webContentTitle", "groupID" }, new Object[] { webContentTitle, groupID }, null, null);

		JournalArticle journalArticle = null;
		String journalContent = null;
		try {
			journalArticle = JournalArticleLocalServiceUtil.getArticleByUrlTitle(groupID,
					PropsUtil.get(webContentTitle));
			journalContent = JournalArticleLocalServiceUtil.getArticleContent(journalArticle,
					journalArticle.getDDMTemplateKey(), StringPool.BLANK, StringPool.BLANK, null, null);
		} catch (PortalException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00030,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00030), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		} catch (SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00031,
					ErrorCodes.map.get(ErrorCodes.DIP_ERR_00031), e);
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), portletException);
			throw portletException;
		}
		LoggingWrapper.logExit(log, signature, new Object[] { journalContent }, entranceTimestamp);
		return journalContent;
	}

	/**
	 * 
	 * @param fromAddress
	 * @param toAddress
	 * @param ccAddress
	 * @param bccAddress
	 * @param mailSubject
	 * @param mailBody
	 * @param htmlFormat
	 * @param attachment
	 * @return
	 */
	public static boolean sendMailUserReport(InternetAddress fromAddress, InternetAddress toAddress,
			InternetAddress[] ccAddress, InternetAddress[] bccAddress, String mailSubject, String mailBody,
			Boolean htmlFormat, File attachment) {
		String signature = CLASS_NAME
				+ "#sendMail(InternetAddress fromAddress, InternetAddress[] toAddress, InternetAddress[] ccAddress, InternetAddress[] bccAddress, String mailSubject, String mailBody, Boolean htmlFormat)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "fromAddress", "toAddress", "ccAddress", "bccAddress", "mailSubject", "mailBody",
						"htmlFormat" },
				new Object[] { fromAddress, toAddress, ccAddress, bccAddress, mailSubject, mailBody, htmlFormat }, null,
				null);

		MailMessage mailMessage = new MailMessage(fromAddress, mailSubject, mailBody, htmlFormat);
		mailMessage.addFileAttachment(attachment);

		mailMessage.setTo(toAddress);
		if (ArrayUtil.isNotEmpty(ccAddress)) {
			mailMessage.setCC(ccAddress);
		}
		if (ArrayUtil.isNotEmpty(bccAddress)) {
			mailMessage.setBCC(bccAddress);
		}
		MailServiceUtil.sendEmail(mailMessage);
		
		log.info("mail sent");
		LoggingWrapper.logExit(log, signature, new Object[] { Boolean.TRUE }, entranceTimestamp);
		return Boolean.TRUE;
	}

	/**
	 * @param emailsTo
	 *            as parameter
	 * @param sender
	 *            as parameter
	 * @param emailHeaderLoggerCommunicationTemplate
	 *            as parameter
	 * @param emailHeaderLoggerMyDipSenderComponent
	 *            as parameter
	 * @return HeaderLog as a String
	 */
	public static HeaderLog headerLogSetter(String requester, String emailsTo, String sender,
			String emailHeaderLoggerCommunicationTemplate, String emailHeaderLoggerMyDipSenderComponent) {
		String senderServerIp = StringPool.BLANK;
		try {
			InetAddress ipAddr = InetAddress.getLocalHost();
			senderServerIp = ipAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		java.util.Date date = new java.util.Date();
		String timeStamp = encodeSpecialCharactersInString(new Timestamp(date.getTime()).toString());
		String messageUUID = emailsTo + StringPool.UNDERLINE + date.getTime();
		return new HeaderLog(encodeSpecialCharactersInString(encodeEmailTag(requester)), encodeEmailTag(sender),
				encodeEmailTag(emailsTo), encodeSpecialCharactersInString(encodeEmailTag(messageUUID)), timeStamp,
				encodeSpecialCharactersInString(senderServerIp),
				encodeSpecialCharactersInString(emailHeaderLoggerCommunicationTemplate),
				encodeSpecialCharactersInString(emailHeaderLoggerMyDipSenderComponent));
	}

	/**
	 * @param mailTag
	 *            as mailtag
	 * @return The mailTag
	 */
	public static String encodeSpecialCharactersInString(final String mailTag) {
		if (Validator.isNotNull(mailTag)) {
			return mailTag.replaceAll("\\.", "-").replaceAll("\\s", StringPool.DASH).replaceAll(StringPool.COLON,
					StringPool.DASH);
		} else {
			return mailTag;
		}
	}

	/**
	 * @param mailTag
	 *            as mailtag
	 * @return The mailTag
	 */
	public static String encodeEmailTag(final String mailTag) {
		if (Validator.isNotNull(mailTag)) {
			return mailTag.replaceAll(StringPool.AT, "_AT_").replaceAll("\\.", "_DOT_").replaceAll(StringPool.COMMA,
					StringPool.UNDERLINE);
		} else {
			return mailTag;
		}
	}

	/**
	 * 
	 * @param requester
	 * @param toAddress
	 * @param fromAddress
	 * @param emailHeaderLoggerCommunicationTemplate
	 * @param emailHeaderLoggerMyDipSenderComponent
	 */
	public static void setHeaders(String requester, String toAddress, String fromAddress,
			String emailHeaderLoggerCommunicationTemplate, String emailHeaderLoggerMyDipSenderComponent) {
		final String configurationSetName = "";
		log.info("---------------------------HEADER SET FOR EMAIL--START------------------------------");
		log.info(headerLogSetter(requester, toAddress, fromAddress, emailHeaderLoggerCommunicationTemplate,
				emailHeaderLoggerMyDipSenderComponent).toString() + ", xSesConfigurationSet=" + configurationSetName);
		log.info("---------------------------HEADER SET FOR EMAIL--END------------------------------");

	}

	/**
	 * @param headerMap
	 * @param httpMethod
	 */
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
	 * @param cesTag
	 * @param xMyDipIdentity
	 * @return
	 */
	public static Map<String, String> getDefaultHeaderMap(String cesTag, String xMyDipIdentity) {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put(CommonConstant.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		headerMap.put(CommonConstant.ACCEPT, ContentTypes.APPLICATION_JSON);
		return headerMap;
	}
	
	
	/**
	 * @param request
	 * @return
	 */
	public static String getUserId(Object request){
		ThemeDisplay themeDisplay = null;
		if (request instanceof ResourceRequest){
			ResourceRequest req =(ResourceRequest)request;
			themeDisplay = (ThemeDisplay) req.getAttribute(WebKeys.THEME_DISPLAY);
		}else if (request instanceof RenderRequest){
			RenderRequest req =(RenderRequest)request;
			themeDisplay = (ThemeDisplay) req.getAttribute(WebKeys.THEME_DISPLAY);
		}else if(request instanceof PortletRequest){
			PortletRequest req =(PortletRequest)request;
			themeDisplay = (ThemeDisplay) req.getAttribute(WebKeys.THEME_DISPLAY);
		}
		User liferayUser = themeDisplay.getUser();
		String upsUerId = null;
		/*if (liferayUser.getExpandoBridge().hasAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_UPSUSERID))) {
			upsUerId = (String) liferayUser.getExpandoBridge()
					.getAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_UPSUSERID));
		}*/
		return upsUerId;
	}
	
	/**
	 * @param portletRequest
	 * @param siteFriendlyURL
	 * @param pageFriendlyURL
	 * @param portletName
	 * @param renderActionName
	 * @param state
	 * @param parameters
	 * @return
	 */
	public static String getPortletURL(PortletRequest portletRequest, String siteFriendlyURL, String pageFriendlyURL,
			String portletName, String renderActionName, String state, Map<String, String[]> parameters) {
		// Set basic parameters
		parameters.put(CESUrlConstants.PURL_KEY_ACTION.getKey(), new String[] { renderActionName });
		parameters.put(CESUrlConstants.PURL_KEY_STATE.getKey(), new String[] { state });
		parameters.put(CESUrlConstants.PURL_KEY_MODE.getKey(), new String[] { CESUrlConstants.PURL_KEY_VALUE.getKey() });
		return getFullPortletURL(portletRequest, siteFriendlyURL, pageFriendlyURL, portletName,
				PortletRequest.RENDER_PHASE, parameters);
	}

	/**
	 * This method will actuall return the portlet url
	 * 
	 * @param portletRequest
	 * @param friendlyUrl
	 * @param portletName
	 * @param parameters
	 * @param portletPhase
	 * @param communityName
	 * @return String
	 */
	public static String getFullPortletURL(PortletRequest portletRequest, String siteFriendlyURL, String pageFriendlyUrl,
			String portletName, String portletPhase, Map<String, String[]> parameters) {
		Layout layout = getLayout(portletRequest, siteFriendlyURL, pageFriendlyUrl);
		if (layout != null) {
			HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);
			PortletURL liferayURL = PortletURLFactoryUtil.create(request, portletName, layout.getPlid(), portletPhase);
			if (liferayURL != null) {
				liferayURL.setParameters(parameters);
				return liferayURL.toString();
			}
		}
		return null;
	}

	/**
	 * This method will return layout based on friendly url of liferay page.
	 * 
	 * @param request
	 * @param pageFriendlyURL
	 * @param communityName
	 * @return
	 */
	public static Layout getLayout(PortletRequest request, String siteFriendlyURL, String pageFriendlyURL) {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		Layout layout = null;
		try {
			Group group = GroupLocalServiceUtil.getFriendlyURLGroup(themeDisplay.getCompanyId(), siteFriendlyURL);
			layout = LayoutLocalServiceUtil.getFriendlyURLLayout(group.getGroupId(), true, pageFriendlyURL);
			if (layout == null) {
				layout = LayoutLocalServiceUtil.getFriendlyURLLayout(group.getGroupId(), false, pageFriendlyURL);
			}
		} catch (PortalException e) {
			log.error(e.getMessage(), e);
		} catch (SystemException e) {
			log.error(e.getMessage(), e);
		}

		return layout;
	}
	
    /**
     * @return
     */
    public static CloseableHttpClient doTrustToCertificatesAndCreateHttpClient() {
    	CloseableHttpClient httpclient = null;
           TrustManager[] trustAllCerts = new TrustManager[]{
             new X509TrustManager() {
                 public X509Certificate[] getAcceptedIssuers() {
                     return null;
                 }
                 public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                     return;
                 }
                 public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                     return;
                 }
             }
           };

           SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			 sc.init(null, trustAllCerts, new SecureRandom());
	         httpclient = HttpClients.custom().setSslcontext(sc).build();
	         return httpclient;
		} catch (NoSuchAlgorithmException e) {
			log.error(e);
		} catch (KeyManagementException e) {
			log.error(e);
		}
          
             return null;
   }

	/**
	 * @param request
	 * @return
	 */
	public static Map<String, String> getDispatchEntityHeaderMap(ResourceRequest request) {
		String upsUserId = getUserId(request);
		Map<String, String> headerMap = CommonUtil.getDefaultHeaderMap(CommonUtil.getCESTag(request), upsUserId);
		return headerMap;
	}

	/**
	 * @return
	 */
	public static String generateUuidForCallId() {
		UUID guid = UUID.randomUUID();
		return guid.toString();
	}
	
	/**
	 * @param stateCode
	 * @param countryCode
	 * @return
	 */
	public static String zoneMapping(String stateCode,String countryCode){
		Map<String,String> timeZone=new HashMap<String,String>();
		timeZone.put(DEF,"Australia/ACT");
		timeZone.put(ACT, "Australia/ACT");
		timeZone.put(QLD, "Australia/Queensland");
		timeZone.put(NT, "Australia/North");
		timeZone.put(NSW, "Australia/NSW");
		timeZone.put(SA, "Australia/South");
		timeZone.put(TAS, "Australia/Tasmania");
		timeZone.put(VIC, "Australia/Victoria");
		timeZone.put(WA, "Australia/West");
		timeZone.put(NZ, "Pacific/Auckland");
		System.out.println(timeZone.get(stateCode));
		return timeZone.get(stateCode);
	}
	
	public static String ConvertTimeZones(String sFromTimeZone, String sToTimeZone, String sFromDateTime){
        DateTimeZone oFromZone       = DateTimeZone.forID(sFromTimeZone);
        DateTimeZone oToZone         = DateTimeZone.forID(sToTimeZone);
        DateTime oDateTime           = new DateTime(sFromDateTime);
        DateTime oFromDateTime       = oDateTime.withZoneRetainFields(oFromZone);
        DateTime oToDateTime         = new DateTime(oFromDateTime).withZone(oToZone);
        DateTimeFormatter oFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
        DateTime oNewDate            = oFormatter.withOffsetParsed().parseDateTime(oToDateTime.toString());
        return oFormatter.withZone(oToZone).print(oNewDate.getMillis());
   }
   
   /**
     * Concatenate with symbol.
     *
     * @param symbol the symbol
     * @param args the args
     * @return the String
     */
    public static String concatenateWithSymbol(final String symbol, final Object... args) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length - 1; i++) {
            stringBuilder.append(args[i]);
            stringBuilder.append(symbol);
        }
        stringBuilder.append(args[args.length - 1]);
        return stringBuilder.toString();
    }

    /**
     * 
     * @param key
     * @param str
     * @return
     */
	public static String getEncryptedString(Key key, String str) {
		if (Validator.isNull(key)) {
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			HttpServletRequest httpServletRequest = serviceContext.getRequest();
			ThemeDisplay themeDisplay = (ThemeDisplay) httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY);
			key = themeDisplay.getCompany().getKeyObj();
		}
		try {
			return Encryptor.encrypt(key, str);
		} catch (EncryptorException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 
	 * @param key
	 * @param str
	 * @return
	 */
	public static String getDecryptedString(Key key, String str) {
		if (Validator.isNull(key)) {
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			HttpServletRequest httpServletRequest = serviceContext.getRequest();
			ThemeDisplay themeDisplay = (ThemeDisplay) httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY);
			key = themeDisplay.getCompany().getKeyObj();
		}
		try {
			return Encryptor.decrypt(key, str);
		} catch (EncryptorException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 
	 * @param text
	 * @param offset
	 * @return
	 */
	public static String encrypt(String text, int offset) {
		char[] plain = text.toCharArray();

		for (int i = 0; i < plain.length; i++) {
			for (int j = 0; j < chars.length; j++) {
				if (j <= chars.length - offset) {
					if (plain[i] == chars[j]) {
						plain[i] = chars[j + offset];
						break;
					}
				} else if (plain[i] == chars[j]) {
					plain[i] = chars[j - (chars.length - offset + 1)];
				}
			}
		}
		return String.valueOf(plain);
	}

	/**
	 * 
	 * @param cip
	 * @param offset
	 * @return
	 */
	public static String decrypt(String cip, int offset) {
		char[] cipher = cip.toCharArray();
		for (int i = 0; i < cipher.length; i++) {
			for (int j = 0; j < chars.length; j++) {
				if (j >= offset && cipher[i] == chars[j]) {
					cipher[i] = chars[j - offset];
					break;
				}
				if (cipher[i] == chars[j] && j < offset) {
					cipher[i] = chars[(chars.length - offset + 1) + j];
					break;
				}
			}
		}
		return String.valueOf(cipher);
	}

	private static char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z', '!', '@', '$', '%', '^', '(', ')', '+', '-', '*', '/', '[', ']', '{', '}', '=', '<',
			'>', '_', '"', '.', ',', ' ' };

	public static int offset = 5;
}