package com.dy.baf.controller.phone.finance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.finance.BountyService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.entity.Page;
import com.dy.core.utils.StringUtils;

/**
 * 我的红包
 * @author Administrator
 *
 */
@Controller(value="appBountyController")
public class BountyController extends AppBaseController {

	@Autowired
	private BountyService bountService;
	
	/**
	 * 我的红包获取数据
	 */
	@ResponseBody
	@RequestMapping("/bounty/list")
	public DyPhoneResponse bountyList(String xmdy, String diyou) throws Exception {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String pageNumStr = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			String login_token = paramsMap.get("login_token");
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("用户登录标识不能为空");
			}
			Page pageObj =  bountService.bountyList(pageNumStr,login_token,epage);
			return successJsonResonse(pageObj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 获取当前登录用户的可用红包列表
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping("/bounty/useableList")
	public DyPhoneResponse bountyUseableList(String xmdy, String diyou) throws Exception {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String page = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			String amount = paramsMap.get("amount");
			String login_token = paramsMap.get("login_token");
			String loanId = paramsMap.get("loan_id");
			
			if (StringUtils.isBlank(login_token)) {
				return errorJsonResonse("用户登录标识不能为空");
			}
			
			if (StringUtils.isBlank(amount)) {
				return errorJsonResonse("金额不能为空");
			}
			
			Page pageObj = bountService.bountyUseableList(page, epage, amount, login_token, loanId);
			return successJsonResonse(pageObj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}

}
