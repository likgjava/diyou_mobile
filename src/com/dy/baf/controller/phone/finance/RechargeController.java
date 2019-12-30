package com.dy.baf.controller.phone.finance;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.finance.RechargeService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;

/**
 * 
 * 
 * @Description: 充值
 * @author 波哥
 * @date 2015年9月10日 下午9:31:23
 * @version V1.0
 */
@Controller(value = "appRechargeController")
public class RechargeController extends AppBaseController {
	@Autowired
	private RechargeService rechargeService;

	/**
	 * 充值方式
	 */
	@ResponseBody
	@RequestMapping("/recharge/getList")
	public DyPhoneResponse getlist(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String page = paramsMap.get("page");
			String login_token = paramsMap.get("login_token");
			return this.rechargeService.getlist(page, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}

	}

	/**
	 * 充值手续费计算
	 */
	@ResponseBody
	@RequestMapping("/recharge/rechargeFee")
	public DyPhoneResponse rechargefee(String xmdy, String diyou) {

		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			BigDecimal amount = new BigDecimal(paramsMap.get("amount"));
			String payment_nid = paramsMap.get("payment_nid");
			String login_token = paramsMap.get("login_token");
			return this.rechargeService.rechargefee(amount, payment_nid, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}
	}

	/**
	 * 充值
	 */
	@ResponseBody
	@RequestMapping("/recharge/recharge")
	public DyPhoneResponse recharge(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			BigDecimal amount = new BigDecimal(paramsMap.get("amount"));
			String query_type = paramsMap.get("query_type");
			String type = paramsMap.get("type");
			String payment = paramsMap.get("payment");
			String valicode = paramsMap.get("valicode");
			String login_token = paramsMap.get("login_token");

			// 验证码校验
			if ("".equals(valicode))
				return errorJsonResonse("验证码不能为空!");
			String sessionVerifyCode = (String) this.getSessionAttribute(Constant.SESSION_VERIFY_CODE);
			if (!valicode.equalsIgnoreCase(sessionVerifyCode))
				return errorJsonResonse("验证码错误!");
			return this.rechargeService.recharge(query_type, type, payment, amount, valicode, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}

	}

	/**
	 * 充值记录列表
	 */
	@ResponseBody
	@RequestMapping("/recharge/rechargelog")
	public DyPhoneResponse rechargelog(String xmdy, String diyou) {
		try {

			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			Integer page = Integer.valueOf(paramsMap.get("page"));
			Integer status = null;
			String login_token = paramsMap.get("login_token");
			Page pageObject =  this.rechargeService.rechargelog(page, status, login_token);

			return successJsonResonse(dataConvert(pageObject,"status_name:getRechargeStatus,type_name:getRechargeType"));

		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}

	}

	/**
	 * 充值资金总额
	 */
	@ResponseBody
	@RequestMapping("/recharge/rechargecount")
	public DyPhoneResponse rechargecount(String login_token) {
		try {
			return this.rechargeService.rechargecount(login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}
	}
}
