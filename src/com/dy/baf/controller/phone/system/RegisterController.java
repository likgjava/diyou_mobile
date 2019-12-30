package com.dy.baf.controller.phone.system;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.system.RegisterService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.Constant;

/**
 * 
 * @Description: 用户注册
 * @author 波哥
 * @date 2015年9月3日 下午5:58:52 
 * @version V1.0
 */
@Controller(value="appRegisterController")
public class RegisterController extends AppBaseController {
	@Autowired
	private RegisterService registerService;
	
	/**
	 * 注册第一步填写信息
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/public/reg")
	public DyPhoneResponse fillinfo(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String phone = paramsMap.get("phone");
			String password = paramsMap.get("password");
			String referrer = paramsMap.get("referrer");
			String invite_username = paramsMap.get("invite_username");
			
			DyPhoneResponse response = this.registerService.fillinfo(phone, password, referrer, invite_username, this.getRemoteIp());
			if(response.getCode() == DyPhoneResponse.OK){
				// 把用户信息加入session
				this.setSessionAtrribute(Constant.SESSION_USER, response.getData());
				return successJsonResonse("注册成功");
			}else{
				return response;
			}
			
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 注册时用户账号校验
	 * @param username
	 * @return
	 */
	@ResponseBody
	@RequestMapping("member/checkExit")
	public DyPhoneResponse checkExit(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			return this.registerService.checkExit(paramsMap);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return errorJsonResonse(e.getMessage());
		}
	}
}
