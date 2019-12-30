package com.dy.baf.service.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.httpinvoker.MemberService;

/**
 * 
 * @Description: 用户登录
 * @author 波哥
 * @date 2015年9月3日 下午10:52:06 
 * @version V1.0
 */
@Service("mobileLoginService")
public class LoginService extends MobileService {

	@Autowired
	private MemberService memberService;
	@Autowired
	private BaseService baseService;
	
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
	public DyPhoneResponse login(String memberName,String password,String code,String type,String phoneType,String clientId,Session session,Long ip) throws Exception {
			
			//非空校验
			if(StringUtils.isEmpty(memberName)) return errorJsonResonse("用户名不能为空");
			if(StringUtils.isEmpty(password) && "2".equals(type)) return errorJsonResonse("密码不能为空");
			
			//验证该用户是否存在
			QueryItem queryItem = new QueryItem("member", "member");
			List<Where> where = new ArrayList<Where>();
			List<NameValue> ands = new ArrayList<NameValue>();
			ands.add(new NameValue("name",memberName,"=",true));
			ands.add(new NameValue("phone",memberName,"=",true));
			ands.add(new NameValue("email",memberName,"=",true));
			where.add(new Where(ands));
			queryItem.setWhere(where);//设置where条件
			MbMember member = this.baseService.getOne(queryItem,MbMember.class);
			if (member == null) {
				return errorJsonResonse("用户不存在!");
			}
			if (member.getStatus() == -1) {
				return errorJsonResonse("账号已被锁定,请联系管理员");
			}
			//验证该用户是否为黑名单
			String configValue = getConfigValue("site_blacklist_member");
			if(configValue.contains(memberName))return errorJsonResonse("用户已加入黑名单,请联系管理员");
			String pwdIsOpend = getConfigValue("error_pwd_isopen");
			Long currentTime = DateUtil.getCurrentTime();
			String errorPwdLong  = getConfigValue("error_pwd_long");
			float pwdLong = (errorPwdLong == null || "".equals(errorPwdLong)) ? 0 : Float.parseFloat(errorPwdLong);
			if("1".equals(pwdIsOpend)){
				//验证用户是否锁定
				if (member.getStatus() != 1) {
					if(member.getLockTime() != null){
						//判断锁定时间是否大于系统时间
						if(DateUtil.minuteBetween(member.getLockTime(), currentTime) < pwdLong){
							session.removeAttribute("login_error");
							return errorJsonResonse("由于连续输入错误密码达到上限，账号已被锁定，请于"+ (pwdLong-DateUtil.minuteBetween(member.getLockTime(), currentTime)) +"分钟后重新登录");
						}else{
							member.setStatus(1);
							this.baseService.updateById(Module.MEMBER, Function.MB_MEMBER, member);
						}
					}
				}
			}
			if("1".equals(type)){
				//验证码登陆方式
				Map<String,Object> map = (Map<String, Object>) session.getAttribute("codeMap");
				Long sessionPhone = map == null ? null : (Long) map.get("phone");
				String sessionCode = map == null ? "" : (String) map.get("sys_code");
				if(!code.equals(sessionCode))return errorJsonResonse("验证码错误!");
				if(!memberName.equals(sessionPhone.toString()))return errorJsonResonse("手机号码不匹配! ");
				session.removeAttribute("codeMap");
				password = member.getPassword() ;
			}else{
				//验证密码是否正确
				String encryptPassword = "";
				if (member != null) {
					encryptPassword = SecurityUtil.md5(SecurityUtil.sha1(password + member.getPwdAttach()));
				}
				Integer login_error = (Integer) session.getAttribute("login_error");
				if (!encryptPassword.equals(member.getPassword())) {
					//记录错误次数
					if (login_error == null) {
						login_error = 1;
					}
					login_error = login_error + 1;
					session.setAttribute("login_error", login_error);
					session.setAttribute("username", memberName);
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("login_error", login_error);

					if("1".equals(pwdIsOpend)){
						int sysErrorTimes = StringUtils.isBlank(getConfigValue("error_pwd_times")) ? 0 : Integer.valueOf(getConfigValue("error_pwd_times"));
						//连续输错密码N次，锁定用户
						if (login_error >= sysErrorTimes+1) {
							member.setStatus(-2);
							member.setLockTime(currentTime);
							this.baseService.updateById(Module.MEMBER, Function.MB_MEMBER, member);
							return errorJsonResonse("由于连续输入错误密码达到上限，账号已被锁定，请于"+pwdLong+"分钟后重新登录");
						}else{
							return errorJsonResonse("密码错误"+(login_error-1)+"次,达到"+sysErrorTimes+"次将锁定账户");

						}
					}
					return errorJsonResonse("密码错误!");
				}
			}
			
			//加入Shiro身份验证
			Subject subject = SecurityUtils.getSubject(); 
			UsernamePasswordToken token = new UsernamePasswordToken(memberName, password); 
			subject.login(token); 
			
			//登录成功
			//插入登录log信息
			//更新最后登录时间和ip
			memberService.successLogin(member,ip);
			
			//把用户信息加入session
			session.setAttribute(Constant.SESSION_USER, member);
			QueryItem infoQueryItem = new QueryItem("member", "memberinfo");
			infoQueryItem.getWhere().add(Where.eq("member_id", member.getId()));
			MbMemberInfo memberInfo = this.baseService.getOne(infoQueryItem, MbMemberInfo.class);
			session.setAttribute("member_info", memberInfo);
			
			//从session中删除验证码信息
			session.removeAttribute(Constant.SESSION_VERIFY_CODE);
			session.removeAttribute("login_error");
			
			Map<String,Object> resonseMap = new HashMap<String, Object>();
			resonseMap.put("member", member);
			resonseMap.put("login_token", member.getId());
			return successJsonResonse(resonseMap);
	}
	
