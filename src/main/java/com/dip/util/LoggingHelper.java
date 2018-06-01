package com.dip.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import com.liferay.portal.kernel.util.StringPool;

/**
 * This is a static helper class that provides log message generation
 * functionality for LoggingWrapper.
 */
public class LoggingHelper {

    /**
     * Retrieves the method entrance log message.
     * 
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName)
     * @return the constructed method entrance message
     */
    public static String getMethodEntranceMessage(String signature) {
        return "Entering method [" + signature + "].";
    }
    
    /**
     * Retrieves the log message for the given input parameters. It's assumed
     * that paramNames and paramValues contain the same number of elements.
     * 
     * @param paramNames
     *            the names of input parameters (not null)
     * @param paramValues
     *            the values of input parameters (not null)
     * @return the constructed log message
     */
    public static String getInputParametersMessage(String[] paramNames,
            Object[] paramValues, Long userId, String emailId) {
        StringBuilder sb = new StringBuilder(" Input parameters [");
        for (int i = 0; i < paramNames.length; i++) {
            if (i!=0) {
                sb.append(StringPool.SPACE + StringPool.COMMA);
            }
            sb.append(paramNames[i]).append(StringPool.COLON);
            sb.append(paramValues[i]);
        }
        sb.append("]");
        if(userId != null && emailId != null) {
            sb.append(getLoggedInUserMessage(userId, emailId));
        }
        return sb.toString();
    }
    
    /**
     * 
     * @param userId
     * @param emailId
     * @return
     */
    public static String getLoggedInUserMessage(long userId, String emailId) {
    	 return ". Logged in user id [" + userId + "] and user email id [" + emailId + "].";
    }
    
    /**
     * Retrieves the method exit log message..
     * 
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName)
     * @param entranceTimestamp
     *            the method entrance timestamp (null if not available), is used
     *            for calculating method execution time
     *           
     * @return the constructed method exit message
     */
    public static String getMethodExitMessage(String signature,
            Date entranceTimestamp) {
        StringBuilder sb = new StringBuilder("Exiting method [");
        sb.append(signature).append("]");
        if (entranceTimestamp !=null) {
            sb.append(", time spent in the method: ");
            sb.append((new Date()).getTime() - entranceTimestamp.getTime());
            sb.append(" milliseconds");
        }
        sb.append(".");
        
        return sb.toString();
    }
    
    /**
     * Retrieves the log message for the given method output value.
     * 
     * @param value
     *            the value returned by the method
     * @return the constructed log message
     */
    public static String getOutputValueMessage(Object value) {
        return " Output parameter: " + value;
    }
    
    /**
     * Retrieves the exception log message.
     * 
     * @param exception
     *            the exception to be logged (assumed to be not null)
     * @param signature
     *            the signature that uniquely identifies the method (e.g.
     *            className#methodName)
     * @return the retrieved exception message
     */
    public static String getExceptionMessage(String signature, String secretCode, String errorKey,
            Throwable exception) {
        StringBuilder sb = new StringBuilder("Error in method [");
        sb.append(signature).append("], details: ").append(exception.getMessage());
        if(secretCode != null){
        	sb.append(". Secret code: ").append(secretCode);
        }
        if(errorKey != null){
        	sb.append(". Error Key: ").append(errorKey);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        ps.println();
        exception.printStackTrace(ps);
        sb.append(out.toString());
        
        return sb.toString();
    }
    
}
