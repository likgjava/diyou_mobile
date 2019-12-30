package com.dy.baf.service.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApprovePhone;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.StringUtils;
import com.dy.core.utils.sms.SmsUtil;
import com.dy.httpinvoker.MemberService;

/**
 * 
 * 
 * @Description: 手机相关类
 * @author 波哥
 * @date 2015年9月9日 上午9:05:37 
 * @version V1.0
 */
@Service("mobilePhoneService")
public class PhoneService extends MobileService{

	@Autowired
	private BaseService baseService;
	@Autowired
	public SmsUtil smsUtil;
	@Autowired
	private MemberService memberService;
	
	/**
	 * 发送短信
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse sendSms(String type, Long phone, String memberId, String is_check, String msg_code) throws Exception{
		if(StringUtils.isBlank(msg_code)){
			return errorJsonResonse("验证码不能为空");
		}
		if(StringUtils.isBlank(type)){
			return errorJsonResonse("发送类型不能为空");
		}
		
		Long sendPhone = phone;
		MbMember user = new MbMember();
		if(StringUtils.isNotBlank(memberId)){
			QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
			userItem.getWhere().add(Where.eq("id", memberId));
			user = this.getMbMember(Long.valueOf(memberId));
			if (user.getId() != null) {
				if (sendPhone == null) {
					sendPhone = user.getPhone();
				}
			}
		}
		if (sendPhone == null) {
			return errorJsonResonse("请输入您的手机号码");
		}
		
		if (phone != null && StringUtils.isBlank(memberId)) {
			QueryItem userItem = new QueryItem();
			userItem.getWhere().add(Where.eq("phone", phone));
			user = (MbMember) this.getOne(Module.MEMBER, Function.MB_MEMBER, userItem, MbMember.class);
		}
		if("reg".equals(type)&&user!=null){
			return errorJsonResonse("手机已存在");
		}
		Integer smsType = null;
		if ("reset".equals(type)) {//解绑手机
			smsType = 4;
		} else if ("approve".equals(type)) {//认证手机
			smsType = 3;
		} else if ("company".equals(type)) {//企业借款
			smsType = 99;
		} else if ("pwd".equals(type)) {//找回密码
			smsType = 2;
		} else if ("login".equals(type)) {
			smsType = 17;
		} else {
			smsType = 3;
		}
		Map<String, String> templateParam = new HashMap<String,String>();
		templateParam.put("#code#", msg_code);
		templateParam.put("#service_tel#", this.getSysValue("service_tel"));
		if(null == user || user.getId() == null){
			//注册时发送短信
			smsUtil.send(String.valueOf(sendPhone),10L,templateParam,1,1,0L,"");
		}else{
			smsUtil.send(String.valueOf(sendPhone),10L,templateParam,smsType,1,user.getId(),user.getName());
		}
		return successJsonResonse(sendPhone);
	}
	
	/**
	 * 解绑
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse resetPhone(String memberId,String new_phone) throws Exception {
		//解绑
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		memberService.unbundling(user); 
		
		DyPhoneResponse dyPhoneResponse=this.approvephone(Long.valueOf(new_phone), user);
		if(dyPhoneResponse.getCode()==DyPhoneResponse.OK){
			return successJsonResonse("绑定成功");
		}
		
		return successJsonResonse("解绑成功");
	}
	
	/**
	 * 绑定手机
	 */
	public DyPhoneResponse approvephone(Long phone, MbMember user) throws Exception{
			
		if(phone == null){
			return errorJsonResonse("手机号不能为空!");
		}
		
		//绑定
		//判断手机是否被占用
		QueryItem userQueryItem = new QueryItem(Module.MEMBER,Function.MB_MEMBER);
		userQueryItem.getWhere().add(Where.eq("phone", phone));
		MbMember muser = (MbMember) this.baseService.getOne(userQueryItem, MbMember.class);
		
		QueryItem phoneQueryItem = new QueryItem(Module.MEMBER,Function.MB_PHONE);
		List<Where> where = new ArrayList<Where>();
		where.add(Where.eq("phone", phone));
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("status",1,"=",true));
		ands.add(new NameValue("status",-2,"=",true));
		where.add(new Where(ands));
		phoneQueryItem.setWhere(where);
		MbApprovePhone mphone = (MbApprovePhone) this.baseService.getOne(phoneQueryItem, MbApprovePhone.class);
		if(muser !=null || mphone !=null){
			return errorJsonResonse("该手机已存在!");
		}
		//更新用户
		user.setPhone(phone);
		user.setIsPhone(1);
		
		memberService.updateUserAndPhone(user);
		
		String returnPhone =String.valueOf(phone);
		returnPhone = returnPhone.substring(0,returnPhone.length()-(returnPhone.substring(3)).length())+"****"+returnPhone.substring(7);
		
		return successJsonResonse(user);
	}
	
	/**
	 * 获取用户认证的手机
	 * @param login_token
	 * @return
	 */
	public MbMember getPhone(String memberId) throws DyServiceException{
		return this.getMbMember(Long.valueOf(memberId));
	}
	
	/**
	 * 手机号（格式/是否存在）检测
	 * @param phone
	 * @return
	 */
	public DyPhoneResponse checkPhone(String phone) throws Exception{
		if(StringUtils.isBlank(phone)){
			return errorJsonResonse("手机号码不能为空！");
		}
		if(!StringUtils.checkPhone(phone)){
			return errorJsonResonse("手机号码格式错误！");
		}
		//判断手机号码是否被他人占用
		QueryItem phoneQuery = new QueryItem(Module.MEMBER,Function.MB_MEMBER);
		phoneQuery.getWhere().add(Where.eq("phone", phone));
		MbMember phoneMember = (MbMember) this.baseService.getOne(phoneQuery, MbMember.class);
		Map<String,String> statusMap = new HashMap<String,String>();
		if(phoneMember!=null){
			return errorJsonResonse("手机号已存在");
		}else{
			statusMap.put("status", "0");
		}
		return successJsonResonse(statusMap);
	}
	
	/**
	 * 验证短信验证码
	 * @param phone
	 * @param phoneCode
	 * @param type
	 * @param sessionMap 
	 */
	public boolean verifyCode(String phone, String phoneCode, String type, Map<String, Object> sessionMap) {
		Long phoneNum = 0L ;
		if(StringUtils.isNotBlank(phone)){
			phoneNum = Long.valueOf(phone);
		}
		Long sessionPhone = sessionMap == null ? null : (Long) sessionMap.get("phone");
		String sessionCode = sessionMap == null ? "" : (String) sessionMap.get("sys_code");
		if(!phoneNum.equals(sessionPhone)){
			return false;
		}
		if(!phoneCode.equals(sessionCode)){
			return false;
		}
		return true ;
	}
	
}
