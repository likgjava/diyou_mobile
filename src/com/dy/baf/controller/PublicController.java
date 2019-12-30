package com.dy.baf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.entity.SystemInfo;


/**
 * 
 * 
 * @Description: 消息提示页面
 * @author 波哥
 * @date 2015年10月13日 上午11:06:22 
 * @version V1.0
 */
@Controller(value="publicController")
public class PublicController extends FrontBaseController {
	

	/**
	 * 消息提示页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "public/public/msg", method = RequestMethod.GET)
	public ModelAndView toTrustMsg(@ModelAttribute("status") String status,
			@ModelAttribute("message") String message,
			@ModelAttribute("url") String url,
			@ModelAttribute("type") String type) throws Exception {
		ModelAndView view = new ModelAndView("common");
		SystemInfo system = new SystemInfo();
		system.setContentPage("trust/msg.jsp");
		system.setThemeDir("wapassets/trust");
		view.addObject("system", system);
		view.addObject("status", status);
		view.addObject("message", message);
		String temp = typeConver(type) + "_status="+("SUCCESS".equals(status) ? "success" : "fail");
		view.addObject("url", url.endsWith("?") ? (url + temp) : (url + "?"+temp));
		view.addObject("type", temp);
		return view;
	}
	
	
	/**
	 * 操作类型转换（兼容pc与app）
	 * @param type
	 * @return
	 */
	public static String typeConver(String type){
		
		if("tender".equals(type)){
			return "invest";
		}
		if("buyTransfer".equals(type)){
			return "buy";
		}
		return type.toLowerCase();
	}
}
