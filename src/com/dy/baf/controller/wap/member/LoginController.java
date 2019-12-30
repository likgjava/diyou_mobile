package com.dy.baf.controller.wap.member;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.service.system.LoginService;
import com.dy.core.exception.DyServiceException;
import com.dy.core.utils.Constant;
import com.dy.core.utils.IpUtil;

@Controller(value="wapLoginController")
public class LoginController extends WapBaseController {
	@Autowired
	private LoginService loginService ;
	/**
	 * 手机登录(第二步)
	 * @param request
	 * @return
	 * @throws DyServiceException 
	 */
	@RequestMapping(value="/system/login",method=RequestMethod.GET)
	public ModelAndView login(HttpServletRequest request) throws DyServiceException {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/login.jsp");
			view = this.initCommonPageView(system);
			view.addObject("accounts", this.getSessionAttribute("account")) ;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 邮箱账号登录(第二步)
	 * @param request
	 * @return
	 * @throws DyServiceException 
	 */
	@RequestMapping(value="/system/login2",method=RequestMethod.GET)
	public ModelAndView login2(HttpServletRequest request) throws DyServiceException {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/login2.jsp");
			view = this.initCommonPageView(system);
			view.addObject("accounts", this.getSessionAttribute("account")) ;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 登陆
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/login",method=RequestMethod.POST)
	public DyPhoneResponse loginSub(HttpServletRequest request) throws Exception {
		long ip = 0L;
		try {
			System.out.println("----------ip--------------"+this.getRemoteIp());
			ip = IpUtil.ipStrToLong(this.getRemoteIp());
		} catch (Exception e) {
			
		}
		String memberName = request.getParameter("account"); 
		String logintype = request.getParameter("logintype");
		String code = request.getParameter("phoneCode");
		String password = request.getParameter("password");
		return loginService.login(memberName, password, code, logintype, null, null, this.getSession(),ip);
	}
	
	/**
	 * 退出登录
	 * @return
	 */
	@RequestMapping("/member/exit")
	public ModelAndView exit(){
		//销毁session
		this.removeSessionAttribute(Constant.SESSION_USER);
		this.removeSessionAttribute(Constant.SESSION_VERIFY_CODE);
		this.removeSessionAttribute(Constant.SESSION_DATE_FORMAT_TYPE);
		this.removeSessionAttribute("member_info");
		
		//Shiro销毁
		Subject subject = SecurityUtils.getSubject(); 
		subject.logout();
		
		return new ModelAndView("redirect:/wap/index/index");
	}
}
