package com.dy.baf.controller.phone.loan;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.loan.RepayMobileService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.IpUtil;

/**
 * 
 * 
 * @Description: 还款
 * @author 波哥
 * @date 2015年11月15日 上午9:43:54 
 * @version V1.0
 */
@Controller(value="appTenderRepayController")
public class RepayController extends AppBaseController {
	@Autowired
	private RepayMobileService repayMobileService;
	
	/**
	 * 获取还款页面数据
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/repay")
	public DyPhoneResponse repay(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("id");
			return this.repayMobileService.getRepayData(id, login_token, isTrust, id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
		
	}

	/**
	 * 正常还款
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/repaySub")
	public DyPhoneResponse repaySub(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String loan_id = paramsMap.get("loan_id");
			String repay_id = paramsMap.get("repay_id");
			String paypassword = paramsMap.get("paypassword");
			return this.repayMobileService.repaySubmit(login_token, repay_id, paypassword, IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
	
	
	/**
	 * 提前还款数据
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/repayAdvance")
	public DyPhoneResponse repayAdvance(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("id");
			return this.repayMobileService.getRepayAdvanceData(id, login_token, isTrust, "");
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 提前还款
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/repayAdvanceSub")
	public DyPhoneResponse repayAdvanceSub(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String loan_id = paramsMap.get("loan_id");
			String repay_id = paramsMap.get("repay_id");
			String paypassword = paramsMap.get("paypassword");
			return this.repayMobileService.repayAdvanceSubmit(loan_id, login_token, paypassword, IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
}