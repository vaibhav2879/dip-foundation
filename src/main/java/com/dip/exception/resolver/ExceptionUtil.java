package com.dip.exception.resolver;

import java.util.Date;

import com.dip.util.LoggingWrapper;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class ExceptionUtil {
	
	private static Log log = LogFactoryUtil.getLog(ExceptionUtil.class);
	private static final String CLASS_NAME = ExceptionUtil.class.getName();
	
	private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String NUMBERS = "0123456789";

	/**
	 * Default implementation generates a 4-character String of letters/numbers
	 * used as Error Code
	 * 
	 * @return String
	 */
	public static String generateSecretErrorCode() {
		String signature = CLASS_NAME
				+ "#generateSecretErrorCode()";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] {}, new Object[] {}, null, null);
		String secretCode = pickRandomChar(ALPHABETS) + pickRandomChar(ALPHABETS)
				+ pickRandomChar(NUMBERS) + pickRandomChar(NUMBERS);
		LoggingWrapper.logExit(log, signature, new Object[] { secretCode },
				entranceTimestamp);
		return secretCode;
	}

	/**
	 * The method to pick a random char to create exception code.
	 * 
	 * @param source
	 *            	The character combination.
	 * @return String
	 */
	public static String pickRandomChar(String source) {
		String signature = CLASS_NAME
				+ "#pickRandomChar(String source)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] {"source"}, new Object[] {source}, null, null);
		int pos = (int) (source.length() * Math.random());
		String randomString = String.valueOf(source.charAt(pos));
		LoggingWrapper.logExit(log, signature, new Object[] { randomString },
				entranceTimestamp);
		return randomString;
	}
}
