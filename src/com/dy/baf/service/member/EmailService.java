package com.dy.baf.service.member;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveEmail;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysEmailPort;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Email;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.EmailUtil;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.MemberService;

/**
 * 邮箱认证
 *
 */
@Service("mobileEmailService")
public class EmailService extends MobileService {

	@Autowired
	private BaseService baseService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private EmailUtil emailUtil;
	/**
	 * 提交邮箱认证
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse approveEmail(String email,String code, Long memberId,Map<String, Object> sessionMap) throws DyServiceException {
		if (memberId == null) {
			return errorJsonResonse("登录标识不能为空");
		}
		// 解绑原邮箱进行验证
		if (code != null && !"".equals(code)) {
			String sessionCode = sessionMap == null ? null : (String) sessionMap.get("code");
			if (!code.equals(sessionCode)) {

				return errorJsonResonse("验证码错误");
			}
			MbMember user = (MbMember) this.getMbMember(memberId);
			// 更新member，删除Email认证
			memberService.removeEmail(user);
		}
		// 验证邮箱
		if (StringUtils.isBlank(email)) {
			return errorJsonResonse("邮箱不能为空");
		}
		if (!StringUtils.checkEmail(email)) {
			return errorJsonResonse("邮箱格式错误");
		}
		// 验证邮箱是否已存在
		QueryItem emailQueryItem = new QueryItem(Module.MEMBER, Function.MB_EMAIL);
		List<Where> where = new ArrayList<Where>();
		where.add(new Where("email", email));
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("status", 1, "=", true));
		ands.add(new NameValue("status", -2, "=", true));
		where.add(new Where(ands));
		emailQueryItem.setWhere(where);
		MbApproveEmail approveEmail = (MbApproveEmail) this.baseService.getOne(emailQueryItem, MbApproveEmail.class);
		if (approveEmail != null) {
			return errorJsonResonse("该邮箱已经存在");
		}
		
		MbMember member = this.getMbMember(memberId);
				
		//插入邮箱验证
		MbApproveEmail appEmail = new MbApproveEmail();
		appEmail.setMemberId(member.getId());
		appEmail.setMemberName(member.getName());
		appEmail.setEmail(email);
		appEmail.setAddTime(DateUtil.getCurrentTime());
		appEmail.setStatus(1);
		appEmail.setToken("adad");
		appEmail.setVerifyTime(appEmail.getAddTime());
		this.baseService.insert(Module.MEMBER, Function.MB_EMAIL, appEmail);

		//更新member表
		member.setEmail(email);
		member.setIsEmail(1);
		this.baseService.updateById(Module.MEMBER, Function.MB_MEMBER, member);

		//重新设置session
		Session session = SecurityUtils.getSubject().getSession();
		session.removeAttribute(Constant.SESSION_USER);
		session.setAttribute(Constant.SESSION_USER, member);

		return successJsonResonse("认证成功");
	}
	
	/**
	 * 修改绑定的邮箱
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/approve/resetEmail")
	public DyPhoneResponse resetEmail(HttpServletRequest request){
		DyPhoneResponse response = new DyPhoneResponse();
		return response;
	}
	
	/**
	 * 发送邮件
	 * @param email
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse sendemail(String email, String verifyCode, String memberId,String type) throws Exception {
		if(StringUtils.isBlank(email)){
			return errorJsonResonse("邮箱不能为空");
		}
		if(StringUtils.isBlank(verifyCode)){
			return errorJsonResonse("验证码不能为空");
		}
		if(StringUtils.isBlank(type)){
			return errorJsonResonse("类型不能为空");
		}
		//获取当前用户
		MbMember user = (MbMember)this.getMbMember(Long.valueOf(memberId));
		
		//判断邮箱是否被使用
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("email", email));
		whereList.add(Where.notEq("id", user.getId()));
		String errorMsg = validateExist(email, "邮箱", Module.MEMBER, Function.MB_MEMBER, whereList);
		if(org.apache.commons.lang.StringUtils.isNotEmpty(errorMsg)) {
			return errorJsonResonse(errorMsg);
		}
		
		user = this.getMbMember(user.getId());
		if ("reset".equals(type) && email == null) {
			email = user.getEmail();
		}

		//组装email
		Email newEmail = new Email();
		newEmail.setMemberId(user.getId());
		newEmail.setMemberName(user.getName());
		if ("reset".equals(type) && email == null) {
			newEmail.setType(3);//重置email
		} else if("recover".equals(type)) {
			newEmail.setType(2);//重置email
		} else if("again".equals(type)) {
			newEmail.setType(5);//重新绑定
		} else {
			newEmail.setType(1);//注册email
		}
		if ("approve".equals(type) || "recover".equals(type) || "again".equals(type) ) {
			newEmail.setTo(new String[] { email });
		} else if ("reset".equals(type)) {
			newEmail.setTo(new String[] { user.getEmail() });
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("#email_account#", getEmailPort().getEmailAccount());
		map.put("#web_name#", getSysConfig("site_name"));
		map.put("#web_url#", "" + getSysConfig("site_domain"));//http://
		map.put("#code#", verifyCode);
		map.put("#web_now_time#", DateUtil.dateTimeFormat(new Date()));
		map.put("#logo#", PropertiesUtil.getImageHost() + getSysConfig("site_logo"));
		map.put("#member_name#", user.getName());
		map.put("#email#", email);
		map.put("#web_time#", DateUtil.getCurrentDateStr());
		map.put("#member_url#", "" + getSysConfig("site_domain") + "/member/member/center");//http://
		map.put("#service_qq_name#", getSysConfig("service_qq"));
		map.put("#service_tel#", getSysConfig("service_tel"));
		map.put("#service_hours#", getSysConfig("service_hours"));
		map.put("#site_copyright#", getSysConfig("site_copyright"));
		map.put("#valicode#", verifyCode);
		newEmail.setTemplateParam(map);
		if ("reset".equals(type)) {
			newEmail.setTemplateType(4);
		} else if ("approve".equals(type)) {
			newEmail.setTemplateType(1);
		} else if ("recover".equals(type)) {
			newEmail.setTemplateType(6);
		} else if ("again".equals(type)) {
			newEmail.setTemplateType(5);
		}

		Map<String, Object> sessionMap = new HashMap<String, Object>();
		Session session = SecurityUtils.getSubject().getSession();
		sessionMap.put("code", verifyCode);
		sessionMap.put("email", email);
		if (session.getAttribute("sessionMap") != null) {
			session.removeAttribute("sessionMap");
		}
		session.setAttribute("sessionMap", sessionMap);
		boolean sendStatus = emailUtil.send(newEmail, EmailUtil.SEND_IMMEDIATE);
		if (sendStatus == false) {
			return errorJsonResonse("邮箱发送失败");
		}
		
		return successJsonResonse(sessionMap);
	}
	
	/**
	 * 验证验证码
	 * @param email
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse verifyEmail(String email, String code,String memberId) throws Exception{
		//获取当前用户
		MbMember user = (MbMember) this.getMbMember(Long.valueOf(memberId));
		
		//判断邮箱是否被使用
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("email", email));
		whereList.add(Where.notEq("id", user.getId()));
		String errorMsg = validateExist(email, "邮箱", Module.MEMBER, Function.MB_MEMBER, whereList);
		if(org.apache.commons.lang.StringUtils.isNotEmpty(errorMsg)){
			return errorJsonResonse("验证成功");
		}
		
		//插入邮箱验证
		MbApproveEmail appEmail = new MbApproveEmail();
		appEmail.setMemberId(user.getId());
		appEmail.setMemberName(user.getName());
		appEmail.setEmail(email);
		appEmail.setAddTime(DateUtil.getCurrentTime());
		appEmail.setStatus(1);
		appEmail.setToken("adad");
		this.baseService.insert(Module.MEMBER, Function.MB_EMAIL, appEmail);

		//更新member表
		user.setEmail(email);
		user.setIsEmail(1);
		this.baseService.updateById(Module.MEMBER, Function.MB_MEMBER, user);
		return successJsonResonse("验证成功");
	}
	
	
	/**
	 * 邮箱号码检查
	 * @param email
	 * @return
	 */
	public DyPhoneResponse checkEmail(String email) throws DyServiceException{
		if(StringUtils.isBlank(email)){
			return errorJsonResonse("邮箱号不能为空！");
		}
		if(!StringUtils.checkEmail(email)){
			return errorJsonResonse("邮箱号格式错误！");
		}
		
		//验证邮箱是否已存在
		QueryItem emailQueryItem = new QueryItem("member","email");
		List<Where> where = new ArrayList<Where>();
		where.add(new Where("email",email));
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("status",1,"=",true));
		ands.add(new NameValue("status",-2,"=",true));
		where.add(new Where(ands));
		emailQueryItem.setWhere(where);
		MbApproveEmail approveEmail = (MbApproveEmail) this.baseService.getOne(emailQueryItem, MbApproveEmail.class);
		Map<String,String> statusMap = new HashMap<String,String>();
		if(approveEmail!=null){
			statusMap.put("status", "1");
			return errorJsonResonse("邮箱已经存在");
		}else{
			statusMap.put("status", "0");
		}
		
