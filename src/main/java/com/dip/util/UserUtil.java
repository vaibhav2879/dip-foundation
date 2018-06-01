package com.dip.util;

import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;

import com.dip.constant.CommonConstant;
import com.dip.exception.DipPortletException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

public class UserUtil {

	private static Log log = LogFactoryUtil.getLog(UserUtil.class);
	private static String CLASS_NAME = UserUtil.class.getName();

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getCustomerId(PortletRequest request) {
		String signature = CLASS_NAME + "#getCustomerId(PortletRequest request)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "request" },
				new Object[] { request }, null, null);
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

		/*if (themeDisplay.getUser().getExpandoBridge()
				.hasAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID))) {
			String customerId = (String) themeDisplay.getUser().getExpandoBridge()
					.getAttribute(PropsUtil.get(CommonConstant.CUSTOM_FIELD_USER_CUSTOMERID));
			LoggingWrapper.logExit(log, signature, new Object[] { customerId }, entranceTimestamp);
			return customerId;
		}*/
		return null;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static Long getUserId(PortletRequest request) {
		String signature = CLASS_NAME + "#getUserId(PortletRequest request)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "request" },
				new Object[] { request }, null, null);
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		LoggingWrapper.logExit(log, signature, new Object[] { themeDisplay.getUserId() }, entranceTimestamp);
		return themeDisplay.getUserId();
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getUserEmailId(PortletRequest request) {
		String signature = CLASS_NAME + "#getUserEmailId(PortletRequest request)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "request" },
				new Object[] { request }, null, null);
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		LoggingWrapper.logExit(log, signature, new Object[] { themeDisplay.getUser().getEmailAddress() },
				entranceTimestamp);
		return themeDisplay.getUser().getEmailAddress();
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isUserAuthenticated(PortletRequest request) {
		String signature = CLASS_NAME + "#isUserAuthenticated(PortletRequest request)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "request" },
				new Object[] { request }, getUserId(request), getUserEmailId(request));

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		LoggingWrapper.logExit(log, signature, new Object[] { themeDisplay.isSignedIn() }, entranceTimestamp);
		return themeDisplay.isSignedIn();
	}

	/**
	 * 
	 * @param emailAddress
	 * @return
	 */
	public static boolean isUserExists(String emailAddress) {
		String signature = CLASS_NAME + "#isUserExists(String emailAddress)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "emailAddress" },
				new Object[] { emailAddress }, null, null);

		User user = null;
		try {
			user = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), emailAddress);
		} catch (PortalException | SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00016, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00016) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), e);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { emailAddress }, entranceTimestamp);
		return (Validator.isNull(user) ? Boolean.FALSE : Boolean.TRUE);
	}

	/**
	 * 
	 * @param portletRequest
	 * @return
	 */
	public static String getPhoneNo(String emailAddress) {
		String signature = CLASS_NAME + "#getPhoneNo(String emailAddress)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "emailAddress" },
				new Object[] { emailAddress }, null, null);

		User user = null;
		List<Phone> phoneList = null;
		String phoneNumber = StringPool.BLANK;
		try {
			user = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), emailAddress);
		} catch (PortalException | SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00016, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00016) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), e);
		}
		if (Validator.isNotNull(user)) {
			try {
				phoneList = user.getPhones();
			} catch (SystemException e) {
				DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00015,
						ErrorCodes.map.get(ErrorCodes.DIP_ERR_00015), e);
				LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
						portletException.getErrorKey(), e);
				return StringPool.BLANK;
			}
			for (Phone phones : phoneList) {
				if (Validator.isNotNull(phones.getNumber())) {
					if (phones.isPrimary() == Boolean.TRUE) {
						return phones.getNumber();
					} else {
						phoneNumber = phones.getNumber();
					}
				}
			}
		}
		LoggingWrapper.logExit(log, signature, new Object[] { emailAddress }, entranceTimestamp);
		return phoneNumber;
	}

	/**
	 * 
	 * @param themeDisplay
	 * @return
	 */
	public static User getDefaultUser(ThemeDisplay themeDisplay) {
		String signature = CLASS_NAME + "#getDefaultUser()";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature, new String[] { "themeDisplay" },
				new Object[] { themeDisplay }, null, null);
		User defaultUser = null;
		try {
			defaultUser = UserLocalServiceUtil.getDefaultUser(themeDisplay.getCompanyId());
		} catch (PortalException | SystemException e) {
			DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00051, e);
			portletException.setMessage(ErrorCodes.map.get(ErrorCodes.DIP_ERR_00051) + e.getMessage());
			LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
					portletException.getErrorKey(), e);
		}
		LoggingWrapper.logExit(log, signature, new Object[] { defaultUser }, entranceTimestamp);
		return defaultUser;
	}

	/**
	 * 
	 * @param liferayUserObj
	 * @param customFieldName
	 * @param value
	 * @return
	 */
	public static User setCustomField(User liferayUserObj, String customFieldName, String value) {
		PermissionChecker permissionChecker = null;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(liferayUserObj);
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		} catch (Exception e) {

		}
		if (Validator.isNotNull(permissionChecker)
				&& liferayUserObj.getExpandoBridge().hasAttribute(customFieldName))
			liferayUserObj.getExpandoBridge().setAttribute(customFieldName, value, Boolean.FALSE);
		return liferayUserObj;
	}

	/**
	 * 
	 * @param liferayUserObj
	 * @param customFieldName
	 * @param value
	 * @return
	 */
	public static User setCustomField(User liferayUserObj, String customFieldName, Boolean value) {
		PermissionChecker permissionChecker = null;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(liferayUserObj);
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		} catch (Exception e) {

		}
		if (Validator.isNotNull(permissionChecker) && liferayUserObj.getExpandoBridge().hasAttribute(customFieldName))
			liferayUserObj.getExpandoBridge().setAttribute(customFieldName, value, Boolean.FALSE);
		return liferayUserObj;
	}

	/**
	 * 
	 * @param liferayUserObj
	 * @param customFieldName
	 * @return
	 */
	public static Object getCustomField(User liferayUserObj, String customFieldName) {
		PermissionChecker permissionChecker = null;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(liferayUserObj);
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		} catch (Exception e) {

		}
		if (Validator.isNotNull(permissionChecker) && liferayUserObj.getExpandoBridge().hasAttribute(customFieldName))
			return liferayUserObj.getExpandoBridge().getAttribute(customFieldName);
		else
			return null;
	}
}