package com.dy.baf.controller.phone.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnAccountWithdraw;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemLinkage;
import com.dy.baf.service.finance.WithdrawService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.utils.Constant;
import com.dy.core.utils.FeeUtil;

/**
 * 提现
 * @author Administrator
 *
 */
@Controller(value="appWithdrawController")
public class WithdrawController extends AppBaseController {
	
	@Autowired
	private WithdrawService withdrawService;
	@Autowired
	private FeeUtil feeUtil;
	
	/**
	 * 计算提现费用
	 */
	@ResponseBody
	@RequestMapping("/member/getCashFee")
	public DyPhoneResponse getCashFee(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			BigDecimal account = new BigDecimal(paramsMap.get("account")); 
			return this.withdrawService.getCashFee(account, login_token, isTrust);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 提现提交
	 */
	@ResponseBody
	@RequestMapping("/member/withDraw")
	public DyPhoneResponse cashSubmit(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			String paypassword = paramsMap.get("paypassword"); 
			BigDecimal amount = new BigDecimal(paramsMap.get("amount")); 
			return this.withdrawService.cashSubmit(amount, paypassword, login_token, isTrust);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 提现记录列表
	 */
	@ResponseBody
	@RequestMapping("/member/withDrawLog")
	public DyPhoneResponse withdrawlog(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token"); 
			Integer page = Integer.valueOf(paramsMap.get("page")); 
			
			return this.withdrawService.withdrawlog(page, null, null, null, login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
	/**
	 * 提现资金总额
	 */
	@ResponseBody
	@RequestMapping("/withdraw/withdrawCount")
	public Map<String, Object> withdrawCount() throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		//提现记录
		QueryItem cashItem = new QueryItem("finance","withdraw");
		cashItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<FnAccountWithdraw> cashList = (List<FnAccountWithdraw>) this.getList(cashItem, FnAccountWithdraw.class);
		BigDecimal cashAccount = new BigDecimal(0);
		for(FnAccountWithdraw withdraw:cashList){
			cashAccount = cashAccount.add(withdraw.getAmount());
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("income_total", cashAccount);
		return map;
	}
	
	
	/**
	 * 提现费用工具
	 * @throws Exception 
	 */
	public DyResponse getFee(BigDecimal account) throws Exception{
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		return feeUtil.getWithdrawFee(account, user.getId(), isTrust);
	}
	
	/**
	 * 根据bankid获取银行名称
	 * @throws Exception 
	 */
	private String getBankName(String id) throws Exception{
		QueryItem bankItem = new QueryItem();
		bankItem.getWhere().add(Where.eq("id", id));
		SysSystemLinkage linkage = this.getOneByEntity(bankItem, Module.SYSTEM, Function.SYS_LINKAGE, SysSystemLinkage.class);
		return linkage == null ? null : linkage.getName();
	}
	
}
