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
import com.dy.baf.service.member.PhoneService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 手机相关类
 * @author 波哥
 * @date 2015年9月9日 上午9:05:37 
 * @version V1.0
 */
@Controller(value="appPhoneController")
public class PhoneController extends AppBaseController {

	@Autowired
	private PhoneService phoneService;
	@Autowired
	private BaseService baseService;
	
	
	/**
	 * 发送短信
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/public/sendSms")
	public DyPhoneResponse sendSms(String xmdy,String diyou){
		try {		
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String type = paramsMap.get("type"); 
			Long phone = StringUtils.isBlank(paramsMap.get("phone")) ? null : Long.valueOf(paramsMap.get("phone"));		
			if(type.equals("approve")){
				QueryItem phoneQueryItem = new QueryItem();
				phoneQueryItem.getWhere().add(Where.eq("phone", phone));		
				Map<String,Object> muser = this.getOneByMap(phoneQueryItem, Module.MEMBER,Function.MB_MEMBER);
				if(muser!=null){
					return errorJsonResonse("该手机号已存在");
				}
			}
			String login_token = paramsMap.get("login_token"); 
			String is_check = paramsMap.get("is_check"); 
			String msg_code = paramsMap.get("phone_code"); 
			this.setSessionAtrribute("sys_code", msg_code);
			this.setSessionAtrribute(Constant.SESSION_PHONE_CODE,msg_code);
			Map<String,Object> data = new HashMap<String,Object>() ;
			data.put("phone", phone) ;
			data.put("sys_code", msg_code);
			this.setSessionAtrribute("codeMap", data);
			return this.phoneService.sendSms(type, phone, login_token, is_check, msg_code);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 解绑
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/approve/resetPhone")
	public DyPhoneResponse resetPhone(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String type = paramsMap.get("type"); 
			String login_token = paramsMap.get("login_token"); 
			String new_phone = paramsMap.get("new_phone"); 
			return this.phoneService.resetPhone(login_token, new_phone);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 绑定手机
	 */
	@ResponseBody
	@RequestMapping("/approve/approvePhone")
	public DyPhoneResponse approvephone(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String phone = paramsMap.get("phone"); 
			String phone_code = paramsMap.get("phone_code"); 
			String login_token = paramsMap.get("login_token"); 
			
			
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("用户标识不能为空!");
			}
			String sessionCode = (String) this.getSessionAttribute("sys_code");
			if(!phone_code.equals(sessionCode))return errorJsonResonse("验证码错误!");
			MbMember  user = this.getMember(Long.valueOf(login_token));
			
			DyPhoneResponse response = this.phoneService.approvephone(Long.valueOf(phone), user);
			if(response.getCode() == DyPhoneResponse.OK){
				//重新设置session
				this.removeSessionAttribute(Constant.SESSION_USER);
				this.setSessionAtrribute(Constant.SESSION_USER, user);
			}
			return response;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 验证短信验证码
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/public/verifyCode")
	public DyPhoneResponse verifyCode(String xmdy,String diyou) {
		
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String phone = paramsMap.get("phone"); 
			String phone_code = paramsMap.get("phone_code"); 
			
			Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("map");
			Long sessionPhone = (Long) map.get("phone");
			String sessionCode = (String) map.get("sys_code");
			
			if(!phone.equals(sessionPhone))return errorJsonResonse("手机号码不匹配! ");
			if(!phone_code.equals(sessionCode))return errorJsonResonse("验证码错误!");
			
			return successJsonResonse("验证成功");
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 获取用户认证的手机
	 * @param login_token
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/getPhone")
	public DyPhoneResponse getPhone(String xmdy,String diyou){
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			
			MbMember member = this.phoneService.getPhone(login_token);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("phone", member.getPhone());
			return successJsonResonse(map);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 手机号（格式/是否存在）检测
	 * @param phone
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/public/checkPhone")
	public DyPhoneResponse checkPhone(String xmdy,String diyou){
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String phone = paramsMap.get("phone"); 
			
			return this.phoneService.checkPhone(phone);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