		return successJsonResonse(statusMap);
	}
	
	/**
	 * 获取认证邮箱
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getAppEmail(String memberId) throws Exception {
		if (StringUtils.isBlank(memberId)) {
			return errorJsonResonse("登录标识不能为空");
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		
		if(StringUtils.isBlank(user.getEmail())){
			return successJsonResonse(user);
		}
		
		String[] emails = user.getEmail().split("@");
		String email = emails[0].substring(0, 1) + "****@" + emails[1];
		user.setEmail(email);
		if (user == null) {
			return errorJsonResonse("用户不存在");
		}
		return successJsonResonse(user);
	}
	
	/**
	 * 获取系统配置
	 * @throws Exception 
	 */
	private String getSysConfig(String type) throws Exception{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		item.getWhere().add(Where.eq("nid", type));
		SysSystemConfig config = this.baseService.getOne(item, SysSystemConfig.class);
		return config.getValue();
	} 
	
	/**
	 * 获取邮件发送配置
	 * @throws Exception 
	 */
	private SysEmailPort getEmailPort() throws Exception{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_EMAILPORT);
		item.getWhere().add(Where.eq("status", 1));
		item.setOrders("sort_index");
		List<SysEmailPort> list = this.baseService.getList(item, SysEmailPort.class);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	private String validateExist(Object fieldValue, String viewName, String moudle, String function, List<Where> whereList) throws Exception {
		if(whereList == null || whereList.size() <= 0) return null;
		
		QueryItem queryItem = new QueryItem(moudle, function);
		queryItem.setWhere(whereList);
		queryItem.setFields("1");
		
		List result = (List)this.baseService.getList(queryItem, Map.class);
		if(result == null || result.size() <= 0) return null;
		
		return "该邮箱已存在";
	}
	
	/**
	 * 通过email或者phone查询用户
	 * @throws Exception 
	 */
	public MbMember getUser(Object keywords) throws Exception{
		QueryItem queryItem = new QueryItem(Module.MEMBER,Function.MB_MEMBER);
		List<Where> where = new ArrayList<Where>();
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("name",keywords,"=",true));
		ands.add(new NameValue("phone",keywords,"=",true));
		ands.add(new NameValue("email",keywords,"=",true));
		where.add(new Where(ands));
		queryItem.setWhere(where);//设置where条件
		MbMember member = this.baseService.getOne(queryItem,MbMember.class);
		return member;
	}
	/**
	 * 验证邮箱验证码
	 * @param email
	 * @param code
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public DyPhoneResponse verifyCode(String email,String code,String type) throws Exception{
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		Map<String, Object> sessionMap = (Map<String, Object>) request.getSession().getAttribute("sessionMap") ;
		if (sessionMap == null) {//session已过期
			return this.errorJsonResonse("验证码错误");
		} else {
			//session未过期
			String sessionCode = (String) sessionMap.get("code");
			String sessionEmail = (String) sessionMap.get("email");
			if (!email.equals(sessionEmail)) {
				return this.errorJsonResonse("邮箱不正确");
			}
			if (!sessionCode.equals(code)) {
				return this.errorJsonResonse("验证码错误");
			}
		}
		return successJsonResonse("验证成功");
	}
}
