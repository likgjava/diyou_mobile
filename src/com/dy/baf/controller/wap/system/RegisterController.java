package com.dy.baf.controller.wap.system;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.service.member.PhoneService;
import com.dy.baf.service.system.RegisterService;
import com.dy.core.exception.DyServiceException;
import com.dy.core.utils.RequestUtil;

/**
 * 
 * 
 * @Description: 注册
 * @author 波哥
 * @date 2015年9月15日 下午4:08:21 
 * @version V1.0
 */
@Controller(value="wapRegisterController")
public class RegisterController extends WapBaseController {
	@Autowired
	private RegisterService mobileRegisterService;
	@Autowired
	private PhoneService phoneService;
	/**
	 * 注册或登录第一步
	 * @return
	 */
	@RequestMapping(value="/system/reglogin",method=RequestMethod.GET)
	public ModelAndView reglogin(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/login2.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 微信注册
	 * @return
	 */
	@RequestMapping(value="/system/wechatRegister",method=RequestMethod.GET)
	public ModelAndView wechatRegister(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/wechatRegister.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 微信注册提交
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/system/wechatRegisterSub",method=RequestMethod.POST)
	public DyPhoneResponse wechatRegisterSub(HttpServletRequest request) throws DyServiceException {
		String account = request.getParameter("account");
		request.getSession().setAttribute("account", account);
		return mobileRegisterService.wechatRegister(account);
	}
	
	/**
	 * 注册或登录页面(第一步)
	 * @param request
	 * @return
	 * @throws DyServiceException 
	 */
	@ResponseBody
	@RequestMapping(value="/system/reglogin",method=RequestMethod.POST)
	public DyPhoneResponse reglogin(HttpServletRequest request) throws DyServiceException {
		String account = request.getParameter("account");
		request.getSession().setAttribute("account", account) ;
		return mobileRegisterService.regLogin(account) ;
	}
	
	/**
	 * 注册第二步
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/system/register2",method=RequestMethod.GET)
	public ModelAndView register2(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/register2.jsp");
			view = this.initCommonPageView(system);
			view.addObject("accounts", this.getSessionAttribute("account"));
			view.addObject("invite", this.getSessionAttribute("invite"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 注册第二步
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/register2",method=RequestMethod.POST)
	public DyPhoneResponse register2Submit(HttpServletRequest request) throws Exception {
		String phone = request.getParameter("phone");
		String password = request.getParameter("password") ;
		Pattern pattern=Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
		Matcher matcher = pattern.matcher(password);
		if(!matcher.matches()){
			DyPhoneResponse response = new DyPhoneResponse();
			response.setCode(DyPhoneResponse.NO);
			response.setDescription("密码只能为数字和字母组合的密码");
			response.setResult(DyPhoneResponse.ERROR);
			return response;
		}
		String referrer = request.getParameter("referrer") ;
		String invite_username = request.getParameter("invite_username") ;
		String ip = this.getRemoteIp() ;
		String type = RequestUtil.getString(request, "type", "");
		String phoneCode = RequestUtil.getString(request, "phone_code", "") ;
		
		Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
		boolean checkResult = phoneService.verifyCode(phone,phoneCode,type,map) ;
		if(!checkResult){
			DyPhoneResponse response = new DyPhoneResponse();
			response.setCode(DyPhoneResponse.NO);
			response.setDescription("验证码错误");
			response.setResult(DyPhoneResponse.ERROR);
			return response;
		}
		
		DyPhoneResponse response = this.mobileRegisterService.fillinfo(phone, password, referrer, invite_username, ip) ;
		this.removeSessionAttribute("invite");
		return response;
	}
}