package com.dy.baf.controller.phone.member;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.EmailVerifyCode;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.EmailService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.StringUtils;

/**
 * 邮箱认证
 *
 */
@Controller(value="appEmailController")
public class EmailController extends AppBaseController {

	@Autowired
	private EmailService emailService;
	/**
	 * 提交邮箱认证
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/approve/approveEmail")
	public DyPhoneResponse approveEmail(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("email"); 
			String code = paramsMap.get("code"); 
			String login_token = paramsMap.get("login_token"); 
			
			Map<String,Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap");
			return this.emailService.approveEmail(email, code, Long.valueOf(login_token), sessionMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 修改绑定的邮箱
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/approve/resetEmail")
	public DyPhoneResponse resetEmail(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("new_email"); 
			String code = paramsMap.get("new_code"); 
			String login_token = paramsMap.get("login_token"); 
			
			Map<String,Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap");
			return this.emailService.approveEmail(email, code, Long.valueOf(login_token), sessionMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 找回密码，发送邮件
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/sendRecoveryEmail")
	public DyPhoneResponse sendRecoveryEmail(String xmdy, String diyou) {

		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("email");
			String verifyCode = GetUtils.sixCode();
			EmailVerifyCode emailVerifyCode = EmailVerifyCode.getCode(email, verifyCode, 60);
			if (emailVerifyCode.getErrorMsg() != null)
				return errorJsonResonse(emailVerifyCode.getErrorMsg());

			MbMember user = this.emailService.getUser(email);
			return this.emailService.sendemail(email, verifyCode, String.valueOf(user.getId()), "recover");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 发送邮件
	 * @param email
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approve/sendApproveEmail")
	public DyPhoneResponse sendemail(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("email"); 
			String emailCode = paramsMap.get("email_code"); 
			String login_token = paramsMap.get("login_token"); 
			String type = StringUtils.isNotBlank(paramsMap.get("type"))?paramsMap.get("type"):"reset"; 
			//发送激活邮箱,将验证码和email存入session
			if(StringUtils.isBlank(emailCode)){
				emailCode = GetUtils.sixCode();
			}
			if("4".equals(type)){
				type = "reset";
			}else if("1".equals(type)){
				type = "approve";
			}else if("5".equals(type)){
				type = "again";
			}
			
			return this.emailService.sendemail(email, emailCode, login_token, type);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 验证验证码
	 * @param email
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/approve/verifyemail")
	public DyPhoneResponse verifyEmail(String xmdy,String diyou) {
		DyPhoneResponse response = new DyPhoneResponse();
		
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("email"); 
			String code = paramsMap.get("code"); 
			String login_token = paramsMap.get("login_token"); 
			
			Map<String, Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap");
			if (sessionMap == null) {//session已过期
				response.setDescription("验证码错误");
				return response;
			} else {
				//session未过期
				String sessionCode = (String) sessionMap.get("sessionCode");
				String sessionEmail = (String) sessionMap.get("sessionEmail");
				if (!email.equals(sessionEmail)) {
					response.setDescription("邮箱不正确");
					return response;
				}
				if (!sessionCode.equals(code)) {
					response.setDescription("验证码错误");
					return response;
				}
			}
			return this.emailService.verifyEmail(email, code, login_token);
			
		} catch (Exception e) {
			
			logger.error(e.getStackTrace());
		}
		
		response.setDescription("验证成功!");
		return response;
	}
	
	/**
	 * 获取用户认证的邮箱
	 * @param login_token
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/getEmail")
	public DyPhoneResponse getEmail(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			return this.emailService.getAppEmail(login_token);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 邮箱号码检查
	 * @param email
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/public/checkEmail")
	public DyPhoneResponse checkEmail(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String email = paramsMap.get("email"); 
			return this.emailService.checkEmail(email);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return errorJsonResonse(e.getMessage());
		}
	}
}
