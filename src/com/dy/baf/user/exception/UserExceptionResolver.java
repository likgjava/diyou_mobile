package com.dy.baf.user.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.support.RequestContext;

import com.dy.core.entity.DyResponse;
import com.dy.core.exception.DyServiceException;

public class UserExceptionResolver extends ExceptionHandlerExceptionResolver {
	protected Logger logger = Logger.getLogger(UserExceptionResolver.class);
	
	@Autowired
    private GsonHttpMessageConverter messageConverter;
	
	@Override
	protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
		if(handlerMethod == null) return null;
        if(handlerMethod.getMethod() == null) return null;
        
		//获取错误信息
		String errorMsg = null;
		if(exception instanceof DyServiceException) {
			DyServiceException serviceException = (DyServiceException) exception;
			if(StringUtils.isNotEmpty(serviceException.getMessage())) {
				if(serviceException.getParams() == null || serviceException.getParams().length <= 0)
					errorMsg = new RequestContext(request).getMessage(serviceException.getMessage());
				else
					errorMsg = new RequestContext(request).getMessage(serviceException.getMessage(), serviceException.getParams());
			}
		}
		
		//判断是否配置@ResponseBody
        if(AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ResponseBody.class) != null) {
        	MediaType jsonMediaType = MediaType.APPLICATION_JSON;
            response.setContentType("application/json;charset=utf-8");
            try {
            	Map<String, Object> result = new HashMap<String, Object>();
            	result.put("status", DyResponse.ERROR);
            	result.put("description", errorMsg == null ? exception.getMessage() : errorMsg);
            	
            	messageConverter.write(result, jsonMediaType, new ServletServerHttpResponse(response));
			} catch (Exception e) {
			}
			
			return null;
        }
        
        exception.printStackTrace();
        ModelAndView modelAndView = new ModelAndView("common/error");
        if(StringUtils.isEmpty(errorMsg)) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
			exception.printStackTrace(new PrintStream(byteArrayOutputStream));
			errorMsg = byteArrayOutputStream.toString();
		}
        modelAndView.addObject("error", "<pre>" + errorMsg + "</pre>");
		
    	return modelAndView;
	}
}