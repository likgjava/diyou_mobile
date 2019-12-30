package com.dy.baf.service.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveEmail;
import com.dy.baf.entity.common.MbApprovePhone;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.Condition;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.MemberService;

/**
 * 
 * @Description: 用户注册
 * @author 波哥
 * @date 2015年9月3日 下午5:58:52
 * @version V1.0
 */
@Service("mobileRegisterService")
public class RegisterService extends MobileService {

	@Autowired
	private BaseService baseService;
	@Autowired
	private MemberService memberService;

	/**
	 * 注册第一步填写信息
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse fillinfo(String phone, String password, String referrer, String invite_username, String ip) throws Exception {
		// 验证码校验
		if (StringUtils.isBlank(phone)) {
			return errorJsonResonse("手机号不能为空");
		}
		if (StringUtils.isBlank(password)) {
			return errorJsonResonse("密码不能为空");
		}
		// 判断手机是否合法
		if (!StringUtils.checkPhone(phone)) {
			return errorJsonResonse("手机号码格式错误！");
		}
		// 判断推广人是否存在
		if (referrer != null && !"".equals(referrer)) {
			MbMember user = getMember(referrer);
			if (user == null)
				return errorJsonResonse("推荐人不存在");
		}
		if (invite_username != null) {
			invite_username = new String(SecurityUtil.decode(invite_username), "UTF-8");
			MbMember user = getMember(invite_username);
			if (user == null)
				return errorJsonResonse("推广人不存在");
		}

		// 判断用户名是否被他人占用
		QueryItem usenameQuery = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		usenameQuery.getWhere().add(Where.eq("name", phone));
		MbMember member = this.baseService.getOne(usenameQuery, MbMember.class);
		if (member != null)
			return errorJsonResonse("用户名已存在!");

		// 判断手机号码是否被他人占用
		QueryItem phoneQuery = new QueryItem(Module.MEMBER, Function.MB_PHONE);
		phoneQuery.getWhere().add(Where.eq("phone", phone));
		MbMember phoneMember = this.baseService.getOne(phoneQuery, MbMember.class);
		if (phoneMember != null)
			return errorJsonResonse("手机已存在!");

		// 判断用户名是否作为邮箱号已经存在
		QueryItem emailQuery = new QueryItem(Module.MEMBER, Function.MB_EMAIL);
		emailQuery.getWhere().add(Where.eq("email", phone));
		MbApproveEmail email = this.baseService.getOne(emailQuery, MbApproveEmail.class);
		if (email != null) {
			return errorJsonResonse("用户名已存在!");
		}
		member = new MbMember();
		member.setName(RandomStringUtils.random(2, "abcdefghijklmnopqrstuvwxyz") + phone);
		member.setPassword(password);
		member.setPhone(Long.valueOf(phone));
		member.setRegisterIp(IpUtil.ipStrToLong(ip));
		// 注册提交
		memberService.regSubmit(member, IpUtil.ipStrToLong(ip));

		// 加入session
		QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		queryItem.getWhere().add(Where.eq("name", member.getName()));
		MbMember user = this.baseService.getOne(queryItem, MbMember.class);

		// 建立推广
		memberService.buildSpread(user, referrer, invite_username);

		//加入Shiro身份验证
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession() ;
		Subject subject = SecurityUtils.getSubject(); 
		UsernamePasswordToken token = new UsernamePasswordToken(user.getName(), member.getPassword()); 
		subject.login(token); 
		
		session.setAttribute(Constant.SESSION_USER, user);
		QueryItem infoQueryItem = new QueryItem("member", "memberinfo");
		infoQueryItem.getWhere().add(Where.eq("member_id", member.getId()));
		MbMemberInfo memberInfo = this.baseService.getOne(infoQueryItem, MbMemberInfo.class);
		session.setAttribute("member_info", memberInfo);
		
		// 插入登录log信息
		// 更新最后登录时间和ip
		memberService.successLogin(user, IpUtil.ipStrToLong(ip));
		return successJsonResonse(user);
	}

	/**
	 * 注册时用户账号校验
	 * 
	 * @param username
	 * @return
	 */
   public DyPhoneResponse checkExit(Map<String,String> responseMap) throws Exception {
	   String username = responseMap.get("username");
		/**
		 * 用户找回密码时name不为空
		 */
		String name = responseMap.get("name");
		if (StringUtils.isNotBlank(name)) {
			QueryItem usenameQuery = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
			
			List<Where> where = new ArrayList<Where>();
			List<NameValue> ands = new ArrayList<NameValue>();
			ands.add(new NameValue("name",name,"=",true));
			ands.add(new NameValue("phone",name,"=",true));
			ands.add(new NameValue("email",name,"=",true));
			where.add(new Where(ands));
			usenameQuery.setWhere(where);//设置where条件
			usenameQuery.setFields("id,name,email,phone");
			MbMember bmember = this.baseService.getOne(usenameQuery, MbMember.class);
			
			if(bmember != null && StringUtils.isNotBlank(bmember.getName())){
				
				//手机找回密码
				if(StringUtils.checkPhone(username)){
					QueryItem phoneQuery = new QueryItem(Module.MEMBER,Function.MB_PHONE);
					phoneQuery.setWhere(Where.eq("status", 1));
					phoneQuery.setWhere(Where.eq("phone", username));
					phoneQuery.setFields("id,member_name");
					MbApprovePhone approvePhone = this.baseService.getOne(phoneQuery, MbApprovePhone.class);
					if(approvePhone != null && approvePhone.getId() != null){
						if(!bmember.getName().equals(approvePhone.getMemberName())){
							return errorJsonResonse("该手机号与用户绑定的手机号不一致");
						}
					}else{
						return errorJsonResonse("该手机号与用户绑定的手机号不一致");
					}
				}
				//邮箱找回密码
				if(StringUtils.checkEmail(username)){
					QueryItem phoneQuery = new QueryItem(Module.MEMBER,Function.MB_EMAIL);
					phoneQuery.setWhere(Where.eq("status", 1));
					phoneQuery.setWhere(Where.eq("email", username));
					phoneQuery.setFields("id,member_name");
					MbApproveEmail approveEmail = this.baseService.getOne(phoneQuery, MbApproveEmail.class);
					if(approveEmail != null && approveEmail.getId() != null){
						if(!bmember.getName().equals(approveEmail.getMemberName())){
							return errorJsonResonse("该邮箱与用户绑定的邮箱号不一致");
						}
					}else{
						return errorJsonResonse("该邮箱与用户绑定的邮箱号不一致");
					}
				}
			}
		}
		if (StringUtils.isBlank(username)) {
			return errorJsonResonse("请传入用户名");
		}
		Map<String, String> responeMap = new HashMap<String, String>();
		// 判断用户名是否已经存在
		QueryItem usenameQuery = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		usenameQuery.getWhere().add(Where.eq("name", username));
		MbMember member = this.baseService.getOne(usenameQuery, MbMember.class);
		if (member != null) {
			responeMap.put("status", "1");
			responeMap.put("type", "3");
			return successJsonResonse(responeMap);
		}
		// 判断用户名是否作为邮箱号已经存在
		QueryItem emailQuery = new QueryItem(Module.MEMBER, Function.MB_EMAIL);
		emailQuery.getWhere().add(Where.eq("email", username));
		emailQuery.getWhere().add(Where.eq("status", 1));
		MbApproveEmail email = this.baseService.getOne(emailQuery, MbApproveEmail.class);
		if (email != null) {
			responeMap.put("status", "1");
			responeMap.put("type", "1");
			return successJsonResonse(responeMap);
		}
		QueryItem phoneQuery = new QueryItem(Module.MEMBER, Function.MB_PHONE);
		phoneQuery.getWhere().add(Where.eq("phone", username));
		phoneQuery.getWhere().add(Where.eq("status", 1));
		MbApprovePhone phone = this.baseService.getOne(phoneQuery, MbApprovePhone.class);
		if (phone != null) {
			responeMap.put("status", "1");
			responeMap.put("type", "2");
			return successJsonResonse(responeMap);
		}
		responeMap.put("status", "0");
		return successJsonResonse(responeMap);
	}
	
