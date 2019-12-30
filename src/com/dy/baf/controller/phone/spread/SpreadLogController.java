package com.dy.baf.controller.phone.spread;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.spread.SpreadLogService;
import com.dy.baf.utils.AppSecurityUtil;

@Controller(value="appSpreadLogController")
public class SpreadLogController extends AppBaseController {

	@Autowired
	private SpreadLogService spreadLogService;
	/**
	 * 推广记录列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/spread/mySpreadLog")
	public DyPhoneResponse mySpreadLog(String xmdy,String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String page = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			String name = paramsMap.get("name");
			return spreadLogService.getSpreadLog(login_token, name, page, epage);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
