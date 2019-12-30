package com.dy.baf.controller.wap;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.PropertiesUtil;

@RequestMapping("wap")
public class WapBaseController extends FrontBaseController {
	
	protected static String THEME_DIR = null;
	static {
		if("1".equals(PropertiesUtil.getProperty("trust.enable"))) {
			THEME_DIR = "wapassets/trust";
		} else {
			THEME_DIR = "wapassets/default";
		}
	}
	
	public ModelAndView initMemberPageView(SystemInfo system) throws Exception {
		ModelAndView view = new ModelAndView("member");
		system.setThemeDir(THEME_DIR);
		view.addObject("system", system);
		return view;
	}
	
	public ModelAndView initIndexPageView(SystemInfo system) throws Exception {
		ModelAndView view = new ModelAndView("index");
		view.addObject("system", this.getSystemInfo(system));	
		return view;
	}
	public ModelAndView initCommonPageView(SystemInfo system) throws Exception {
		ModelAndView view = new ModelAndView("common");
		view.addObject("system", this.getSystemInfo(system));
		return view;
	}
	public ModelAndView initSystemPageView(SystemInfo system) throws Exception {
		ModelAndView view = new ModelAndView("system");
		view.addObject("system", this.getSystemInfo(system));
		return view;
	}
	
	
	
	
	private SystemInfo getSystemInfo(SystemInfo system) throws Exception{
		StringBuffer nids = new StringBuffer();
		nids.append("site_name,");
		nids.append("site_ico,");
		nids.append("service_tel,");
		nids.append("service_hours,");
		QueryItem queryItem = new QueryItem("system", "config");
		queryItem.setFields("id, nid, name, value, status");
		queryItem.getWhere().add(Where.in("nid", nids.toString()));
		List<SysSystemConfig> configList = (List<SysSystemConfig>) this.getList(queryItem, SysSystemConfig.class);
		String imageHost = PropertiesUtil.getImageHost();
		if (configList != null && configList.size() > 0) {
			for (SysSystemConfig config : configList) {
				if ("site_name".equals(config.getNid())) {
					system.setSiteName(config.getValue());
				} else if ("site_ico".equals(config.getNid())) {
					system.setSiteIco(imageHost + config.getValue());
				} else if("service_tel".equals(config.getNid())){
					system.setServiceTel(config.getValue()) ;
				} else if("service_hours".equals(config.getNid())){
					system.setServiceHours(config.getValue()) ;
				}
			}
		}
		system.setThemeDir(THEME_DIR);
		return system;
	}
}
