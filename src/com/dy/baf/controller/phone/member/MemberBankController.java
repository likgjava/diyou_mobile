package com.dy.baf.controller.phone.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.service.member.MemberBankService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.baf.utils.BankInfoUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.trust.chinapnr.ChinapnrCardInfo;
import com.dy.core.utils.StringUtils;

/**
 * 银行卡信息
 * @author Administrator
 *
 */
@Controller(value="appMemberBankController")
public class MemberBankController extends AppBaseController {

	@Autowired
	private MemberBankService memberBankService;
	
	/**
	 * 银行卡信息
	 * @param login_token
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/bank/index")
	public DyPhoneResponse index(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			String url = this.getWebDomain(this.getRequest(),"wapassets/"+ fileName + "/egimages/bank/#bank_nid#.jpg");
			
			MbMember user = this.getMember(Long.valueOf(login_token));
			QueryItem bankItem = new QueryItem();
			bankItem.getWhere().add(Where.eq("member_id", user.getId()));
			bankItem.getWhere().add(Where.eq("status", 1));
			List<MbMemberBank> bankList = this.getListByEntity(bankItem, Module.MEMBER, Function.MB_BANK, MbMemberBank.class);
			Map<String,Object> paramMap = new HashMap<String,Object>();
			for(MbMemberBank mBank : bankList){
				paramMap.put("cardId", mBank.getAccount());
				paramMap.put("memberId", mBank.getMemberId());
				paramMap.put("trustAccount",user.getTrustAccount());
				
				ChinapnrCardInfo cardInfo = (ChinapnrCardInfo) trustManager.queryCardInfo(null, paramMap);
				if("000".equals(cardInfo.getRespCode()) && (cardInfo.getUsrCardInfolist() == null || cardInfo.getUsrCardInfolist().size() <= 0)){
					this.deleteById(mBank.getId(), Module.MEMBER, Function.MB_BANK);
				}
			}
			
			return memberBankService.index(login_token, isTrust, trustType,url);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 获取卡头识别
	 * @param getBankNid
	 * @return
	 */
	@ResponseBody
	@RequestMapping("bank/getBankNid")
	public DyPhoneResponse getBankNid(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String bankcode = paramsMap.get("bankcode"); 
			
			if (StringUtils.isBlank(bankcode)) {
				return errorJsonResonse("请输入银行卡号前六位");
			}
			
			Map<String,String> responseMap = new HashMap<String, String>();
			
			char[] cardNumber = {bankcode.charAt(0),bankcode.charAt(1),bankcode.charAt(2),bankcode.charAt(3),bankcode.charAt(4),bankcode.charAt(5)};
			
			responseMap.put("name", BankInfoUtil.getNameOfBank(cardNumber, 0));
			responseMap.put("nid", BankInfoUtil.getNidOfBank(cardNumber, 0));
			return successJsonResonse(responseMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 银行卡绑定
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/bank/addBank")
	public DyPhoneResponse addBank(String xmdy,String diyou) {
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			
			MbMemberBank memberBank = new MbMemberBank();
			String login_token= paramsMap.get("login_token");
			String account= paramsMap.get("account");
			String bank= paramsMap.get("bank");
			String branch= paramsMap.get("branch");
			String province= paramsMap.get("province");
			String city= paramsMap.get("city");
			
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("登陆标识不能为空");
			}
			if(StringUtils.isBlank(account)){
				return errorJsonResonse("银行卡号不能为空");
			}
			if(StringUtils.isBlank(bank)){
				return errorJsonResonse("银行代码不能为空");
			}
			if(StringUtils.isBlank(province)){
				return errorJsonResonse("开户省份不能为空");
			}
			if(StringUtils.isBlank(city)){
				return errorJsonResonse("登陆标识开户城市不能为空");
			}
			if(StringUtils.isBlank(branch)){
				return errorJsonResonse("开户行不能为空");
			}
			
			memberBank.setAccount(account);
			memberBank.setMemberId(Long.valueOf(login_token));
			memberBank.setBankNid(bank);
			memberBank.setProvince(Long.valueOf(province));
			memberBank.setCity(Long.valueOf(city));
			memberBank.setName(branch);
			return this.memberBankService.addBank(memberBank,null);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	

	/**
	 * 修改银行卡
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/bank/editBank")
	public DyPhoneResponse editBank(String xmdy,String diyou) {
		
		try {
			MbMemberBank memberBank = new MbMemberBank();
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			
			String login_token= paramsMap.get("login_token");
			String account= paramsMap.get("account");
			String bank= paramsMap.get("bank");
			String branch= paramsMap.get("branch");
			String province= paramsMap.get("province");
			String city= paramsMap.get("city");
			String nowAccount = paramsMap.get("now_account");
			String payPassWord =paramsMap.get("paypassword");
			memberBank.setAccount(account);
			memberBank.setMemberId(Long.valueOf(login_token));
			memberBank.setBankNid(bank);
			memberBank.setProvince(Long.valueOf(province));
			memberBank.setCity(Long.valueOf(city));
			return this.memberBankService.editBank(memberBank, nowAccount, payPassWord);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 验证银行卡是否被占用
	 * @param account
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/funds/getBankByAccount")
	public DyPhoneResponse getBankByAccount(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String account = paramsMap.get("account"); 
			return this.memberBankService.getBankByAccount(account);
			
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
}
