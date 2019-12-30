package com.dy.baf.service.member;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveEmail;
import com.dy.baf.entity.common.MbApprovePhone;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 用户信息
 * @author 波哥
 * @date 2015年9月10日 上午9:23:52 
 * @version V1.0
 */
@Service("mobilePasswrodService")
public class PasswrodService extends MobileService {

	@Autowired
	private BaseService baseService;
	
	/**
	 * 修改支付密码
	 * @param raw
	 * @param newPwd
	 * @param confirmPwd
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse editPaypwd(String raw,String newPwd,String confirmPwd, String memberId) throws Exception {
		
		
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("登录标识不能为空");
		}
		//验证两次密码是否一致
		if(!newPwd.equals(confirmPwd)){
			return errorJsonResonse("两次密码不一致");
		}
		//新密码与旧密码不能一致
		if(newPwd.equals(raw)) {
			return errorJsonResonse("新密码不能与旧密码一致");
		}
		//验证原密码是否正确
		
		QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		userItem.getWhere().add(Where.eq("id", memberId));
		MbMember user = this.baseService.getOne(userItem, MbMember.class);
		String paypassword = user.getPaypassword();
		if(paypassword == null ||"".equals(paypassword)){
			paypassword = user.getPassword();
			raw =  SecurityUtil.md5(SecurityUtil.sha1(raw+user.getPwdAttach()));
			if(!raw.equals(paypassword)){
				return errorJsonResonse("原支付密码不正确");
			}
		}else{
			raw =  SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+raw));
			if(!raw.equals(paypassword)){
				return errorJsonResonse("原支付密码不正确");
			}
		}
		//修改支付密码
		user.setPaypassword(SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+newPwd)));
		baseService.updateById(Module.MEMBER, Function.MB_MEMBER, user);
		
		return successJsonResonse("支付密码修改成功");
	}
	
	/**
	 * 登录密码修改
	 * @param raw
	 * @param newPwd
	 * @param confirmPwd
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse editPwd(String raw,String newPwd,String confirmPwd,String memberId) throws Exception {
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("登录标识不能为空");
		}
		//验证两次密码是否一致
		if(!newPwd.equals(confirmPwd))return errorJsonResonse("两次密码不一致");
		
		//新密码与旧密码不能一致
		if(newPwd.equals(raw))return errorJsonResonse("新密码不能与旧密码一致");
		//验证原密码是否正确
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		String password = user.getPassword();
		raw =  SecurityUtil.md5(SecurityUtil.sha1(raw+user.getPwdAttach()));
		if(!raw.equals(password))return errorJsonResonse("原密码不正确");
		if(newPwd.length()<6||newPwd.length()>16) return errorJsonResonse("密码必须在6-16位之间");
		Pattern pattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
		Matcher matcher = pattern.matcher(newPwd);
		if (!matcher.matches()) {
			return errorJsonResonse("密码只能为数字和字母组合的密码");
		}
		//修改密码
		user.setPassword(SecurityUtil.md5(SecurityUtil.sha1(newPwd+user.getPwdAttach())));
		baseService.updateById(Module.MEMBER, Function.MB_MEMBER, user);
		return successJsonResonse("密码修改成功");
	}
	
	
	/**
	 * 重置密码（找回密码）
	 * @param raw
	 * @param newPwd
	 * @param confirmPwd
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse editRecoverPwd(String username,String newPwd,String confirmPwd,String type) throws Exception {
		if(StringUtils.isBlank(type)){
			return errorJsonResonse("找回密码类型不能为空");
		}
		if(StringUtils.isBlank(username)){
			return errorJsonResonse("用户名不能为空");
		}
		//验证两次密码是否一致
		if(!newPwd.equals(confirmPwd))return errorJsonResonse("两次密码不一致");
		
		Long memberId = 0l;
		if("1".equals(type)){
			//手机找回
			QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_PHONE);
			userItem.getWhere().add(Where.eq("status", "1"));
			userItem.getWhere().add(Where.eq("phone", username));
			MbApprovePhone phone = this.baseService.getOne(userItem, MbApprovePhone.class);
			if(phone != null){
				memberId = phone.getMemberId();
			}else{
				return errorJsonResonse("用户不存在");
			}
		}else if("2".equals(type)){
			//手机找回
			QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_EMAIL);
			userItem.getWhere().add(Where.eq("status", "1"));
			userItem.getWhere().add(Where.eq("email", username));
			MbApproveEmail email = this.baseService.getOne(userItem, MbApproveEmail.class);
			if(email != null){
				memberId = email.getMemberId();
			}else{
				return errorJsonResonse("用户不存在");
			}
		}
		if(memberId > 0){
			MbMember user = this.getMbMember(memberId);
			//修改密码
			user.setPassword(SecurityUtil.md5(SecurityUtil.sha1(newPwd+user.getPwdAttach())));
			baseService.updateById(Module.MEMBER, Function.MB_MEMBER, user);
			return successJsonResonse("密码修改成功");
		}else{
			return successJsonResonse("参数非法");
		}
	}
}
