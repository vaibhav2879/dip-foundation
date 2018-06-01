package com.dip.exception.resolver;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.portlet.HandlerExceptionResolver;
import org.springframework.web.portlet.ModelAndView;

import com.dip.exception.SystemServiceException;
import com.dip.exception.DipPortletAjaxException;
import com.dip.exception.DipPortletException;
import com.dip.util.ErrorCodes;
import com.dip.util.LoggingWrapper;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;

/**
 * @author HCL
 *
 */
public class PortletExceptionResolver implements HandlerExceptionResolver {

	private Log log = LogFactoryUtil.getLog(PortletExceptionResolver.class);

	private static final String CLASS_NAME = PortletExceptionResolver.class.getName();

	private static final String EXCEPTION_DETAILS = "exceptionDetails";
	private static final String ERROR_VIEW = "error";

	/**
	 * @param portletRequest
	 * @param portletResponse
	 * @param exception
	 * @return
	 * 
	 * 		   The method checks if the exception thrown is an
	 *         DipPortletException or SystemServiceException or
	 *         DipPortletAjaxException and the corresponding actions are
	 *         performed. The SystemServiceException will be redirected to the
	 *         default error page and the DipPortletException will be handled
	 *         by creating a json object containing the error details.
	 */
	private ModelAndView handleException(PortletRequest portletRequest, PortletResponse portletResponse,
			Exception exception) {
		String signature = CLASS_NAME
				+ "#handleException(PortletRequest portletRequest, PortletResponse portletResponse, Exception exception)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "portletRequest", "portletResponse", "exception" },
				new Object[] { portletRequest, portletResponse, exception }, null, null);

		ModelAndView modelAndView = new ModelAndView();
		ExceptionDetails details = null;

		if (exception instanceof DipPortletException) {
			log.info("Handling DipPortletException Exception type");
			details = new ExceptionDetails(exception, new Date(), "",
					((DipPortletException) exception).getSecretCode());
			JSONObject errorDetails = JSONFactoryUtil.createJSONObject();
			errorDetails.put("message", details.getException().getMessage());
			errorDetails.put("errorcode", details.getSecretErrorCode());

			Map<String, String> errorMap = new HashMap<String, String>();
			errorMap.put("message", details.getException().getMessage());
			errorMap.put("errorcode", details.getSecretErrorCode());
			modelAndView.addObject("errorMap", errorMap);
			modelAndView.addObject("errorDetails", errorDetails);
			modelAndView.setViewName(ERROR_VIEW);
		} else if (exception instanceof SystemServiceException) {
			log.info("Handling SystemServiceException Exception type");
			details = new ExceptionDetails(exception, new Date(), StringPool.BLANK,
					((SystemServiceException) exception).getSecretCode());
			portletRequest.setAttribute(EXCEPTION_DETAILS, details);
			modelAndView.setViewName(ERROR_VIEW);
		} else if (exception instanceof DipPortletAjaxException) {
			log.info("Handling DipPortletAjaxException Exception type");
			details = new ExceptionDetails(exception, new Date(), StringPool.BLANK,
					((DipPortletAjaxException) exception).getSecretCode());
			portletRequest.setAttribute(EXCEPTION_DETAILS, details);
			portletResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
					Integer.toString(HttpServletResponse.SC_EXPECTATION_FAILED));
			JSONObject errorDetails = JSONFactoryUtil.createJSONObject();
			errorDetails.put("message", details.getException().getMessage());
			errorDetails.put("errorcode", details.getSecretErrorCode());

			if (portletResponse instanceof ResourceResponse) {
				ResourceResponse resourceResponse = (ResourceResponse) portletResponse;
				resourceResponse.setContentType(ContentTypes.TEXT_HTML);
				try {
					resourceResponse.getWriter()
							.write(details.getSecretErrorCode());
				} catch (IOException ex) {
					final DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00035,
							ErrorCodes.map.get(ErrorCodes.DIP_ERR_00035), ex);
					LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
							portletException.getErrorKey(), portletException);
				}
			} else if (portletResponse instanceof RenderResponse) {
				RenderResponse renderResponse = (RenderResponse) portletResponse;
				renderResponse.setContentType(ContentTypes.TEXT_HTML);
				try {
					renderResponse.getWriter()
							.write(details.getSecretErrorCode());
				} catch (IOException ex) {
					final DipPortletException portletException = new DipPortletException(ErrorCodes.DIP_ERR_00035,
							ErrorCodes.map.get(ErrorCodes.DIP_ERR_00035), ex);
					LoggingWrapper.logException(log, signature, portletException.getSecretCode(),
							portletException.getErrorKey(), portletException);
				}
			}

			modelAndView = null;
		} else {
			String secretCode = ExceptionUtil.generateSecretErrorCode();
			details = new ExceptionDetails(exception, new Date(), StringPool.BLANK, secretCode);
			LoggingWrapper.logException(log, signature, secretCode, ErrorCodes.DIP_UNEXP_ERR, exception);
			portletRequest.setAttribute(EXCEPTION_DETAILS, details);
			modelAndView.setViewName(ERROR_VIEW);
		}
		
		LoggingWrapper.logExit(log, signature, new Object[] { modelAndView }, entranceTimestamp);
		return modelAndView;
	}
	/**
	 * @param renderRequest
	 * @param renderResponse
	 * @param object
	 * @param exception
	 * @return
	 */
	public ModelAndView resolveException(RenderRequest renderRequest, RenderResponse renderResponse, Object object,
			Exception exception) {
		String signature = CLASS_NAME
				+ "#resolveException(RenderRequest renderRequest, RenderResponse renderResponse, Object object, Exception exception)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "renderRequest", "renderResponse", "object", "exception" },
				new Object[] { renderRequest, renderResponse, object, exception }, null, null);

		ModelAndView modelAndView = handleException(renderRequest, renderResponse, exception);
		LoggingWrapper.logExit(log, signature, new Object[] { modelAndView }, entranceTimestamp);
		return modelAndView;
	}
	/**
	 * @param renderRequest
	 * @param renderResponse
	 * @param object
	 * @param exception
	 * @return
	 */
	public ModelAndView resolveException(ResourceRequest resourceRequest, ResourceResponse resourceResponse,
			Object object, Exception exception) {
		String signature = CLASS_NAME
				+ "#resolveException(ResourceRequest resourceRequest, ResourceResponse resourceResponse, Object object, Exception exception)";
		Date entranceTimestamp = LoggingWrapper.logEntrance(log, signature,
				new String[] { "resourceRequest", "resourceResponse", "object", "exception" },
				new Object[] { resourceRequest, resourceResponse, object, exception }, null, null);
		ModelAndView modelAndView = handleException(resourceRequest, resourceResponse, exception);
		LoggingWrapper.logExit(log, signature, new Object[] { modelAndView }, entranceTimestamp);
		return modelAndView;
	}
}
