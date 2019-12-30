package com.dy.baf.controller.wap.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.PhoneService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.utils.Constant;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 手机相关类
 * @author 波哥
 * @date 2015年9月9日 上午9:05:37 
 * @version V1.0
 */
@Controller(value="wapPhoneController")
public class PhoneController extends WapBaseController {

	@Autowired
	private PhoneService phoneService;
	
	
	
	/**
	 * 解绑
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/phone/resetPhone")
	public DyPhoneResponse resetPhone(String type,String phone_code,String login_token) {
		try {
			String sessionCode =  (String) this.getSessionAttribute("sys_code");
			if(!phone_code.equals(sessionCode)){
				return errorJsonResonse("验证码错误!");
			}
			return this.phoneService.resetPhone(type, login_token);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 发送手机短信
	 */
	@ResponseBody
	@RequestMapping("/system/sendsms")
	public DyPhoneResponse sendSms(HttpServletRequest request) throws Exception {
		//短信接口
		String type = RequestUtil.getString(request, "type", "reg") ;
		String phoneStr = request.getParameter("phone") ;
		String memberId = "" ;	
		if("reg".equals(type)){
			if(StringUtils.isBlank(phoneStr)){
				return errorJsonResonse("请输入手机号码!");
			}else{
				if(StringUtils.isNotBlank(phoneStr)&&!StringUtils.checkPhone(phoneStr)){
					return errorJsonResonse("请输入正确手机号码!");
				}
			}		
			//phoneStr = (String) this.getSessionAttribute("phone") ;
		}else if("pwd".equals(type)){
			phoneStr = (String) this.getSessionAttribute(PasswordController.SEARCH_ACCOUNT) ;
			if(StringUtils.isBlank(phoneStr)){
				return errorJsonResonse("请输入手机号码!");
			}
			QueryItem queryItem = new QueryItem() ;
			queryItem.setWhere(Where.eq("phone", phoneStr)) ;
			MbMember member = this.getOneByEntity(queryItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class) ;
			memberId = member.getId().toString() ;
		}else if("login".equals(type)){
			//phoneStr = (String) this.getSessionAttribute("account") ;
			QueryItem queryItem = new QueryItem() ;
			List<Where> where = new ArrayList<Where>();
			List<NameValue> ands = new ArrayList<NameValue>();
			ands.add(new NameValue("name",phoneStr,"=",true));
			ands.add(new NameValue("phone",phoneStr,"=",true));
			ands.add(new NameValue("email",phoneStr,"=",true));
			where.add(new Where(ands));
			queryItem.setWhere(where);//设置where条件
			MbMember member = this.getOneByEntity(queryItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class) ;
			memberId = member.getId().toString() ;
			phoneStr = String.valueOf(member.getPhone());
			/**
			 * 将session中的account替换为手机号
			 */
			this.setSessionAtrribute("account",StringUtils.isBlank(phoneStr)?member.getName():phoneStr);
		}else if("reset".equals(type)){
			phoneStr = this.getMember(this.getMemberId()).getPhone().toString() ;
		}else if("approve".equals(type)){
			if(StringUtils.isBlank(phoneStr))
			phoneStr =  (String) this.getSessionAttribute("resetphone") ;
			if(StringUtils.isBlank(phoneStr)){
				return errorJsonResonse("请输入您要绑定的手机号码!");
			}
		}else if("newphone".equals(type)){
			DyPhoneResponse checkResponse = this.phoneService.checkPhone(phoneStr);
			if(checkResponse.getCode() == DyPhoneResponse.OK){
				if(StringUtils.isBlank(phoneStr)){
					return errorJsonResonse("请输入您要绑定的手机号码!");
				}
			}else{
				return checkResponse;
			}
		}
		if(null != this.getSessionAttribute(Constant.SESSION_USER))
			memberId = this.getMemberId().toString() ;
		if(StringUtils.isNotBlank(phoneStr)&&!StringUtils.checkPhone(phoneStr)){
			return errorJsonResonse("请输入正确手机号码!");
		}
		Long phoneNum = StringUtils.checkPhone(phoneStr) ? Long.parseLong(phoneStr) : null ;
		
		
		//对手机唯一性校验
		if("approve".equals(type) || "reg".equals(type)){//注册、新手机验证
			QueryItem userQueryItem = new QueryItem();
			userQueryItem.setFields(" count(1) numcount ");
			userQueryItem.getWhere().add(Where.eq("phone", phoneNum));		
			Map<String,Object> muser = this.getOneByMap(userQueryItem, Module.MEMBER,Function.MB_MEMBER);
			if(muser!=null){
				Integer numCount = Integer.parseInt(String.valueOf(muser.get("numcount")));
				if(numCount!=null&&numCount>0){
					return errorJsonResonse("该手机号已被使用,请更换其他手机号");
				}		
			}
		}
		
		String code = GetUtils.sixCode();
		Map<String,Object> data = new HashMap<String,Object>() ;
		data.put("phone", phoneNum) ;
		data.put("sys_code", code);
		this.setSessionAtrribute("codeMap", data);
		this.setSessionAtrribute(Constant.SESSION_PHONE_CODE,code);
		return phoneService.sendSms(type,phoneNum,memberId,null,code);
	}
	
