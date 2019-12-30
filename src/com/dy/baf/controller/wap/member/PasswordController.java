package com.dy.baf.controller.wap.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.dy.baf.entity.common.MbApproveEmail;
import com.dy.baf.entity.common.MbApprovePhone;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.PasswrodService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.Condition;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.utils.Constant;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;
/**
 * 密码管理
 */
@Controller(value="wapPasswordController")
public class PasswordController extends WapBaseController  {
	@Autowired
	private PasswrodService passwordService;
	/**
	 * 找回密码验证标示
	 */
	public static final String VERIFY_STATUS = "verify_status" ;
	/**
	 * 申请找回密码的手机号或者邮箱
	 */
	public static final String SEARCH_ACCOUNT = "search_account" ;
	/**
	 * 找回密码类型
	 */
	public static final String SEARCH_TYPE = "search_type";
	/**
	 * 修改登录密码
	 * @return
	 */
	@RequestMapping(value="/member/editpwd",method=RequestMethod.GET)
	public ModelAndView editpwd(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/editPwd.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 修改登录密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/editpwd",method=RequestMethod.POST)
	public DyPhoneResponse editpwd(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String password = request.getParameter("raw");
			String new_password = request.getParameter("newPwd");
			String confirm_password = request.getParameter("confirmPwd");

			return passwordService.editPwd(password, new_password, confirm_password, member.getId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 设置支付密码
	 * @return
	 */
	@RequestMapping(value="/member/setPaypwd",method=RequestMethod.GET)
	public ModelAndView setPaypwd(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/set_paypwd.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 修改支付密码
	 * @return
	 */
	@RequestMapping(value="/member/editpaypwd",method=RequestMethod.GET)
	public ModelAndView editPayPwd(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/edit_paypwd.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 修改支付密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/editpaypwd",method=RequestMethod.POST)
	public DyPhoneResponse editpaypwd(HttpServletRequest request){
		try {
			String password = request.getParameter("raw");
			String confirm_paypassword = request.getParameter("confirmPwd");
			String new_paypassword = request.getParameter("newPwd");
			String memberId = this.getMemberId().toString();
			return passwordService.editPaypwd(password, new_paypassword, confirm_paypassword, memberId);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 忘记密码
	 * @param request
	 * @return
	 * @throws DyServiceException 
	 */
	@RequestMapping(value="/system/searchPwd",method=RequestMethod.GET)
	public ModelAndView searchPwd() throws DyServiceException {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/searchPwd.jsp");
		/*	String account=String.valueOf(this.getSession().getAttribute("account"));
			if(StringUtils.isNotBlank(account)){
				system.setContentPage("system/searchPwd.jsp");
			}else{
				system.setContentPage("system/reglogin.jsp");
			}*/
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 找回密码
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/searchPwd",method=RequestMethod.POST)
	public DyPhoneResponse searchPwd(HttpServletRequest request) throws Exception {
		String type = RequestUtil.getString(request, "type", "") ;
		String account=request.getParameter("account");
		String account2 = "" ;
		//校验用户名
		QueryItem item = new QueryItem() ;
		List<NameValue> conditionList = new ArrayList<NameValue>() ;
		conditionList.add(new NameValue("name", account, Condition.EQ)) ;
		conditionList.add(new NameValue("email", account, Condition.EQ, true)) ;
		conditionList.add(new NameValue("phone", account, Condition.EQ, true)) ;		
		item.getWhere().add(new Where(conditionList));
		MbMember member = this.getOneByEntity(item, Module.MEMBER, Function.MB_MEMBER,MbMember.class);
		if(member==null){
			if("phone".equals(type)){
				return errorJsonResonse("手机号不存在！") ;
			}
			if("email".equals(type)){
				return errorJsonResonse("邮箱不存在！") ;
			}			
		}
		
		if("phone".equals(type)){
			String phone = RequestUtil.getString(request, "account", null) ;
			if(member.getPhone() == null){
				return errorJsonResonse("该手机未绑定账号！") ;
			}
			if(!phone.equals(String.valueOf(member.getPhone()))){
				return errorJsonResonse("输入的手机号与该用户绑定的手机号不一致！") ;
			}
			account2 = phone ;
			QueryItem phoneQuery = new QueryItem();
			phoneQuery.getWhere().add(Where.eq("phone", phone));
			phoneQuery.getWhere().add(Where.eq("status", 1));
			MbApprovePhone approvePhone = this.getOneByEntity(phoneQuery,Module.MEMBER, Function.MB_PHONE, MbApprovePhone.class);
			if (approvePhone == null) {
				return errorJsonResonse("该手机未注册！") ;
			}
		}else{
			// 判断用户名是否作为邮箱号已经存在
			String email = RequestUtil.getString(request, "account", null) ;
			account = email ; 
			QueryItem emailQuery = new QueryItem();
			emailQuery.getWhere().add(Where.eq("email", email));
			emailQuery.getWhere().add(Where.eq("status", 1));
			emailQuery.getWhere().add(Where.eq("member_name", member.getName()));
			MbApproveEmail approveEmail = this.getOneByEntity(emailQuery, Module.MEMBER, Function.MB_EMAIL, MbApproveEmail.class);
			if (approveEmail == null) {
				return errorJsonResonse("该邮箱未注册或非用户注册邮箱！") ;
			}
		}
		this.removeSessionAttribute(VERIFY_STATUS) ;
		this.setSessionAtrribute(SEARCH_ACCOUNT, account) ;
		return successJsonResonse("验证成功");
	}
	
	/**
	 * 邮箱找回密码
	 * @return
	 */
	@RequestMapping(value="/system/searchPwdemail",method=RequestMethod.GET)
	public ModelAndView searchPwdemail(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			
			String account=String.valueOf(this.getSession().getAttribute(this.SEARCH_ACCOUNT));
			if(StringUtils.isNotBlank(account)){
				system.setContentPage("system/searchPwdemail.jsp");
			}else{
				system.setContentPage("system/reglogin.jsp");
			}
			
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 邮箱找回密码
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/searchPwdemail",method=RequestMethod.POST)
	public DyPhoneResponse searchPwdemail(HttpServletRequest request) throws Exception {
		Map<String, Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap") ;
		String sessionCode = (String) sessionMap.get("code");
		String sessionEmail = (String) sessionMap.get("email") ;
		String email = (String) this.getSessionAttribute(SEARCH_ACCOUNT) ;
		String code = request.getParameter("emailCode") ;
		if (!code.equals(sessionCode)) {
			return errorJsonResonse("验证码错误");
		}
		if(!email.equals(sessionEmail)){
			return errorJsonResonse("邮箱错误");
		}
		this.removeSessionAttribute("sessionMap") ;
		this.setSessionAtrribute(VERIFY_STATUS, "1") ;
		this.setSessionAtrribute(SEARCH_TYPE, "2") ;
		return successJsonResonse("验证成功") ;
	}
	
	/**
	 * 手机找回密码
	 * @return
	 */
	@RequestMapping(value="/system/searchPwdphone",method=RequestMethod.GET)
	public ModelAndView searchPwdphone(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			String account=String.valueOf(this.getSession().getAttribute(this.SEARCH_ACCOUNT));
			if(StringUtils.isNotBlank(account)){
				system.setContentPage("system/searchPwdphone.jsp");
			}else{
				system.setContentPage("system/reglogin.jsp");
			}
			
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 手机找回密码
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/searchPwdphone",method=RequestMethod.POST)
	public DyPhoneResponse searchPwdphone(HttpServletRequest request) throws Exception {
		String phoneCode = request.getParameter("phoneCode") ;
		String phone = (String) this.getSessionAttribute(SEARCH_ACCOUNT) ;
		Map<String,Object> map = (Map<String, Object>) this.getSessionAttribute("codeMap");
		if(map == null){
			return errorJsonResonse("请先获取验证码");
		}
		Long sessionPhone = (Long) map.get("phone");
		String sessionCode = (String) map.get("sys_code");
		if(!Long.valueOf(phone).equals(sessionPhone))return errorJsonResonse("手机号码不匹配! ");
		if(!phoneCode.equals(sessionCode))return errorJsonResonse("验证码错误!");
		this.removeSessionAttribute("codeMap");
		this.setSessionAtrribute(VERIFY_STATUS, "1") ;
		this.setSessionAtrribute(SEARCH_TYPE, "1") ;
		return successJsonResonse("验证成功");
	}
	
	/**
	 * 找回密码重置密码
	 * @return
	 */
	@RequestMapping(value="/system/searchPwdreset",method=RequestMethod.GET)
	public ModelAndView searchPwdreset(){
		ModelAndView view = new ModelAndView();
		try {
			
			SystemInfo system = new SystemInfo();
			
			String verify_status = String.valueOf(this.getSessionAttribute(VERIFY_STATUS));
			
			if(StringUtils.isNotBlank(verify_status)){
				system.setContentPage("system/searchPwdreset.jsp");
			}else{
				system.setContentPage("system/reglogin.jsp");
			}
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 找回密码重置密码
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/system/searchPwdreset",method=RequestMethod.POST)
	public DyPhoneResponse searchPwdreset(HttpServletRequest request) throws Exception {
		String status = (String) this.getSessionAttribute(VERIFY_STATUS);
		if(StringUtils.isBlank(status)){
			return errorJsonResonse("重置密码失败");
		}
		String type = (String) this.getSessionAttribute(SEARCH_TYPE);
		String username = (String) this.getSessionAttribute(SEARCH_ACCOUNT);
		String confirm_password = request.getParameter("against_password");
		String new_password = request.getParameter("password");
		Pattern pattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
		Matcher matcher = pattern.matcher(confirm_password);
		if (!matcher.matches()) {
			DyPhoneResponse response = new DyPhoneResponse();
			response.setCode(DyPhoneResponse.NO);
			response.setDescription("密码只能为数字和字母组合的密码");
			response.setResult(DyPhoneResponse.ERROR);
			return response;
		}
		DyPhoneResponse response = passwordService.editRecoverPwd(username, new_password, confirm_password, type);
		if(DyPhoneResponse.OK == response.getCode()){
			this.removeSessionAttribute(VERIFY_STATUS) ;
			this.removeSessionAttribute(SEARCH_ACCOUNT) ;
			this.removeSessionAttribute(SEARCH_TYPE) ;
		}
		return response;
	}
}
