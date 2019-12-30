package com.dy.baf.controller.phone.trust;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.trust.entity.TrustAccount;
import com.dy.core.utils.PropertiesUtil;

@Controller(value = "appTrustTurnController")
public class TrustTurnController extends AppBaseController {
	
	/**
	 * 我的支付账户
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/trust/myTrust")
	public DyPhoneResponse myTrust(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			
			MbMember member =getMember(Long.valueOf(login_token));
			TrustAccount trustAccount = trustManager.getTrustAccountInfo(member.getTrustAccount(), "1".equals(member.getIsAuto()));
			
			Map<String,Object> mapResonse = new HashMap<String, Object>();
			mapResonse.put("trust_account", trustAccount.getTrustAccount());
			mapResonse.put("freezeamount", trustAccount.getFreezeamount());
			mapResonse.put("availableamount", trustAccount.getAvailableamount());
			mapResonse.put("balance", trustAccount.getBalance());
			mapResonse.put("activestatus", "");
			mapResonse.put("membertype", trustAccount.isAuto());
			String url = this.getWebDomain(this.getRequest(),"wapassets/images/payment/pay/"+PropertiesUtil.getProperty("trust.type")+".jpg");
			mapResonse.put("image_url", url);
			
			return successJsonResonse(mapResonse);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}