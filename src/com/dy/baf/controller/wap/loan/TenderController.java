/**
 * 
 */
package com.dy.baf.controller.wap.loan;

import java.math.BigDecimal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.loan.LoanTenderService;
import com.dy.baf.service.loan.TenderService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;

/**
 * 我的投资
 */
@Controller(value="wapTenderController")
public class TenderController extends WapBaseController{
	@Autowired
	private TenderService tenderService;
	@Autowired
	private LoanTenderService loanTenderService ;
	/**
	 * 我的投资记录
	 * @return
	 */
	@RequestMapping(value="/member/mytender",method=RequestMethod.GET)
	public ModelAndView mytender() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/mytender.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 我的投资数据请求
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/myTenderData",method=RequestMethod.POST)
	public DyPhoneResponse myTenderData(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
		Integer page = RequestUtil.getInteger(request, "page", 1) ;
		return this.tenderService.mytenderlist(member.getId().toString(), page, null, null, null);
//		return accountService.accountLog(member.getId(),page) ;
	}
	
	/**
	 * 我的投资记录
	 * @return
	 */
	@RequestMapping(value="/loan/tenderLoanview",method=RequestMethod.GET)
	public ModelAndView tenderLoanview() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("tender/tenderloan.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我的投资数据请求
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/loan/loansimpleinfo",method=RequestMethod.POST)
	public DyPhoneResponse loanSimpleInfo(HttpServletRequest request) throws Exception{
		String id = request.getParameter("id");
		return this.loanTenderService.investdata(id, Integer.valueOf(this.getMemberId().toString()));
	}
	
	/**
	 * 我的投资数据请求
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/loan/investinterest",method=RequestMethod.POST)
	public DyPhoneResponse investInterest(HttpServletRequest request) throws Exception{
		BigDecimal amount = new BigDecimal(request.getParameter("amount"));
		Integer period = Integer.valueOf(request.getParameter("period"));
		BigDecimal apr = new BigDecimal(request.getParameter("apr"));
		Integer repay_type = Integer.valueOf(request.getParameter("repay_type"));
		Double award_scale = StringUtils.isNotBlank(request.getParameter("award_scale")) ? Double.valueOf(request.getParameter("award_scale")) : null;
		Integer additional_status = RequestUtil.getInteger(request, "additional_status", -1) ; 
		BigDecimal additional_apr = new BigDecimal(RequestUtil.getString(request, "additional_apr", "0"));
		String loanId = request.getParameter("loan_id");
		return this.loanTenderService.investInterest(amount, period, apr, repay_type, award_scale,additional_status,additional_apr, this.getMemberId().toString(), loanId);
	}
	
	/**
	 * 立即投资
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/loan/invest",method=RequestMethod.POST)
	public DyPhoneResponse invest(HttpServletRequest request) throws Exception{
		Integer memberId = Integer.valueOf(this.getMemberId().toString()) ;
		Integer id = RequestUtil.getInteger(request, "id", null) ;
		BigDecimal amount = new BigDecimal(RequestUtil.getString(request, "amount", "0")) ;
		String paypassword = request.getParameter("paypassword") ;
		String password = request.getParameter("loan_password") ;
		String depositCertificate = request.getParameter("depositCertificate") ;
		String redId = request.getParameter("redbag");
		return this.loanTenderService.invest(memberId, id, amount, paypassword, password, depositCertificate, redId);
	}
	
	/**
	 * 投资流转标
	 * @param id
	 * @param number
	 * @param paypassword
	 * @param password
	 * @param depositCertificate
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/loan/investRoam", method=RequestMethod.POST)
	public DyPhoneResponse investRoam(Integer id,Integer number,String paypassword,String password,String depositCertificate, String redbag) {
		try {
			Long memberId = this.getMemberId() ;

			
			return this.loanTenderService.investRoam(this.isTrust, memberId, id, number, paypassword, password, depositCertificate, redbag);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 查看电子协议
	 */
	@RequestMapping("/loan/loanAgreement")
	public ModelAndView showProtocol(Long id,Model model){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/loan/loanAgreement.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 查看电子协议（app）
	 */
	@RequestMapping("/loan/appLoanAgreement")
	public ModelAndView appLoanAgreement(Long id,Model model){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/loan/appLoanAgreement.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 我的投资详情
	 */
	@RequestMapping(value="/loan/myloaninfo", method=RequestMethod.GET)
	public ModelAndView myLoanInfo(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/loan/myloanInfo.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我的投资详情数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/tender/tenderinfodata", method=RequestMethod.POST)
	public DyPhoneResponse tenderInfoData(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", 0L) ;
		try {
			Map<String,Object> loanInfo = this.tenderService.tenderinfodata(id) ;
			return this.successJsonResonse(loanInfo) ;
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 电子合同获取数据
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/loan/getAgreeInfo",method=RequestMethod.POST)
	public DyPhoneResponse getAgreeInfo(Long id) {
		try {
			return this.tenderService.getAgreeInfo(id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
