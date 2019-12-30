package com.dy.baf.controller.phone.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.PasswrodService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 密码模块
 * @author 波哥
 * @date 2015年9月10日 上午10:20:27
 * @version V1.0
 */
@Controller(value = "appPasswordController")
public class PasswordController extends AppBaseController {

	@Autowired
	private PasswrodService passwordService;

	/**
	 * 修改登录密码
	 * 
	 * @param raw
	 * @param newPwd
	 * @param confirmPwd
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/member/editPwd")
	public DyPhoneResponse editPwd(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String password = paramsMap.get("password");
			String new_password = paramsMap.get("new_password");
			String confirm_password = paramsMap.get("confirm_password");
			String login_token = paramsMap.get("login_token");

			return passwordService.editPwd(password, new_password, confirm_password, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 重置密码（找回密码）
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/editRecoverPwd")
	public DyPhoneResponse editRecoverPwd(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String type = paramsMap.get("type");
			String username = paramsMap.get("username");
			String confirm_password = paramsMap.get("confirm_password");
			String new_password = paramsMap.get("new_password");

			return passwordService.editRecoverPwd(username, new_password, confirm_password, type);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 修改支付密码
	 * 
	 * @param raw
	 * @param newPwd
	 * @param confirmPwd
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/editPaypwd")
	public DyPhoneResponse editPaypwd(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String password = paramsMap.get("password");
			String confirm_paypassword = paramsMap.get("confirm_paypassword");
			String new_paypassword = paramsMap.get("new_paypassword");
			String login_token = paramsMap.get("login_token");
			return passwordService.editPaypwd(password, new_paypassword, confirm_paypassword, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 用户是否设置支付密码
	 * 
	 * @param login_token
	 * @return
	 */
	@RequestMapping("/member/getPaypwd")
	public DyPhoneResponse getPaypwd(String xmdy, String diyou) {
		try {
			
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			
			if (StringUtils.isBlank(login_token)) {
				return errorJsonResonse("登录标识不能为空");
			}
			MbMember user = this.getMember(Long.valueOf(login_token));
			Map<String, String> statusMap = new HashMap<String, String>();
			if (StringUtils.isBlank(user.getPaypassword())) {
				statusMap.put("status", "0");
			} else {
				statusMap.put("status", "1");
			}
			return successJsonResonse(statusMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}

	}
}