	/**
	 * 绑定手机
	 */
	@ResponseBody
	@RequestMapping("/approve/approvePhone")
	public DyPhoneResponse approvephone(Long phone,String phone_code) {
		try {
			
			MbMember  user = this.getMember(this.getMemberId());
			
			DyPhoneResponse response = this.phoneService.approvephone(phone, user);
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
	public DyPhoneResponse verifyCode(Long phone,String type,String phone_code) {
		
		try {
			Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
			Long sessionPhone = (Long) map.get("phone");
			String sessionCode = (String) map.get("sys_code");
			if(!phone.equals(sessionPhone))return errorJsonResonse("手机号码不匹配! ");
			if(!phone_code.equals(sessionCode))return errorJsonResonse("验证码错误!");
			this.removeSessionAttribute("codeMap");
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
	public DyPhoneResponse getPhone(String login_token){
		try {
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
	@RequestMapping(value="/public/checkPhone",method=RequestMethod.POST)
	public DyPhoneResponse checkPhone(String phone){
		try {
			this.getSession().setAttribute("resetphone", phone) ;
			return this.phoneService.checkPhone(phone);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 手机认证
	 */
	@RequestMapping(value="/member/checkPhone",method=RequestMethod.GET)
	public ModelAndView checkPhone(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/approvePhone.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 手机认证第二部
	 */
	@RequestMapping(value="/member/approvePhone",method=RequestMethod.GET)
	public ModelAndView approvePhone(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/approvePhone2.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 手机认证第二步
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	@ResponseBody
	@RequestMapping(value="/member/approvephone",method=RequestMethod.POST)
	public DyPhoneResponse approvePhone(HttpServletRequest request) throws Exception{
		String code = request.getParameter("phone_code");
		String phone = request.getParameter("phone");
		if(StringUtils.isBlank(phone)){
			phone=(String)this.getSession().getAttribute("resetphone");//解绑后，手机认证
		}
		Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
		Long sessionPhone = map == null ? null : (Long) map.get("phone");
		String sessionCode = map == null ? "" : (String) map.get("sys_code");
		if(!code.equals(sessionCode))return errorJsonResonse("验证码错误!");
		if(!phone.equals(String.valueOf(sessionPhone))){
			return errorJsonResonse("手机号码不匹配! ");
		}

		return this.phoneService.approvephone(Long.valueOf(phone), this.getMember(this.getMemberId()));
	}
	
	/**
	 * 修改手机
	 */
	@RequestMapping(value="/member/updatephoneone",method=RequestMethod.GET)
	public ModelAndView updatePhoneOne(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/updatePhoneone.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 修改手机第一步
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/updatePhoneone",method=RequestMethod.POST)
	public DyPhoneResponse updatePhoneOne(HttpServletRequest request) throws Exception{
		MbMember member = this.phoneService.getPhone(this.getMemberId().toString());
		Map<String,Object> response = new HashMap<String, Object>() ;
		String phone=StringUtils.subString(member.getPhone().toString(), 3)+"****"+member.getPhone().toString().substring(member.getPhone().toString().length()-4, member.getPhone().toString().length());
		response.put("phone", phone);
		return this.successJsonResonse(response) ;
	}
	
	/**
	 * 修改手机第二步
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/updatephonetwo",method=RequestMethod.POST)
	public DyPhoneResponse updatePhoneTwo(HttpServletRequest request) throws Exception{
		String code = request.getParameter("code");
		MbMember member = this.getMember(this.getMemberId()) ;
		Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
		if (map == null) {
			return errorJsonResonse("请先获取验证码");
		}
		return this.verifyCode(member.getPhone(), "reset", code) ;
	}
	/**
	 * 修改手机第三步
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/updatephonethree",method=RequestMethod.POST)
	public DyPhoneResponse updatePhoneThree(HttpServletRequest request) throws Exception{
		Long new_phone = RequestUtil.getLong(request, "phone", null);
		String code = request.getParameter("phone_code") ;
		Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
		if(map==null){
			return errorJsonResonse("请先获取验证码");
		}
		Long sessionPhone = (Long) map.get("phone");
		String sessionCode = (String) map.get("sys_code");
		if(!new_phone.equals(sessionPhone))return errorJsonResonse("手机号码不匹配! ");
		if(!code.equals(sessionCode))return errorJsonResonse("验证码错误!");
		this.removeSessionAttribute("codeMap");
		return this.phoneService.resetPhone(this.getMemberId().toString(), new_phone.toString());
	}
}
