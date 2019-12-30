package com.dy.baf.controller.phone.member;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.member.AttestationsService;
import com.dy.baf.utils.AppSecurityUtil;
@Controller(value="appAttestationsController")
public class AttestationsController extends AppBaseController {
	
	@Autowired
	private AttestationsService attestationsService;

	@ResponseBody
	@RequestMapping("/loan/attestations")
	public DyPhoneResponse realnameApprove(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.attestationsService.attestations(login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	
	@ResponseBody
	@RequestMapping("/loan/authenticationUpload")
	public DyPhoneResponse authenticationUpload(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String images = paramsMap.get("images");
			String id = paramsMap.get("login_token");
			return this.attestationsService.authenticationUpload(images,login_token,id);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
