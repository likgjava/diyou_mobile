package com.dy.baf.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CustomHttpServletRequest extends HttpServletRequestWrapper	implements HttpServletRequest {
	private Map<String, String> map;
	
	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public CustomHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getParameter(String name) {
		return map != null ? map.get(name) : super.getParameter(name);
	}

	@Override
	public Map getParameterMap() {
		return map != null ? map : super.getParameterMap();
	}
	
	
}
