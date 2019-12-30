/**
 * 
 */
package com.dy.baf.controller.wap.finance;

import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.service.finance.WithdrawService;
import com.dy.baf.service.member.MemberBankService;
import com.dy.core.utils.RequestUtil;

/**
 * 提现
 */
@Controller(value="wapCashController")
public class CashController extends WapBaseController{
	@Autowired
	private MemberBankService memberBankService ;
	@Autowired
	private WithdrawService withdrawService ;
	/**
	 * 提现
	 * @return
	 */
	@RequestMapping(value="/member/cash",method=RequestMethod.GET)
	public ModelAndView cash(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/cash.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 提现银行卡
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/withdrawCash",method=RequestMethod.POST)
	public DyPhoneResponse withdrawCash(HttpServletRequest request){
		try {
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			String url = this.getWebDomain(request,"wapassets/"+ fileName + "/images/bank/#bank_nid#.gif");
			return memberBankService.index(this.getMemberId().toString(), isTrust, trustType, url);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 提现手续费
	 * @return 
	 */
	@ResponseBody
	@RequestMapping(value="/bank/getCashFee",method=RequestMethod.POST)
	public DyPhoneResponse getCashFee(HttpServletRequest request){
		try {

			String amount = request.getParameter("account");
			
			String[] str = amount.split("\\.");
			if(str.length > 1){
				if (str[1].length() > 2) {
					return errorJsonResonse("请输入整数或保留两位小数");
				}
			}
			BigDecimal account = new BigDecimal(amount);
			return this.withdrawService.getCashFee(account, this.getMemberId().toString(), isTrust);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 提现
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/withdraw",method=RequestMethod.POST)
	public DyPhoneResponse withdraw(HttpServletRequest request){
		try {
			String paypassword = request.getParameter("paypassword"); 
			BigDecimal money = new BigDecimal(request.getParameter("amount")); 
			return this.withdrawService.cashSubmit(money, paypassword, this.getMemberId().toString(), isTrust);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 提现记录
	 * @return
	 */
	@RequestMapping(value="/bank/withdrawLog",method=RequestMethod.GET)
	public ModelAndView withdrawLog(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/cashRecord.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 提现记录查询
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/withdrawLog",method=RequestMethod.POST)
	public DyPhoneResponse withdrawLog(HttpServletRequest request){
		try {
			Integer page = RequestUtil.getInteger(request, "page", 1); 
			Integer status = null; 
			Date start_time = null; 
			Date end_time = null;
			System.out.println(this.withdrawService.withdrawlog(page, status, start_time, end_time, this.getMemberId().toString()));
			return this.withdrawService.withdrawlog(page, status, start_time, end_time, this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
