package com.dy.baf.controller.phone.finance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.finance.AccountLogService;
import com.dy.baf.utils.AppSecurityUtil;
/**
 * 
 * @Description: 交易记录
 * @author 波哥
 * @date 2015年9月14日 下午3:03:04 
 * @version V1.0
 */
@Controller(value="appAccountLogController")
public class AccountLogController extends AppBaseController {
	
	@Autowired
	private AccountLogService accountLogService;
	
	/**
	 * 资金历史记录列表
	 */
	@ResponseBody
	@RequestMapping("/account/accountLog")
	public DyPhoneResponse accountLog(String xmdy,String diyou) {
		
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String page = paramsMap.get("page");
			String start = paramsMap.get("start_time");
			String end = paramsMap.get("end_time");
			String fee_id = paramsMap.get("fee_id");
			String login_token = paramsMap.get("login_token");

			return this.accountLogService.accountLog(Long.valueOf(login_token),Integer.valueOf(page));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 获取交易分类
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/feetype/getfeelist")
	public DyPhoneResponse getfeelist() {
		try {
			return this.accountLogService.getfeelist();
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