	/**
	 * 注册或登录
	 * @param loginMember
	 * @return
	 * @throws DyServiceException
	 */
	public DyPhoneResponse regLogin(String account) throws DyServiceException{
		if(StringUtils.isBlank(account)){
			return errorJsonResonse("账号错误");
		}
		QueryItem item = new QueryItem() ;
		List<NameValue> conditionList = new ArrayList<NameValue>() ;
		item.setFields("count(1) isExist") ;
		conditionList.add(new NameValue("name", account, Condition.EQ)) ;
		conditionList.add(new NameValue("email", account, Condition.EQ, true)) ;
		conditionList.add(new NameValue("phone", account, Condition.EQ, true)) ;
		item.getWhere().add(new Where(conditionList));
		Map<String,Object> rstMap = this.getOne(Module.MEMBER, Function.MB_MEMBER, item) ;
		int count = MapUtils.getIntValue(rstMap, "isExist") ;
		boolean isPhone = StringUtils.checkPhone(account) ;
		if(0 >= count){
			if(isPhone){
				return successJsonResonse(1, null) ;
			}else{
				return errorJsonResonse("账号错误");
			}
		}else if(0 < count && isPhone){
			return successJsonResonse(2, null) ;
		}else{
			return successJsonResonse(3,null);
		}
	}

	/**
	 * 注册
	 * @return
	 * @throws DyServiceException
	 */
	public DyPhoneResponse wechatRegister(String account) throws DyServiceException {
		// 验证码校验
		if (StringUtils.isBlank(account)) {
			return errorJsonResonse("手机号不能为空");
		}
		// 判断手机是否合法
		if (!StringUtils.checkPhone(account)) {
			return errorJsonResonse("手机号码格式错误！");
		}
		QueryItem item = new QueryItem();
		List<NameValue> conditionList = new ArrayList<NameValue>();
		item.setFields("count(1) isExist");
		conditionList.add(new NameValue("name", account, Condition.EQ));
		conditionList.add(new NameValue("email", account, Condition.EQ, true));
		conditionList.add(new NameValue("phone", account, Condition.EQ, true));
		item.getWhere().add(new Where(conditionList));
		Map<String, Object> rstMap = this.getOne(Module.MEMBER, Function.MB_MEMBER, item);
		int count = MapUtils.getIntValue(rstMap, "isExist");
		if (count > 0) {
			return errorJsonResonse("手机号码已存在！");
		}
		return successJsonResonse(1, null);
	}
	
	/**
	 * 根据用户名查询用户
	 * 
	 * @throws Exception
	 */
	private MbMember getMember(String memberName) throws Exception {
		QueryItem item = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		item.getWhere().add(Where.eq("name", memberName));
		MbMember user = this.baseService.getOne(item, MbMember.class);
		return user;
	}

}
