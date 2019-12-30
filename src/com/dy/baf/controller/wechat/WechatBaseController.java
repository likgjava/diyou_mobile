package com.dy.baf.controller.wechat;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.PropertiesUtil;

@RequestMapping("/wechat")
public class WechatBaseController extends FrontBaseController{

	
	protected static String THEME_DIR = null;
	static {
		if("1".equals(PropertiesUtil.getProperty("trust.enable"))) {
			THEME_DIR = "wapassets/trust";
		} else {
			THEME_DIR = "wapassets/default";
		}
	}
	public ModelAndView initIndexPageView(SystemInfo system) throws Exception {
		ModelAndView view = new ModelAndView("index");
		view.addObject("system", system);
		view.addObject("system", this.getSystemInfo(system));	
		view.addObject("isBottom",false);
		return view;
	}
	
	

	private SystemInfo getSystemInfo(SystemInfo system) throws Exception{
		StringBuffer nids = new StringBuffer();
		nids.append("site_name,");
		nids.append("site_ico,");
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
				} 
			}
		}
		system.setThemeDir(THEME_DIR);
		return system;
	}
}
