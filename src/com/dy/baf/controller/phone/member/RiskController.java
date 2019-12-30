package com.dy.baf.controller.phone.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMemberReviews;
import com.dy.baf.entity.common.MbMemberReviewsResult;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.utils.DateUtil;

/**
 * @Description: 风险测评
 * @author panxh
 * @date 2016年8月2日
 * @version V1.0
 */
@Controller(value="appRiskController")
public class RiskController extends AppBaseController {
	
	/**
	 * 风险评测结果
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/answerMobile/result")
	public DyPhoneResponse result(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			Map<String,Object> responseMap = new HashMap<String, Object>();
			
			//判断是否提交过
			QueryItem querySubmit = new QueryItem();
			querySubmit.setWhere(Where.eq("member_id", login_token));
			querySubmit.setOrders("id,create_time asc");
			List<MbMemberReviews> isHaveReviews = this.getListByEntity(querySubmit, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
			
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();		
			if(null == isHaveReviews || isHaveReviews.size() == 0){
				responseMap.put("level","未测评");
				String url = this.getWebDomain(request,"wap/mobileAnswer/record?id="+login_token);
				responseMap.put("echoUrl",url);
				return successJsonResonse(responseMap);
			}
			MbMemberReviews reviews = isHaveReviews.get(isHaveReviews.size()-1);
        	QueryItem queryResult = new QueryItem();
        	queryResult.setFields("id,name");
        	queryResult.setWhere(Where.eq("id", reviews.getLevel()));
        	MbMemberReviewsResult result = this.getOneByEntity(queryResult, Module.MEMBER, Function.MB_REVIEWSRESULT, MbMemberReviewsResult.class);
			
			responseMap.put("level", result.getName());
			String url = this.getWebDomain(request,"wap/mobileAnswer/record?id="+login_token+"&type=echo");
			responseMap.put("echoUrl", url);
			
			return successJsonResonse(responseMap);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 风险测评推介标的
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/answerMobile/introduce")
	public DyPhoneResponse introduceLoan(String xmdy,String diyou) throws Exception {		
			//获取最近发布的三个标的	
			QueryItem item=new QueryItem();
			List<Where> whereList = new ArrayList<Where>();
			whereList.add(Where.eq("status", 3));
			whereList.add(Where.ge("overdue_time", DateUtil.getCurrentTime()));
			item.setOrders("verify_time desc");
			item.setLimit(3);		
			Page loanList =this.getPageByMap(item, Module.LOAN, Function.LN_LOAN);
			List<Map> loanMapList=loanList.getItems();
			for(Map map:loanMapList){
				HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
				String url = this.getWebDomain(request,"wap/loan/appinfoview#?id=");
				map.put("url", url+map.get("id"));		
			}
			return successJsonResonse(loanMapList);
	}
}
