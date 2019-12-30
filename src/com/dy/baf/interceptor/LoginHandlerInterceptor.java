package com.dy.baf.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.dy.baf.entity.common.FnBounty;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;

public class LoginHandlerInterceptor extends HandlerInterceptorAdapter {


	public static final String FRONT_INTERCEPTOR_PATH = ".*/((content)|(articles)|(system)|(public)|(index)|(common))/.*";

	@Autowired
	private BaseService baseService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String path = request.getServletPath();
		
		//app、微信 请求地址不需要拦截
		if(path.contains("/phone/") || path.contains("/wechat/")|| path.contains("/mobileAnswer/")){
			if(path.contains("/phone/")){
				checkBount();
			}
			return true;
		}
		//第三方托管不用拦截
		if(path.matches("/.*/.*/.*/((notify)|(return))")) return true;
		
		// 判断请求是否需要拦截
		if (path.matches(Constant.NO_INTERCEPTOR_PATH) || path.matches(FRONT_INTERCEPTOR_PATH))
			return true;

		List<String> urlList = getUrlList();
		for (String newPath : urlList) {
			if (path.matches(newPath)) {
				return true;
			}
		}
		// 判断用户是否登陆，未登陆就返回到登录页面
		if (SecurityUtils.getSubject().getSession().getAttribute(Constant.SESSION_USER) == null) {
			response.sendRedirect(request.getContextPath() + "/wap/system/login2");
			return false;
		}
		return true;
	}

	/**
	 * 免拦截的url //TODO 后面要转成静态
	 */
	public List<String> getUrlList() {
		List<String> list = new ArrayList<String>();
		list.add("/wap/index/banner");
		list.add("/wap/transfer/transfer");
		list.add("/wap/transfer/transferList");
		list.add("/wap/transfer/transferView");
		list.add("/wap/transfer/transferInfo");
		list.add("/wap/transfer/appTransferView");
		list.add("/wap/member/sendemail") ;
		list.add("/wap/transfer/transferAgreement") ;
		list.add("/wap/transfer/appTransferAgreement") ;
		list.add("/wap/transfer/getAgreeInfo") ;
		
		list.add("/wap/loan/loantender") ;
		list.add("/wap/loan/loantenderdata") ;
		list.add("/wap/loan/loaninfoview") ;
		list.add("/wap/loan/appinfoview") ;
		list.add("/wap/loan/loanInfo") ;
		list.add("/wap/loan/loanProtocol") ;
		list.add("/wap/loan/loanContract") ;
		list.add("/wap/loan/appLoanAgreement") ;
		list.add("/wap/loan/loanProtocolAgreement") ;
		list.add("/wap/loan/getAgreeInfo") ;
		list.add("/wap/loan/getRepayTypeList") ;
		list.add("/wap/loan/loantotal") ;
		list.add("/wap/loan/getLoanSum") ;
		list.add("/wap/risk/levelData") ;
		list.add("/wap/index/loanTopThree") ;
		list.add("/wap/index/find") ;
		list.add("/wap/index/appfind") ;
		list.add("/wap/index/siteConfig") ;
		list.add("/wap/loan/getLoanSumPer") ;
		list.add("/MP_verify_vL3DZ4DRqaGInMc7.txt") ;
		return list;
	}
	
	/**
	 * 更新红包已过期状态
	 */
	private void checkBount(){
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
}