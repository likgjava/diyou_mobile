package com.dy.baf.controller.wechat.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.wechat.WechatBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysWechatUsers;
import com.dy.baf.service.system.LoginService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.WechatService;

/**
 * 绑定微信
 * 
 * @author Administrator
 * 
 */
@Controller(value = "wechatBindController")
public class WechatBindController extends WechatBaseController {

	@Autowired
	private WechatService wechatService;
	@Autowired
	private LoginService loginService;

	@ResponseBody
	@RequestMapping("/system/bindopenid")
	public DyResponse bindOpenId(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String openId = (String) request.getSession().getAttribute("openId");
		if (StringUtils.isBlank(openId)) {
			return this.createErrorJsonResonse("绑定超时，请关闭页面重新点击绑定账户");
		}
		String memberName = request.getParameter("account");
		String password = request.getParameter("password");

		DyPhoneResponse dyPhoneResponse = this.loginService.login(memberName, password, null, null, null, null, this.getSession(), IpUtil.ipStrToLong(this.getRemoteIp()));
		if (dyPhoneResponse.getCode() == 100) {
			return this.createErrorJsonResonse("用户名或密码错误");
		}

		Map<String, Object> map = new HashMap<String, Object>();
		MbMember mbMember = (MbMember) getSessionAttribute(Constant.SESSION_USER);
		map.put("wei_id", openId);
		map.put("user_id", mbMember.getId());
		map.put("addtime", DateUtil.getCurrentTime());
		try {
			QueryItem queryItem = new QueryItem(Module.SYSTEM, Function.SYS_WECHAT_USERS);
			queryItem.setWhere(Where.eq("user_id", mbMember.getId()));
			SysWechatUsers wechatUsers = (SysWechatUsers) this.getOne(queryItem, SysWechatUsers.class);
			if (wechatUsers != null) {
				return this.createErrorJsonResonse("用户已绑定过微信号");
			}
			this.insert(Module.SYSTEM, Function.SYS_WECHAT_USERS, map);
		} catch (Exception e) {
			return this.createErrorJsonResonse(e.getMessage());
		}
		return this.createSuccessJsonResonse("操作成功", "操作成功");
	}

	@ResponseBody
	@RequestMapping("/system/unbindopenid")
	public DyResponse unbindOpenId(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String openId = (String) request.getSession().getAttribute("openId");
		if (StringUtils.isBlank(openId)) {
			return this.createErrorJsonResonse("解绑超时，请关闭页面重新点击绑定账户进行解绑");
		}

		QueryItem queryItem = new QueryItem(Module.SYSTEM, Function.SYS_WECHAT_USERS);
		queryItem.setWhere(Where.eq("wei_id", openId));
		SysWechatUsers wechatUsers = (SysWechatUsers) this.getOne(queryItem, SysWechatUsers.class);
		if (wechatUsers == null) {
			return this.createErrorJsonResonse("用户微信号已解绑");
		}
		this.getSession().removeAttribute(Constant.SESSION_USER);
		DyResponse dyResponse = this.deleteById(wechatUsers.getId(), Module.SYSTEM, Function.SYS_WECHAT_USERS);
		if (dyResponse.getStatus() != 200) {
			return dyResponse;
		}
		return this.createSuccessJsonResonse("操作成功", "操作成功");
	}	
}
