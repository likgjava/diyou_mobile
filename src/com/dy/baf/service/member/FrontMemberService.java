package com.dy.baf.service.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbCreditRank;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.MemberService;

@Service("mobileFrontMemberService")
public class FrontMemberService extends MobileService {

	@Autowired
	private MemberService memberService;
	@Autowired
	private BaseService baseService;

	/**
	 * 获取认证信息（账户详情）
	 * @param login_token
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getApprove(String login_token) throws Exception {			
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("用户ID不能为空");
		}
		QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		userItem.setWhere(new Where("id", login_token));
		MbMember member = this.baseService.getOne(userItem, MbMember.class);
		if(member == null){
			return errorJsonResonse("用户不存在");
		}
		
		/**
		 * 获取银行卡信息
		 */
		QueryItem bankItem = new QueryItem(Module.MEMBER, Function.MB_BANK);
		bankItem.setWhere(new Where("member_id", member.getId()));
		bankItem.setWhere(new Where("status", 1));
		bankItem.setFields("bank_nid,account,name");
		
		
		/**
		 * 如果绑定多张银行卡，则取其中一条
		 */
		List<MbMemberBank> bankList = this.baseService.getList(bankItem, MbMemberBank.class);
		MbMemberBank bank = null;
		if(bankList != null && bankList.size() > 0){
			bank = bankList.get(0);
		}
		
		/**
		 * 获取实名信息
		 */
		MbApproveRealname realname = null ;
		try {
			QueryItem realnameItem = new QueryItem(Module.MEMBER, Function.MB_REALNAME);
			realnameItem.setWhere(new Where("member_id", member.getId()));
			realnameItem.setWhere(Where.notEq("status", -1));
			realnameItem.setFields("positive_card,back_card,realname,card_id");
			List<MbApproveRealname> realNameList = baseService.getList(realnameItem, MbApproveRealname.class);
			
			if(realNameList != null && realNameList.size() > 0) realname = realNameList.get(0);
		} catch (Exception e) {
			
		}
		
		
		Map<String,Object> map = memberService.getApprove(member);
		map.put("member_name", member.getName());
		map.put("paypassword", member.getPaypassword()!=null? member.getPaypassword():"");
		if(realname != null){
			map.put("positive_card", PropertiesUtil.getImageHost()+realname.getPositiveCard());
			map.put("back_card", PropertiesUtil.getImageHost()+realname.getBackCard());
			map.put("card_id", realname.getCardId().substring(0, 3)+"************"+realname.getCardId().substring(realname.getCardId().length()-3));
			map.put("realname", realname.getRealname().substring(0, 1)+"**");
		}else {
			map.put("positive_card", "");
			map.put("back_card", "");
			map.put("card_id", "");
			map.put("realname", "");
		}
		map.put("phone", member.getPhone() == null ? "" : member.getPhone());
		String emailAuth=this.getSysValue("email_auth");
		if("2".equals(emailAuth)){
			map.put("is_email", 1) ;
		}else{
			map.put("is_email", member.getIsEmail()) ;
		}
//		map.put("email", StringUtils.isNotBlank(member.getEmail()) ? member.getEmail() : "");
		if(bank != null){
			map.put("account", bank.getAccount());
			map.put("bank_name", bank.getName());
			map.put("bankName", bank.getName()) ;
		}else{
			map.put("account", "");
			map.put("bank_name", "");
			//Wap需要
			map.put("bankName", "未绑定") ;
		}
		map.put("register", member.getTrustAccount());
		map.put("password", member.getPassword());
		map.put("register", StringUtils.isNotBlank(member.getTrustAccount()) ? 1 : -1);
		map.put("enterprise", member.getGroup());
		
		//add by panxh at 2016年9月21日  控制身份正图片是否需要显示上传
		map.put("is_realname_open", this.getSysValue("idcard"));
		//end by panxh at 2016年9月21日  控制身份正图片是否需要显示上传
		
		return successJsonResonse(map);
	}
	
	/**
	 * 获取会员信息
	 * @param xmdy
	 * @param diyou
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getInfo(String memberId) throws Exception{
		
		MbMember member = this.getMbMember(Long.valueOf(memberId));
		
		QueryItem infoItem = new QueryItem(Module.MEMBER,Function.MB_MEMBERINFO);
		infoItem.getWhere().add(Where.eq("member_id", memberId));
		infoItem.setFields("avatar");
		MbMemberInfo memberInfo = (MbMemberInfo) this.baseService.getOne(infoItem, MbMemberInfo.class);
		
		
		//获取信用等级
		String leveCredit=getLevelCredit(member.getCreditPoint().toString());
		Map<String,Object> responeMap = new HashMap<String, Object>();
		responeMap.put("credit_name", leveCredit);
		responeMap.put("credit_point", member.getCreditPoint());
		responeMap.put("avatar", null != memberInfo && memberInfo.getAvatar()!=null?PropertiesUtil.getImageHost()+memberInfo.getAvatar():"");
		responeMap.put("member_name", member.getName());
		return successJsonResonse(responeMap);
	}
	
	
	/**
	 * 积分等级
	 */
	private String getLevelCredit(String value) throws Exception{
		QueryItem item=new QueryItem( Module.MEMBER, Function.MB_RANKCONFIG);
		
		item.setWhere(new Where("start_point",value,"<="));
		item.setWhere(new Where("end_point", value, ">="));   
		MbCreditRank data=this.baseService.getOne(item,MbCreditRank.class);	   		
		return data == null ? "" : data.getIcon();
	}
		
}
