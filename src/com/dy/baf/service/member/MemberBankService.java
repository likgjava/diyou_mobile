package com.dy.baf.service.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.trust.entity.BankMap;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 银行卡信息
 * @author 波哥
 * @date 2015年9月10日 上午9:23:52
 * @version V1.0
 */
@Service("mobileMemberBankService")
public class MemberBankService extends MobileService {

	@Autowired
	private BaseService baseService;

	/**
	 * 银行卡信息(用户绑定的银行卡列表)
	 * @param memberId
	 * @param isTrust
	 * @param trustType
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse index(String memberId, boolean isTrust, String trustType,String url) throws DyServiceException {
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("登录标识不能为空");
		}
		Map<String,Object> responseMap = new HashMap<String, Object>();
		
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		
		QueryItem bankItem = new QueryItem(Module.MEMBER, Function.MB_BANK);
		bankItem.getWhere().add(Where.eq("member_id", user.getId()));
		bankItem.getWhere().add(Where.eq("status", 1));
		bankItem.setFields("id,account,bank_nid,name,realname,status,is_quickpay");
		List<Map> bankList = this.baseService.getList(bankItem, Map.class);
		int is_paypassword = 0 ; 
		if(StringUtils.isNotBlank(user.getPassword())){
			is_paypassword = 1 ;
		}
		boolean is_quickpay=false;
		if(bankList != null && bankList.size() > 0){
			
			for (Map map : bankList) {
				/**注意：account字段app格式化，由于该方法是移动端公共的，如果wap需要格式化account，请另取变量或者jsp页面做格式化**/
				String account = map.get("account").toString();
				map.put("account2Wap", account.substring(0, 4) + "*********" + account.substring(account.length() - 4));
				String bankNid = map.get("bank_nid").toString() ;
				map.put("bank_name", BankMap.get(bankNid));
				map.put("bank_img", url.replace("#bank_nid#", bankNid)) ;
				map.put("realname", map.get("realname").toString().substring(0, 1)+"**") ;
				if(map.get("is_quickpay").toString().equals("1")){
					is_quickpay=true;
				}
			}
			responseMap.put("is_paypassword", is_paypassword) ;
			responseMap.put("is_bind", 1);
			responseMap.put("bank_info", bankList);
			//是否可以添加新卡
			if(is_quickpay){
				responseMap.put("is_add", 0);
			}else if(bankList.size()>=2){
				responseMap.put("is_add", 0);
			}else{
				responseMap.put("is_add", 1);
			}
			
		}else{
			responseMap.put("is_paypassword", is_paypassword) ;
			responseMap.put("is_bind", 0);
			responseMap.put("type", 0);
			responseMap.put("is_add", 1);
		}
		
		return successJsonResonse(responseMap);
	}
	
	
	/**
	 * 
	 * @param 绑卡
	 * @return
	 * @throws DyServiceException
	 */
	public DyPhoneResponse addBank(MbMemberBank bank,String payPassword) throws DyServiceException {
		QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		userItem.getWhere().add(Where.eq("id", bank.getMemberId()));
		MbMember user = this.baseService.getOne(userItem, MbMember.class);
		if (user.getIsRealname() == -1)
			return errorJsonResonse("尚未实名认证");
		if (user.getIsPhone() == -1)
			return errorJsonResonse("尚未手机认证");
		//查询是否已绑定银行卡
		QueryItem bankItem = new QueryItem(Module.MEMBER, Function.MB_BANK);
		bankItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbMemberBank memberBank = (MbMemberBank) this.baseService.getOne(bankItem, MbMemberBank.class);
		//查询用户的实名认证
		QueryItem realnameItem = new QueryItem(Module.MEMBER, Function.MB_REALNAME);
		realnameItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbApproveRealname realname = (MbApproveRealname) this.baseService.getOne(realnameItem, MbApproveRealname.class);
		if (realname == null){
			return errorJsonResonse("尚未实名认证");
		}
		QueryItem isBankItem = new QueryItem(Module.MEMBER, Function.MB_BANK);
		isBankItem.getWhere().add(Where.eq("status", 1));
		isBankItem.getWhere().add(Where.eq("account", bank.getAccount()));
		isBankItem.setFields("id");
		MbMemberBank isBank = (MbMemberBank) this.baseService.getOne(isBankItem, MbMemberBank.class);
		if(isBank != null && isBank.getId() != null){
			return errorJsonResonse("该卡号已被使用");
		}
		if (memberBank == null) {
			if(StringUtils.isNotBlank(payPassword)){
				payPassword = SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+payPassword));
				if(!payPassword.equals(user.getPaypassword()))return errorJsonResonse("支付密码错误");
			}
			MbMemberBank newBank = new MbMemberBank();
			newBank.setMemberId(user.getId());
			newBank.setMemberName(user.getName());
			newBank.setBankNid(bank.getBankNid());
			newBank.setName(bank.getName());
			newBank.setAccount(bank.getAccount());
			newBank.setRealname(realname.getRealname());
			newBank.setProvince(bank.getProvince());
			newBank.setCity(bank.getCity());
			newBank.setAddTime(DateUtil.getCurrentTime());
			newBank.setStatus(1);
			this.baseService.insert(Module.MEMBER, Function.MB_BANK, newBank);
		} 
		
		return successJsonResonse("绑卡成功");
	}
	
	/**
	 * 修改银行卡号
	 * @param bank
	 * @param now_account
	 * @param paypassword
	 * @return
	 * @throws DyServiceException
	 */
	public DyPhoneResponse editBank(MbMemberBank bank,String now_account,String paypassword) throws DyServiceException {
		MbMember user = this.getMbMember(bank.getMemberId());
		//获取已绑定银行卡
		QueryItem bankItem = new QueryItem(Module.MEMBER,Function.MB_BANK);
		bankItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbMemberBank memberBank = (MbMemberBank) this.baseService.getOne(bankItem, MbMemberBank.class);
		//验证
		if(!now_account.equals(memberBank.getAccount()))return errorJsonResonse("当前银行卡号不符");
		paypassword = SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword));
		if(!paypassword.equals(user.getPaypassword()))return errorJsonResonse("支付密码错误");
		
		memberBank.setBankNid(bank.getBankNid());
		memberBank.setName(bank.getName());
		memberBank.setAccount(bank.getAccount());
		memberBank.setProvince(bank.getProvince());
		memberBank.setCity(bank.getCity());
		memberBank.setUpdateTime(DateUtil.getCurrentTime());
		this.baseService.updateById(Module.MEMBER,Function.MB_BANK, memberBank);
		
		return successJsonResonse("修改银行卡号成功");
	}
	
	/**
	 * 检验银行卡号是否被占用
	 * @param account
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getBankByAccount(String account) throws Exception {
		QueryItem bankItem = new QueryItem("member","bank");
		bankItem.getWhere().add(Where.eq("account", account));
		MbMemberBank memberBank = (MbMemberBank) this.baseService.getOne(bankItem, MbMemberBank.class);
		if(memberBank !=null){
			return errorJsonResonse(null);
		}
		return successJsonResonse(null);
	}
}
