package com.dy.baf.controller.phone.trust;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.trust.TrustRegisterService;
import com.dy.baf.utils.AppSecurityUtil;

/**
 * 
 * 
 * @Description: 托管注册
 * @author 波哥
 * @date 2015年10月10日 下午3:56:34
 * @version V1.0
 */
@Controller(value = "appTrustRegisterController")
public class TrustRegisterController extends AppBaseController {
	@Autowired
	private TrustRegisterService trustRegisterService;


	@ResponseBody
	@RequestMapping("/trust/isregister")
	public DyPhoneResponse isRegister(String xmdy, String diyou) {

		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");

			return trustRegisterService.isRegister(login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}

	}
}