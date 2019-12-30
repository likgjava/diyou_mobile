package com.dy.baf.controller.wap.trust;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.trust.entity.TrustAccount;

/**
 * 托管
 * @author Administrator
 */
@Controller(value="wapTrustController")
public class TrustController extends WapBaseController {

	/**
	 * 我的支付账号
	 * @return
	 */
	@RequestMapping(value="/trust/myTrust",method=RequestMethod.GET)
	public ModelAndView myTrustPage(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("trust/my_trust.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	@ResponseBody
	@RequestMapping(value="/trust/myTrust",method=RequestMethod.POST)
	public DyPhoneResponse myTrust(){
		try {
			MbMember member = getMember(getMemberId());
			TrustAccount trustAccount = trustManager.getTrustAccountInfo(member.getTrustAccount(), "1".equals(member.getIsAuto()));
			trustAccount.setType(trustType);
			boolean isYeepay = true;
			if(!trustManager.YEEPAY.equals(trustType)){
				isYeepay = false;
			}
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("trustAccount", trustAccount);
			map.put("isYeepay", isYeepay);
			return successJsonResonse(map);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
