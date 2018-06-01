package com.dip.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.liferay.portal.kernel.log.Log;

/**
 * This is a utility class that provides static methods for logging method
 * entrance, method exit and exceptions. Exceptions are logged with stack traces.
 * 
 * Default level for method entrance/exit message is DEBUG, for exceptions -
 * ERROR.
 */

public class LoggingWrapper {
	
	private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(
            "[yyyy-MM-dd HH:mm:ss] ");
    
    /**
     * Logs the method entrance together with input parameters (if present).
     * It's assumed that paramNames and paramValues contain the same number of
     * elements.
     * 
     * @param log
     *            the logger to be used (null if logging is not required to be
     *            performed)
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName)
     * @param paramNames
     *            the names of input parameters (null of method doesn't accept
     *            any parameters)
     * @param paramValues
     *            the values of input parameters
     * @param userId
     *            the logged in user id 
     */  
    public static Date logEntrance(Log log, String signature,
            String[] paramNames, Object[] paramValues, Long userId, String emailId) {
    	Date entranceTime = new Date();
    	if(log.isDebugEnabled()){
	        String timeDate = TIMESTAMP_FORMAT.format(entranceTime);
	        
	        StringBuilder sb = new StringBuilder(timeDate);
	        sb.append(timeDate);
	        sb.append(LoggingHelper.getMethodEntranceMessage(signature));
	        
	        if (paramNames != null) {
		        sb.append(LoggingHelper.getInputParametersMessage(paramNames, paramValues, userId, emailId));
	        }
	        log.debug(sb.toString());
    	}
        return entranceTime;
    }
    
    /**
     * Logs the method exit together with the returned value (if present).
     * 
     * @param log
     *            the logger to be used (null if logging is not required to be
     *            performed)
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName)
     * @param value
     *            the value returned from the method (should contain 1 element
     *            with the returned value, or should be null if the method
     *            returns void)
     * @param entranceTimestamp
     *            the method entrance timestamp (null if not available), is used
     *            for calculating method execution time
     * @param userId
     *            the logged in user id 
     */
    public static void logExit(Log log, String signature, Object[] value,
            Date entranceTimestamp) {
        if (log == null) {
            return;
        } else if(log.isDebugEnabled()) {
	        String timeDate = TIMESTAMP_FORMAT.format(new Date());
	        
	        StringBuilder sb = new StringBuilder(timeDate);
	        sb.append(timeDate);
	        sb.append(LoggingHelper.getMethodExitMessage(signature, entranceTimestamp));
	        
	        if (value != null) {
	        	sb.append(LoggingHelper.getOutputValueMessage(value[0]));
	        }
	        log.debug(sb.toString());
        }
    }
    
    /**
     * Logs the given exception together timestamp.
     * 
     * @param log
     *            the logger to be used (null if logging is not required to be
     *            performed)
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName) where the exception is logged 
     * @param exception
     *            the exception to be logged (assumed to be not null)
     * @return the logged exception
     */
    public static <T extends Throwable> T logException(Log log,
            String signature, String secretCode, String errorKey, T exception) {
        String timeDate = TIMESTAMP_FORMAT.format(new Date());
        String message = timeDate + LoggingHelper.getExceptionMessage(signature, secretCode, errorKey, exception);
        log.error(message);
        return exception;
    }


}
