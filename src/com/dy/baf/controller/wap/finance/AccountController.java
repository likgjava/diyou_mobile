/**
 * 
 */
package com.dy.baf.controller.wap.finance;

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
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.finance.AccountLogService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.RequestUtil;

/**
 *	用户资金
 */
@Controller(value="wapAccountController")
public class AccountController extends WapBaseController{
	
	@Autowired
	private AccountLogService accountLogService;
	/**
	 * 用户资金记录
	 * @return
	 */
	@RequestMapping(value="/account/accountLog",method=RequestMethod.GET)
	public ModelAndView accountLog() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/accountlog.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 用户资金记录数据请求
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/account/accountLog",method=RequestMethod.POST)
	public DyPhoneResponse accountLog(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
		Integer page = RequestUtil.getInteger(request, "page", 1) ;
		return accountLogService.accountLog(member.getId(),page) ;
	}
}
