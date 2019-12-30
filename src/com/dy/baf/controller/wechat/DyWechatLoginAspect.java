package com.dy.baf.controller.wechat;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysWechatUsers;
import com.dy.baf.service.system.LoginService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.Constant;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.WechatService;

@Aspect
@Component
@EnableAspectJAutoProxy
public class DyWechatLoginAspect extends WechatBaseController {

	@Autowired
	private WechatService wechatService;
	@Autowired
	private LoginService loginService;
	protected Logger logger = Logger.getLogger(this.getClass());

	@Around(value="@annotation(com.dy.baf.annotation.WechatLogin)")
	public Object aroundAdvice(final ProceedingJoinPoint pjp) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String code = request.getParameter("code");
		if (StringUtils.isNotBlank(code)) {
			try {
				String openId = wechatService.getOpenId(code);
				this.getSession().setAttribute("openId", openId);
				QueryItem queryItem = new QueryItem(Module.SYSTEM, Function.SYS_WECHAT_USERS);
				queryItem.setWhere(Where.eq("wei_id", openId));
				SysWechatUsers wechatUsers = (SysWechatUsers) this.getOne(queryItem, SysWechatUsers.class);
				if (wechatUsers != null) {
					int memberId = wechatUsers.getUserId();
					QueryItem item = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
					item.setWhere(Where.eq("id", memberId));
					MbMember mbMember = (MbMember) this.getOne(item, MbMember.class);
					loginService.wechatLogin(mbMember.getName(), this.getSession(), IpUtil.ipStrToLong(this.getRemoteIp()));
					System.out.println("自动登录成功");
				} else {
					if(request.getRequestURI().endsWith("bindLogin"))
					this.getSession().removeAttribute(Constant.SESSION_USER);
					System.out.println("用户未绑定微信号，自动登录失败");
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			System.out.println("用户未关注，自动登录失败");
		}
		return pjp.proceed();
	}
}
