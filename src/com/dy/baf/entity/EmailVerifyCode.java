package com.dy.baf.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;

public class EmailVerifyCode implements Serializable {
	private static final long serialVersionUID = -3964504952071535016L;

	private String email;
	
	private String verifyCode;
	
	private String remoteIp;
	
	private long time;
	
	private int verifyTimes;
	
	private String errorMsg;
	
	private List<String> emailList;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getVerifyTimes() {
		return verifyTimes;
	}

	public void setVerifyTimes(int verifyTimes) {
		this.verifyTimes = verifyTimes;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public List<String> getEmailList() {
		return emailList;
	}

	public void setEmailList(List<String> emailList) {
		this.emailList = emailList;
	}

	private boolean isEmailExist(String email) {
		return getEmailList().contains(email);
	}

	/**
	 * 获取邮箱验证码
	 * @param email 邮箱
	 * @param verifyCode 验证码
	 * @param timeBetween 时间间隔(秒)
	 * @return
	 */
	public static EmailVerifyCode getCode(String email, String verifyCode, int timeBetween) {
		EmailVerifyCode emailVerifyCode = null;
		List<String> emailList = new ArrayList<String>();
		
		Object obj = getRequest().getSession().getAttribute(Constant.SESSION_EMAIL_CODE);
		if(obj != null) {
			emailVerifyCode = (EmailVerifyCode)obj;
			if(emailVerifyCode.isEmailExist(email)
					&& DateUtil.getCurrentTime() - emailVerifyCode.getTime() < timeBetween) {
				emailVerifyCode.setErrorMsg("操作太频繁，两次发送时间间隔不能小于" + timeBetween + "秒");
				return emailVerifyCode;
			}
			
			emailList = emailVerifyCode.getEmailList();
		}
		emailList.add(email);
		
		emailVerifyCode = new EmailVerifyCode();
		emailVerifyCode.setEmail(email);
		emailVerifyCode.setTime(DateUtil.getCurrentTime());
		emailVerifyCode.setVerifyCode(verifyCode);
		emailVerifyCode.setVerifyTimes(0);
		emailVerifyCode.setRemoteIp(getRequestIp());
		emailVerifyCode.setEmailList(emailList);
		getRequest().getSession().setAttribute(Constant.SESSION_EMAIL_CODE, emailVerifyCode);
		return emailVerifyCode;
	}
	
	/**
	 * 邮箱验证码校验
	 * @param email 手机号码
	 * @param verifyCode 验证码
	 * @param maxErrorTimes 最低错误次数
	 * @return
	 */
	public static String validate(String email, String verifyCode, int maxErrorTimes) {
		if(StringUtils.isBlank(verifyCode)) return "验证码不能为空";
		
		Object obj = getRequest().getSession().getAttribute(Constant.SESSION_EMAIL_CODE);
		if(obj == null || !(obj instanceof EmailVerifyCode)) return "验证码过期或无效，请重新获取";
		
		EmailVerifyCode sessionVerifyCode = (EmailVerifyCode)obj;
		if((email != null && !email.equals(sessionVerifyCode.getEmail()))
				|| !verifyCode.equals(sessionVerifyCode.getVerifyCode())
				|| !getRequestIp().equals(sessionVerifyCode.getRemoteIp())) {
			if(sessionVerifyCode.getVerifyTimes() >= maxErrorTimes) {
				getRequest().getSession().removeAttribute(Constant.SESSION_EMAIL_CODE);
				return "验证码过期或无效，请重新获取";
			}
			
			sessionVerifyCode.setVerifyTimes(sessionVerifyCode.getVerifyTimes() + 1);
			getRequest().getSession().setAttribute(Constant.SESSION_EMAIL_CODE, sessionVerifyCode);
			
			return "验证码错误";
		}
		
		sessionVerifyCode.setVerifyTimes(0);
		getRequest().getSession().setAttribute(Constant.SESSION_EMAIL_CODE, sessionVerifyCode);
		return null;
	}
	
	public static String getSessionEmail() {
		Object obj = getRequest().getSession().getAttribute(Constant.SESSION_EMAIL_CODE);
		if(obj == null || !(obj instanceof EmailVerifyCode)) return null;
		
		EmailVerifyCode sessionVerifyCode = (EmailVerifyCode)obj;
		return sessionVerifyCode.getEmail();
	}
	
	private static HttpServletRequest getRequest() {
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	private static String getRequestIp() {
		HttpServletRequest request = getRequest();
		String ip = request.getHeader("x-forwarded-for");
		if(ip == null) ip = request.getRemoteAddr();
		if("0:0:0:0:0:0:0:1".equals(ip)) ip = "127.0.0.1";
		return ip;
	}
}