package com.dy.baf.controller.phone.finance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.finance.AccountService;
import com.dy.baf.utils.AppSecurityUtil;

/**
 * 
 * @Description: 用户资金
 * @author 波哥
 * @date 2015年9月14日 下午2:56:52 
 * @version V1.0
 */
@Controller(value="appAccountController")
public class AccountController extends AppBaseController {

	@Autowired
	private AccountService accountService;
	/**
	 * 用户资金详情
	 * @param login_token
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/account/memberAccount")
	public DyPhoneResponse memberAccount(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.accountService.memberAccount(login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
