package com.dy.baf.controller.wap.finance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.service.member.ApproveRealnameService;
import com.dy.baf.service.member.MemberBankService;
import com.dy.core.trust.entity.Bank;
import com.dy.core.trust.entity.BankMap;
import com.dy.core.utils.Constant;
import com.dy.core.utils.StringUtils;
/**
 * 银行卡信息
 */
@Controller(value="wapBankController")
public class BankController extends WapBaseController{
	@Autowired
	private MemberBankService memberBankService ;
	@Autowired
	private ApproveRealnameService approveRealnameService ;
	/**
	 * 银行卡信息
	 * @return
	 */
	@RequestMapping(value="/bank/index",method=RequestMethod.GET)
	public ModelAndView bankIndex(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("bank/bank.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 银行卡信息页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/index",method=RequestMethod.POST)
	public DyPhoneResponse bankIndex(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			String url = this.getWebDomain(request,"wapassets/"+ fileName + "/images/bank/#bank_nid#.gif");
			return memberBankService.index(member.getId().toString(), isTrust, trustType, url);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 添加银行卡
	 * @return
	 */
	@RequestMapping(value="/bank/addbank",method=RequestMethod.GET)
	public ModelAndView addbank(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("bank/bankAdd.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 添加银行卡页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/addBank",method=RequestMethod.POST)
	public DyPhoneResponse addBank(HttpServletRequest request){
		try {
			MbApproveRealname realname = approveRealnameService.getRealname(this.getMemberId()) ;
			Map<String,Object> response = new HashMap<String, Object>() ;
			response.put("realname", realname.getRealname()) ;
			return successJsonResonse(response);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 添加银行卡提交
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/addBankSubmit",method=RequestMethod.POST)
	public DyPhoneResponse addBankSubmit(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String memberId= member.getId().toString();
			String account= request.getParameter("account");
			String bank = request.getParameter("bank");
			String branch= request.getParameter("branch");
			String province= request.getParameter("province");
			String city= request.getParameter("city");
			String payPassword = request.getParameter("paypassword");
			
			MbMemberBank memberBank = new MbMemberBank();
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
			if(StringUtils.isBlank(payPassword)){
				return errorJsonResonse("支付密码不能为空");
			}
			DyPhoneResponse response = this.memberBankService.getBankByAccount(account) ;
			if(DyPhoneResponse.NO == response.getCode()){
				return errorJsonResonse("银行卡已存在") ;
			}
			memberBank.setAccount(account);
			memberBank.setMemberId(Long.valueOf(memberId));
			memberBank.setBankNid(bank);
			memberBank.setProvince(Long.valueOf(province));
			memberBank.setCity(Long.valueOf(city));
			memberBank.setName(branch);
			
			return this.memberBankService.addBank(memberBank,payPassword);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 编辑银行卡信息
	 * @return
	 */
	@RequestMapping(value="/bank/editBank",method=RequestMethod.GET)
	public ModelAndView editBank(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("bank/bankChange.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 编辑银行卡信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/editBank",method=RequestMethod.POST)
	public DyPhoneResponse editBank(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			String url = this.getWebDomain(request,"wapassets/"+ fileName + "/images/bank/#bank_nid#.gif");
			return memberBankService.index(member.getId().toString(), isTrust, trustType,url);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 修改银行卡提交
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/editBankSubmit",method=RequestMethod.POST)
	public DyPhoneResponse editBankSubmit(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String account= request.getParameter("account");
			account = org.apache.commons.lang.StringUtils.replace(account, " ", "") ;
			String bank= request.getParameter("bank");
			String branch= request.getParameter("branch");
			String province= request.getParameter("province");
			String city= request.getParameter("city");
			String nowAccount = request.getParameter("now_account");
			nowAccount = org.apache.commons.lang.StringUtils.replace(nowAccount, " ", "") ;
			String payPassWord = request.getParameter("paypassword");
			MbMemberBank memberBank = new MbMemberBank();
			memberBank.setAccount(account);
			memberBank.setMemberId(member.getId());
			memberBank.setBankNid(bank);
			memberBank.setProvince(Long.valueOf(province));
			memberBank.setCity(Long.valueOf(city));
			memberBank.setName(branch) ;
			DyPhoneResponse response = this.memberBankService.getBankByAccount(account) ;
			if(DyPhoneResponse.NO == response.getCode()){
				return errorJsonResonse("银行卡已存在") ;
			}
			return this.memberBankService.editBank(memberBank, nowAccount, payPassWord);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 银行下拉列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/bank/allBank",method=RequestMethod.POST)
	public DyPhoneResponse allBank(HttpServletRequest request){
		try {
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			List<Map> listMap = new ArrayList<Map>();;
			List<Bank> bankList = BankMap.getAllBank();
			for (Bank bank : bankList) {
				Map<String,String> map = new HashMap<String, String>();
				map.put("code", bank.getBankNid());
				map.put("name", bank.getBankName());
				map.put("url", this.getWebDomain(request,"wapassets/"+ fileName + "/images/bank/" + bank.getBankNid() + ".gif")) ;
				listMap.add(map);
			}
			return successJsonResonse(listMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
}
