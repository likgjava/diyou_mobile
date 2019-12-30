package com.dy.baf.controller.wechat.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.annotation.WechatLogin;
import com.dy.baf.controller.wechat.WechatBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.system.LoginService;
import com.dy.core.utils.Constant;
import com.dy.httpinvoker.WechatService;

/**
 * 
 * 
 * @Description: 微信菜单访问地址
 * @author 波哥
 * @date 2015年10月19日 下午2:19:45 
 * @version V1.0
 */
@Controller(value = "wechatMenuUrlController")
public class MenuUrlController extends WechatBaseController{

	@Autowired
	private WechatService wechatService;
	@Autowired
	private LoginService loginService;
	
	
	/**
	 * 首頁
	 * @return
	 * @throws Exception 
	 */
	@WechatLogin
	@RequestMapping("/index/index")
	public ModelAndView index() throws Exception{
		ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		system.setContentPage("index/index.jsp");
		view = this.initIndexPageView(system);
		return view;
	}
	
	@WechatLogin
	@RequestMapping("/loan/loantender")
	public ModelAndView loantender() throws Exception{
		ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		system.setContentPage("loan/public/loan.jsp");
		view = this.initIndexPageView(system);
		return view;
	}
	
	@WechatLogin
	@RequestMapping("/transfer/transfer")
	public ModelAndView transfer() throws Exception{
		ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		system.setContentPage("loan/public/transfer.jsp");
		view = this.initIndexPageView(system);
		return view;
	}
	
	/**
	 * 个人中心
	 * @param request
	 * @return
	 */
	@WechatLogin
	@RequestMapping("/member/index")
	public ModelAndView memberIndex() throws Exception {
		ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		if (this.getSession().getAttribute(Constant.SESSION_USER) == null) {
			return new ModelAndView("redirect:/wap/system/login2");
		} else {
			system.setContentPage("member/index.jsp");
		}
		view = this.initIndexPageView(system);
		return view;
	}
	
	/**
	 * 注册
	 * @return
	 */
	@RequestMapping("/system/reglogin")
	public ModelAndView reglogin() throws Exception{
		if (this.getSession().getAttribute(Constant.SESSION_USER) != null) {
			this.getSession().removeAttribute(Constant.SESSION_USER);
		}
		/*ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		system.setContentPage("system/reglogin.jsp");
		view = this.initIndexPageView(system);
		return view;*/
		return new ModelAndView("redirect:/wap/system/reglogin");
	}
	
	/**
	 * 绑定账号
	 * @return
	 * @throws Exception
	 */
	@WechatLogin
	@RequestMapping("/system/bindLogin")
	public ModelAndView bindLogin() throws Exception {
		ModelAndView view = new ModelAndView();
		SystemInfo system = new SystemInfo();
		if (this.getSession().getAttribute(Constant.SESSION_USER) != null) {
			MbMember mbMember = (MbMember) this.getSession().getAttribute(Constant.SESSION_USER);
			system.setContentPage("system/wechatUnbind.jsp");
			view = this.initIndexPageView(system);
			view.addObject("user", mbMember.getName());
		} else {
			system.setContentPage("system/wechatLogin.jsp");
			view = this.initIndexPageView(system);
		}
		return view;
	}
}
