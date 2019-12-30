package com.dy.baf.controller.phone.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.service.member.FrontMemberService;
import com.dy.baf.service.trust.TrustRequestService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.trust.entity.BankMap;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;

/**
 * 
 * 
 * @Description: 用户管理
 * @author 波哥
 * @date 2015年9月9日 上午9:07:19 
 * @version V1.0
 */
@Controller(value="appFrontMemberController")
public class FrontMemberController extends AppBaseController {
	@Autowired
	private FrontMemberService frontMemberService;
	
	@Autowired
	private TrustRequestService trustRequestService;
	
	/**
	 * 账户详情
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/getApprove")
	public DyPhoneResponse getApprove(String xmdy,String diyou){
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			//Modify 先刷新银行卡
			doUserBindCard(login_token);
			
			return frontMemberService.getApprove(login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	

	public DyResponse isApprove(String type) throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		if (user == null || user.getId() == null) {
			return createLoginError("您未登录或登录超时");
		}
		user = getMember(user.getId());
		if("all".equals(type)){
			//是否实名认证或手机认证
			if(user.getIsRealname() !=1 || user.getIsPhone() != 1 || user.getIsEmail() !=1 || user.getPaypassword() == null){
				return createApproveError("请先进行认证!");
			}
		}else if("realPhone".equals(type)){
			//是否实名认证或手机认证
			if(user.getIsRealname() !=1 || user.getIsPhone() != 1){
				return createApproveError("请先进行实名认证或手机认证!");
			}
		}else if("realname".equals(type)){
			//是否实名认证
			if(user.getIsRealname() !=1){
				return createApproveError("请先进行实名认证!");
			}
		}else if("phone".equals(type)){
			//是否手机验证
			if(user.getIsPhone() != 1){
				return createApproveError("请先进行手机认证!");
			}
		}
		return this.createSuccessJsonResonse(null);
	}

	/**
	 * 系统是否启用自动投标
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isBorrowAutoSwitchOpen() {
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("value");
		queryItem.setWhere(new Where("nid", "borrow_auto_switch"));
		
		Map result = null;
		try {
			result = getOneByMap(queryItem, Module.SYSTEM, Function.SYS_CONFIG);
		} catch (Exception e) {
		}
		
		return result != null && "1".equals(result.get("value"));
	}
	
	/**
	 * 汇付绑卡特殊处理（因没有同步通知导致异步通知调取不到）
	 * @Description: TODO
	 * @CreateTime: 2019年10月21日 下午5:53:57
	 * @author: Administrator
	 * @throws Exception
	 * @throws
	 */
	private void doUserBindCard(String login_token) throws Exception {	
		
		QueryItem userItem = new QueryItem();
		userItem.setWhere(new Where("id", login_token));
		MbMember user = this.getOneByEntity(userItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class);
		if(user != null){
			/**
			 * 获取本地银行卡列表
			 */
			QueryItem bankItem = new QueryItem();
			bankItem.getWhere().add(Where.eq("member_id", user.getId()));
			bankItem.getWhere().add(Where.eq("status", 1));
			List<MbMemberBank> list = (List<MbMemberBank>) this.getListByEntity(bankItem, Module.MEMBER, Function.MB_BANK, MbMemberBank.class);
			List<MbMemberBank> bankTrustList = new ArrayList<MbMemberBank>();
			List<String> localList = new ArrayList<String>();
			for (MbMemberBank mbMemberBank : list) {
				localList.add(mbMemberBank.getAccount());
			}
			
			/**
			 * 获取托管方银行卡列表
			 * 并insert本地没有的卡号
			 */
			trustManager.setTrustRequestService(trustRequestService);
			List<Map<String,String>> trustBankList = trustManager.getCardInfo(user.getTrustAccount(), null);
			
			Map<String,String> trusBanktMap = new HashMap<String, String>();
			if(!CollectionUtils.isEmpty(trustBankList)){
				for (Map<String, String> map2 : trustBankList) {
					trusBanktMap.put(map2.get("CardId"), map2.get("ExpressFlag"));
					//如果本地不存在，则添加到本地
					QueryItem realnameItem = new QueryItem();
					realnameItem.setWhere(Where.eq("member_id", user.getId()));
					realnameItem.setFields("realname");
					MbApproveRealname realname = (MbApproveRealname) this.getOneByEntity(realnameItem, Module.MEMBER, Function.MB_REALNAME, MbApproveRealname.class);
					if(!localList.contains(map2.get("CardId"))){
						MbMemberBank newBank = new MbMemberBank();
						newBank.setMemberId(user.getId());
						newBank.setMemberName(user.getName());
						newBank.setBankNid(map2.get("BankId"));
						newBank.setIsQuickpay("Y".equals(map2.get("ExpressFlag")) ? 1 : -1);
						newBank.setName(new BankMap().get(map2.get("BankId")));
						newBank.setAccount(map2.get("CardId"));
						newBank.setRealname(realname.getRealname());
						newBank.setAddTime(DateUtil.getCurrentTime());
						newBank.setStatus(1);
						this.insert(Module.MEMBER, Function.MB_BANK, newBank);
						
						bankTrustList.add(newBank);
					}
				}
			}
			/**
			 * 本地存在托管方不存在的情况下解绑本地银行卡
			 * 
			 */
			for (MbMemberBank mbMemberBank : list) {
				if(!trusBanktMap.containsKey(mbMemberBank.getAccount())){
					mbMemberBank.setUpdateTime(DateUtil.getCurrentTime());
					mbMemberBank.setStatus(-1);
					this.update(Module.MEMBER, Function.MB_BANK, mbMemberBank);
				}else{
					if(mbMemberBank.getIsQuickpay() == 1 && !"Y".equals(trusBanktMap.get(mbMemberBank.getAccount()))){
						mbMemberBank.setUpdateTime(DateUtil.getCurrentTime());
						mbMemberBank.setIsQuickpay(-1);
						this.update(Module.MEMBER, Function.MB_BANK, mbMemberBank);
					}
				}
			}
		}
	}
	
	
}