package com.dip.util;

import java.util.Date;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class XssUtil {

	private static String CLASS_NAME = XssUtil.class.getName();
	private static final Log log = LogFactoryUtil.getLog(XssUtil.class);
	private static String[] MALICIOUS_ARRAY = { "<script", "<html", "alert(", "onload=" };
	private static String APLHA_NUMERIC_PATTERN = "^[-a-zA-Z0-9\\s'\u0160\u0152\\u017D\u0161\u0153\u017E\u0178\u00C0-\u00FF]*$";
	private static String DIGIT_AND_SPACE_PATTERN = "[0-9\\s]+";

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static boolean checkForMalicious(String text) {
		String signature = CLASS_NAME + "#checkForMalicious(String text)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "text" }, new Object[] { text }, null, null);

		for (int i = 0; i < MALICIOUS_ARRAY.length; i++) {
			if (text.toLowerCase().contains(MALICIOUS_ARRAY[i])) {
				LoggingWrapper.logExit(log, signature,
						new Object[] { Boolean.TRUE }, entranceTimestamp);
				return Boolean.TRUE;
			}
		}
		LoggingWrapper.logExit(log, signature, new Object[] { Boolean.FALSE },
				entranceTimestamp);
		return Boolean.FALSE;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isAlphaNumeric(String s) {
		String signature = CLASS_NAME + "#isAlphaNumeric(String s)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "s" }, new Object[] { s }, null, null);

		if (s.matches(APLHA_NUMERIC_PATTERN)) {
			LoggingWrapper.logExit(log, signature,
					new Object[] { Boolean.TRUE }, entranceTimestamp);
			return Boolean.TRUE;
		}
		LoggingWrapper.logExit(log, signature, new Object[] { Boolean.FALSE },
				entranceTimestamp);
		return Boolean.FALSE;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isDigitOnly(String s) {
		String signature = CLASS_NAME + "#isDigitOnly(String s)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "s" }, new Object[] { s }, null, null);

		if (s.matches(DIGIT_AND_SPACE_PATTERN)) {
			LoggingWrapper.logExit(log, signature,
					new Object[] { Boolean.TRUE }, entranceTimestamp);
			return Boolean.TRUE;
		}
		LoggingWrapper.logExit(log, signature, new Object[] { Boolean.FALSE },
				entranceTimestamp);
		return Boolean.FALSE;
	}
}