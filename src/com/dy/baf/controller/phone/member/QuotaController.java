package com.dy.baf.controller.phone.member;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.member.QuotaService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.IpUtil;

/**
 * @Description: 用户额度管理
 * @author 波哥
 * @date 2015年11月12日 上午11:19:16 
 * @version V1.0
 */
@Controller(value="appQuotaController")
public class QuotaController extends AppBaseController {
	
	@Autowired
	private QuotaService quotaService;


	/**
	 * 额度类型列表
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/amountTypeList")
	public DyPhoneResponse amountTypeList(String xmdy,String diyou){
		try {
			return this.quotaService.amountTypeList();
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 额度申请
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/apply")
	public DyPhoneResponse apply(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String amount_type = paramsMap.get("amount_type");
			String amount = paramsMap.get("amount");
			String remark = paramsMap.get("remark");
			return this.quotaService.quotaApply(login_token, amount_type, amount, remark, IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 额度记录
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/amountApplyList")
	public DyPhoneResponse amountApplyList(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String page = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			return this.quotaService.amountApplyList(login_token, page, epage);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 我的额度
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/myAmount")
	public DyPhoneResponse myAmount(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.quotaService.myAmount(login_token);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
