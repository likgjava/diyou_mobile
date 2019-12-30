package com.dy.baf.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.dy.baf.entity.common.FnBounty;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;

/**
 * 检测红包是否过期
 * @author panxh
 * at 2016年9月5日
 */
@Component
public class CheckMobileBountyListener implements HttpSessionListener,ServletContextListener {

	@Autowired
	private BaseService baseService;
	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		ServletContext application = arg0.getSession().getServletContext();
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application); 
		baseService = (BaseService) context.getBean("baseService"); 

		QueryItem queryBount = new QueryItem(Module.FINANCE, Function.FN_BOUNTY);
		List<NameValue> andList = new ArrayList<NameValue>();
		andList.add(new NameValue("end_time", DateUtil.getCurrentTime(), "<"));
		andList.add(new NameValue("end_time", null, "is not null", false));
		andList.add(new NameValue("red_status", "1", "="));
		andList.add(new NameValue("period", "0", "!="));
		queryBount.setWhere(Where.setAndList(andList));
		List<FnBounty> bountyList = new ArrayList<FnBounty>();
		try {
			bountyList = baseService.getList(queryBount, FnBounty.class);
			for(FnBounty bounty : bountyList){
				bounty.setRedStatus(-1);
				baseService.updateById(Module.FINANCE, Function.FN_BOUNTY, bounty);
			}
		} catch (DyServiceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
	}
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
	}
}
