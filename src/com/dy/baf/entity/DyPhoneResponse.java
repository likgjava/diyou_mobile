package com.dy.baf.entity;

import java.io.Serializable;

/**
 * 
 *手机端返回的数据集
 */
public class DyPhoneResponse implements Serializable {

	private static final long serialVersionUID = -6806721658652345974L;

	public static final int OK = 200;

	public static final int NO = 100;

	public static final int LOGINERR = 250;
	
	public static final String SUCCESS = "success";
	
	public static final String ERROR = "error";
	
	private Integer code = NO;
	
	private String result;

	private Object data;

	private Object description = "";
	
	private String xmdy;
	private String diyou;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getDescription() {
		return description;
	}

	public void setDescription(Object description) {
		this.description = description;
	}

	public String getXmdy() {
		return xmdy;
	}

	public void setXmdy(String xmdy) {
		this.xmdy = xmdy;
	}

	public String getDiyou() {
		return diyou;
	}

	public void setDiyou(String diyou) {
		this.diyou = diyou;
	}
	
}