	/**
	 * 微信用户自动登录
	 * @param memberName
	 * @param session
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse wechatLogin(String memberName, Session session, Long ip) throws Exception {

		if (!memberName.equals(session.getAttribute("username"))) {
			session.removeAttribute("login_error");
		}

		// 验证该用户是否存在
		QueryItem queryItem = new QueryItem("member", "member");
		List<Where> where = new ArrayList<Where>();
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("name", memberName, "=", true));
		ands.add(new NameValue("phone", memberName, "=", true));
		ands.add(new NameValue("email", memberName, "=", true));
		where.add(new Where(ands));
		queryItem.setWhere(where);// 设置where条件
		MbMember member = this.baseService.getOne(queryItem, MbMember.class);
		if (member == null) {
			return errorJsonResonse("用户不存在!");
		}

		// 验证该用户是否为黑名单
		String configValue = getConfigValue("site_blacklist_member");
		if (configValue.contains(memberName))
			return errorJsonResonse("用户已加入黑名单,请联系管理员");

		// 加入Shiro身份验证
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(memberName, "");
		subject.login(token);

		// 登录成功
		// 插入登录log信息
		// 更新最后登录时间和ip
		memberService.successLogin(member, ip);

		// 把用户信息加入session
		session.setAttribute(Constant.SESSION_USER, member);
		QueryItem infoQueryItem = new QueryItem("member", "memberinfo");
		infoQueryItem.getWhere().add(Where.eq("member_id", member.getId()));
		MbMemberInfo memberInfo = this.baseService.getOne(infoQueryItem, MbMemberInfo.class);
		session.setAttribute("member_info", memberInfo);

		// 从session中删除验证码信息
		session.removeAttribute(Constant.SESSION_VERIFY_CODE);
		session.removeAttribute("login_error");

		Map<String, Object> resonseMap = new HashMap<String, Object>();
		resonseMap.put("member", member);
		resonseMap.put("login_token", member.getId());
		return successJsonResonse(resonseMap);

	}
	
	
	/**
	 * 查询系统设置
	 * @throws Exception 
	 */
	private String getConfigValue(String nid) throws Exception{
		QueryItem configItem = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		configItem.getWhere().add(Where.eq("nid", nid));
		SysSystemConfig sysConfig = this.baseService.getOne(configItem,  SysSystemConfig.class);
		String configValue = sysConfig.getValue();
		return configValue;
	}
}
