package com.dy.baf.controller.wap.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.SystemInfo;

@Controller(value = "wapFindController")
public class FindController extends WapBaseController {

	@RequestMapping("/index/find")
	public ModelAndView toIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/public/find.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	@RequestMapping("/index/appfind")
	public ModelAndView toAppIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/public/appfind.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
}