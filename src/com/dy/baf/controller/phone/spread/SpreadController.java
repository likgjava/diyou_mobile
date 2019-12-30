package com.dy.baf.controller.phone.spread;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.spread.SpreadService;
import com.dy.baf.utils.AppSecurityUtil;

@Controller(value="appSpreadController")
public class SpreadController extends AppBaseController {

	@Autowired
	private SpreadService spreadService;
	
	/**
	 * 我的推广
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/spread/mySpreadAll")
	public DyPhoneResponse mySpread(String xmdy,String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String page = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			return spreadService.mySpreadAll(login_token,page,epage);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 结算记录
	 */
	@ResponseBody
	@RequestMapping("/spread/getSettleLog")
	public DyPhoneResponse getSettleLog(String xmdy,String diyou){
		try {
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String page = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			return spreadService.getSettleLog(login_token,page,epage);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 立即结算
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/spread/doAccount")
	public DyPhoneResponse doAccount(String xmdy,String diyou) throws Exception{
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
		String login_token = paramsMap.get("login_token");
		return spreadService.doAccount(login_token,this.getRemoteIp());
	}
	
}
