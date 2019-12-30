package com.dy.baf.controller.wap.member;

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
import com.dy.baf.service.spread.SpreadLogService;
import com.dy.baf.service.spread.SpreadService;
import com.dy.core.utils.StringUtils;
/**
 * 我的推广
 */
@Controller(value="wapSpreadController")
public class SpreadController extends WapBaseController {
	@Autowired 
	private SpreadService spreadService ;
	@Autowired
	private SpreadLogService spreadLogService ;
	/**
	 * 我的推广
	 * @return
	 */
	@RequestMapping(value="/spread/mySpread",method=RequestMethod.GET)
	public ModelAndView mySpread(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("spread/mySpread.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我的推广列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/spread/mySpread",method=RequestMethod.POST)
	public DyPhoneResponse mySpread(HttpServletRequest request){
		try {
			String memberId = this.getMemberId().toString() ;
			String page = this.getRequest().getParameter("page") ;
			return spreadService.mySpread(memberId, page) ;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 我的推广信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/spread/mySpreadData",method=RequestMethod.POST)
	public DyPhoneResponse mySpreadData(){
		try {
			String memberId = this.getMemberId().toString() ;
			return spreadService.mySpreadData(memberId);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 推广记录
	 * @return
	 */
	@RequestMapping(value="/spread/spreadLog",method=RequestMethod.GET)
	public ModelAndView spreadLog(String name){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("spread/spreadLog.jsp");
			view = this.initCommonPageView(system);
			view.addObject("name", StringUtils.isNotBlank(name) ? name : null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 推广记录数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/spread/spreadLog",method=RequestMethod.POST)
	public DyPhoneResponse spreadLog(HttpServletRequest request){
		try {
			String page = request.getParameter("page");
			String name = request.getParameter("name");
			return spreadLogService.getSpreadLog(this.getMemberId().toString(), name, page, null);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 推广记录
	 * @return
	 */
	@RequestMapping(value="/spread/settleLog",method=RequestMethod.GET)
	public ModelAndView settleLog(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("spread/settleLog.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 推广记录数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/spread/settleLog",method=RequestMethod.POST)
	public DyPhoneResponse settleLog(HttpServletRequest request){
		try {
			String page = request.getParameter("page");
			return spreadService.getSettleLog(this.getMemberId().toString(), page, null);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 立即结算
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/spread/doAccount",method=RequestMethod.POST)
	public DyPhoneResponse doAccount(){
		try {
			return spreadService.doAccount(this.getMemberId().toString(),this.getRemoteIp());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
