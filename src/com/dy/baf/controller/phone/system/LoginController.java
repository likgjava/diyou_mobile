package com.dy.baf.controller.phone.system;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.system.LoginService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.Constant;
import com.dy.core.utils.IpUtil;

/**
 * 
 * @Description: 用户登录
 * @author 波哥
 * @date 2015年9月3日 下午10:52:06 
 * @version V1.0
 */
@Controller
public class LoginController extends AppBaseController {

	@Autowired
	private LoginService loginService;
	
	/**
	 * 用户登陆
	 * @param memberName
	 * @param password
	 * @param type
	 * @param phone_type
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/member/login")
	public DyPhoneResponse login(String xmdy,String diyou) throws Exception {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String memberName = paramsMap.get("member_name");
			String password = paramsMap.get("password");
			String type = paramsMap.get("type");
			String phoneType = paramsMap.get("phone_type");
			String clientId = paramsMap.get("clientId");
			String code = paramsMap.get("code") == null ? password : paramsMap.get("code");
			return this.loginService.login(memberName, password, code, type, phoneType, clientId, this.getSession(), IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 退出登录
	 * @param login_token
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/loginOut")
	public DyPhoneResponse loginOut(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			
			this.removeSessionAttribute(Constant.SESSION_USER);
			this.removeSessionAttribute("member_info");
			return successJsonResonse("退出成功");
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
}
