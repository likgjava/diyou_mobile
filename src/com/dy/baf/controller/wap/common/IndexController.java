package com.dy.baf.controller.wap.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.common.IndexService;
import com.dy.core.utils.Constant;

@Controller(value = "wapIndexController")
public class IndexController extends WapBaseController {

	@Autowired
	private IndexService indexService;
	
	@RequestMapping
	public ModelAndView index(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			//判断用户是否已经登陆过
			system.setContentPage("index/index.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}

	@RequestMapping("/index/index")
	public ModelAndView toIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("index/index.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}

	/**
	 * 首页banner图
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/index/banner")
	public Map banner() throws Exception {		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("banner", this.indexService.banner());
		return map;
	}

	@ResponseBody
	@RequestMapping("/index/loanone")
	public Map loan() throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
		List<Map> listMap = this.indexService.loan();
		if(listMap != null && !listMap.isEmpty()){
			List loanList = (List) dataConvert(listMap,
					"repay_type_name:getRepayType,category_name:getBorrowType,status:getBorrowStatus");
			Map<String, String> loanMap = (Map<String, String>) loanList.get(0);
			loanMap.put("url", "/loan/index");
			loanMap.put("slogan_one", "本金保障");
			loanMap.put("slogan_two", "灵活投资");
			loanMap.put("slogan_three", "稳定收益");
			responseMap.put("loan_one", loanMap);
		}
		
		// 新标
		Map previewMap = this.indexService.preview();
		
//		if("5".equals(previewMap.get("repay_type_id").toString())){
//			previewMap.put("period", previewMap.get("period")+"天");
//		}else{
//			previewMap.put("period", previewMap.get("period")+"个月");
//		}
		responseMap.put("new_loan", dataConvert(previewMap, "repay_type_name:getRepayType"));
		return responseMap;
	}
	
	@RequestMapping("/index/settings")
	public ModelAndView settings(Model model) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("index/settings.jsp");
			view = this.initIndexPageView(system);
			//判断用户是否已经登陆过
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if(member == null)  {
				view.addObject("is_login", "");
			}else{
				view.addObject("is_login", member.getId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
}