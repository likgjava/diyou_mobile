package com.dy.baf.controller.wap.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.FnTenderTransfer;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.loan.BuyTransferService;
import com.dy.baf.service.member.ApproveRealnameService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.FinanceService;

@Controller(value="wapBuyTransferController")
public class BuyTransferController extends WapBaseController{
	@Autowired
	private FinanceService financeService;

	@Autowired
	private BuyTransferService buyTransferService;
	
	@Autowired
	private ApproveRealnameService approveRealnameService ;
	/**
	 * 债权转让列表页面
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/transfer/transfer")
	public ModelAndView articleIndex(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/public/transfer.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 债 权转让列表 
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/transferList")
	public Page buyTransferList(String account_status,String borrow_interestrate,String order,Integer page) {
		try {
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			String url = this.getWebDomain(request,"/wap/transfer/transferView#?id=");
			return this.buyTransferService.buyTransferList(account_status, borrow_interestrate, order, page,url);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}
	
	/**
	 * 债权转让详情页面
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/transferView",method=RequestMethod.GET)
	public ModelAndView transferView(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/transferView.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 债权转让购买页
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/transfer/transferinvest",method=RequestMethod.GET)
	public ModelAndView transferinvest(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/transferInvest.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 购买信息数据
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/transferinvest",method=RequestMethod.POST)
	public DyPhoneResponse getTransferBuyData(HttpServletRequest request) throws Exception{
		
		String login_token = this.getMemberId().toString();
		String transferId = request.getParameter("id");
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		Map<String,Object> map = this.buyTransferService.getTransferBuyData(transferId,login_token);
		DyPhoneResponse response= approveRealnameService.isApprove(login_token);
		map.put("description", response.getData());
		return successJsonResonse(map);
	}
	
	/**
	 * 债权转让详情页面（app）
	 * @param request
	 * @return
	 */
	@RequestMapping("/transfer/appTransferView")
	public ModelAndView appTransferView(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/appTransferView.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	/**
	 * 购买信息数据
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/buydata")
	public DyResponse getTransferBuyData(Long id) throws Exception{
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());
		//债权表
		FnTenderTransfer transfer = getTransfer(id);
		//资金表
		FnAccount account = getAccount(user.getId());
		//转让手续费
		BigDecimal transferFee = NumberUtils.mul(transfer.getAmount(), transfer.getTransferFee());
		//最近还款时间
		String lastRepayTime = getLastRepayTime(transfer.getTenderId());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("transfer", transfer);
		map.put("account", account);
		map.put("transferFee", transferFee);
		map.put("user", user);
		map.put("lastRepayTime", lastRepayTime);
		return createSuccessJsonResonse(map);
	}
	
	/**
	 * 开始购买
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/buySubmit")
	public DyResponse buySubmit(Long id,Long loan_id,String paypassword) throws Exception{
		//验证支付密码是否为空
		if("".equals(paypassword))return createErrorJsonResonse("支付密码不能为空");
		//当前债权
		FnTenderTransfer transfer = getTransfer(id);
		//判断当前债权是否转让中
		if(transfer.getStatus() != 1)return createErrorJsonResonse("该债券无法购买");
		//验证购买人的认证信息
		MbMember user =(MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());
		if(user.getIsRealname() !=1)return createErrorJsonResonse("尚未实名认证");
		if(user.getIsPhone() !=1)return createErrorJsonResonse("尚未绑定手机");
		if("".equals(user.getPaypassword()))return createErrorJsonResonse("支付密码不能为空");
		//判断可用余额
		FnAccount account = getAccount(user.getId());
		if(account.getBalanceAmount().compareTo(transfer.getAmount())<0)return createErrorJsonResonse("可用余额不足");
		//判断支付密码是否正确
		String raw = SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword));
		if(!raw.equals(user.getPaypassword()))return createErrorJsonResonse("支付密码不正确");
		//判断债权的更新时间是否是今天,否则重新计算债权价值
		Date now = new Date();
		int days = DateUtil.daysBetween(DateUtil.dateParse(transfer.getUpdateTime()),now);
		if(days >= 1 ){
			FnTender tender = getTender(transfer.getTenderId());
			BigDecimal amountMoney = getAmoutMoney(tender);
			transfer.setAmountMoney(amountMoney);
			Double percent = Double.valueOf(transfer.getCoefficient())/100;
			BigDecimal amount = amountMoney.multiply(new BigDecimal(percent)).subtract(amountMoney.multiply(transfer.getTransferFee()));
			transfer.setAmount(amount);
			transfer.setUpdateTime(DateUtil.getCurrentTime());
			this.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
		}
		financeService.buyTransfer(id,user.getId(),IpUtil.ipStrToLong(this.getRemoteIp()));
		return createSuccessJsonResonse(null, "购买成功");
	}
	
	
	
	
	
	/**
	 * 查看转让协议协议
	 */
	@RequestMapping(value="/transfer/transferAgreement",method=RequestMethod.GET)
	public ModelAndView showProtocol(){
		ModelAndView view = new ModelAndView();
		try {
			
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/transferAgreement.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 查看转让协议协议(app)
	 */
	@RequestMapping(value="/transfer/appTransferAgreement",method=RequestMethod.GET)
	public ModelAndView transferAgreement(){
		ModelAndView view = new ModelAndView();
		try {
			
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/appTransferAgreement.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 转让协议获取数据
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/transferAgreement",method=RequestMethod.POST)
	public DyPhoneResponse getAgreeInfo(Long id) throws Exception{
		//查询债权转让协议书
		QueryItem agreeItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("type", "transfer"));
		agreeItem.setWhere(whereList);
		CtAgreement agreement = this.getOneByEntity(agreeItem, Module.CONTENT, Function.CT_AGREEMENT, CtAgreement.class);
		Map<String,Object> map = new HashMap<String,Object>();
		//获取债权
		FnTenderTransfer transfer = getTransfer(id);
		String nid="zqzr"+DateUtil.getCurrentDateStr();
		String oid;
		if(transfer.getId()<10){
			oid="00"+transfer.getId();
		}else if(transfer.getId()<100){
			oid="0"+transfer.getId();
		}else 
			oid=transfer.getId().toString();
		transfer.setInd(nid+oid);
		
		BigDecimal totalPriInt = getWaitPriInt(transfer.getPeriod(),transfer.getLoanId(),transfer.getTenderId());
		transfer.setAmountMoney(totalPriInt);
		
		
		map.put("agreement", agreement);
		map.put("transfer_info", transfer);
		
		QueryItem loanItem = new QueryItem();
		loanItem.setWhere(new Where("id",transfer.getLoanId()));
		loanItem.setFields("repay_type,name");
		Map loanMap = this.getOneByMap(loanItem, Module.LOAN, Function.LN_LOAN);
		loanMap = (Map) dataConvert(loanMap,"repay_type:getRepayType");
		map.put("loan", loanMap);
		return successJsonResonse(map);
	}
	
	
	
	
	/**
	 * 根据id获取借款标
	 * @throws Exception 
	 * 
	 */
	private FnLoan getLoan(Long loanId) throws Exception{
		QueryItem loanItem  = new QueryItem();
		loanItem.getWhere().add(Where.eq("id", loanId));
		FnLoan loan = this.getOneByEntity(loanItem, "loan", "loan", FnLoan.class);
		return loan;
	}
	
	/**
	 * 根据id获取投标
	 * @throws Exception 
	 * 
	 */
	private FnTender getTender(Long tenderId) throws Exception{
		QueryItem tenderItem  = new QueryItem();
		tenderItem.getWhere().add(Where.eq("id", tenderId));
		FnTender tender = this.getOneByEntity(tenderItem, "loan", "tender", FnTender.class);
		return tender;
	}
	
	/**
	 * 根据id获取债权表
	 * @throws Exception 
	 */
	private FnTenderTransfer getTransfer(Long id) throws Exception{
		QueryItem transferItem = new QueryItem();
		transferItem.getWhere().add(Where.eq("id", id));
		FnTenderTransfer transfer = this.getOneByEntity(transferItem,Module.LOAN, Function.LN_TRANSFER, FnTenderTransfer.class);
		return transfer;
	}
	
	/**
	 * 根据tenderid获取上期还息日
	 * @throws Exception 
	 */
	private String getLastRepayTime(Long tenderId) throws Exception{
		QueryItem tenderItem = new QueryItem();
		tenderItem.getWhere().add(Where.eq("id", tenderId));
		FnTender tender = this.getOneByEntity(tenderItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
		if(tender.getRecoverCountYes() == 0 || tender.getRecoverCountYes() == null){
			return "未还款";
		}
		QueryItem item = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tender.getId()));
		whereList.add(Where.eq("period_no", tender.getRecoverCountYes()));
		item.setWhere(whereList);
		item.setLimit(1);
		List<FnTenderRecover> recover = this.getListByEntity(item,Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		String lastRepayTime = "";
		if(recover != null && recover.size() > 0){
			lastRepayTime = DateUtil.dateFormat(recover.get(0).getRecoverTime());
		}
		return lastRepayTime;
	}
	
	/**
	 * 根据memberid获取账户资金
	 * @throws Exception 
	 */
	private FnAccount getAccount(Long memberId) throws Exception{
		QueryItem accountItem = new QueryItem();
		accountItem.getWhere().add(Where.eq("member_id", memberId));
		FnAccount account = this.getOneByEntity(accountItem, Module.FINANCE, Function.FN_ACCOUNT, FnAccount.class);
		return account;
	}
	
	/**
	 * 计算债权价值
	 * @throws Exception 
	 */
	private BigDecimal getAmoutMoney(FnTender tender) throws Exception{
		Date success_time = DateUtil.dateParse(tender.getSuccessTime());//起息日
		Date current_time = new Date();//当日
		int months = DateUtil.monthBetween(success_time, current_time);
		Date date = DateUtil.addMonth(success_time, months);//当期应还时间
		FnTenderRecover recover = null;
		if(current_time.getTime() <= date.getTime()){//当前时间小于当期应还时间
			 recover = getRecover(tender.getId(),months);
		}else{
			 recover = getRecover(tender.getId(),months + 1);
		}
		BigDecimal waitPrincipal = tender.getRecoverPrincipal().subtract(tender.getRecoverPrincipalYes());//待收本金
		//BigDecimal waitInterest = tender.getRecoverInterest().subtract(tender.getRecoverInterestYes());//待收利息
		BigDecimal waitInterest = recover.getInterest();//当期利息
		BigDecimal amountMoney = BigDecimal.ZERO;
		Date lastRepayTime = null;//上期应还款时间
		if(months == 0){//如果是第一期，则上期应还款时间为满标复审时间
			lastRepayTime = success_time;
		}else{
			FnTenderRecover lastRecover = getRecover(tender.getId(),months -1);//上期还款
			lastRepayTime = DateUtil.dateParse(lastRecover.getRecoverTime());
		}
		if(recover.getStatus() == -1){//当期未还时
			//成交日期 - 上期还款对应的应还款日期
			int days = DateUtil.daysBetween(lastRepayTime,current_time);
			amountMoney = waitPrincipal.add(waitInterest.multiply(new BigDecimal(days)).divide(new BigDecimal(30),BigDecimal.ROUND_HALF_EVEN));
		}else{
			//判断下一期是否已还
			FnTenderRecover nextRecover = getRecover(tender.getId(), recover.getPeriodNo() + 1);
			//成交时所在的期的应还日期
			Date localTime = DateUtil.dateParse(recover.getRecoverTime());
			//成交时所在的期的应还日期 - 成交日期
			int days = DateUtil.daysBetween(localTime, current_time);
			if(nextRecover.getStatus() == -1){//当期已还时,后面的未还
				amountMoney = waitPrincipal.add(waitInterest.multiply(new BigDecimal(days)).divide(new BigDecimal(30),BigDecimal.ROUND_HALF_EVEN));
			}else{//当期及之后的N期已还，但还未还清
				amountMoney = waitPrincipal.add(waitInterest.multiply(new BigDecimal(days)).divide(new BigDecimal(30),BigDecimal.ROUND_HALF_EVEN));
				BigDecimal totalInterest = getTotalInterest(tender.getId(),recover.getPeriodNo());//之后已还利息总和
				if(totalInterest != null){
					amountMoney = amountMoney.subtract(totalInterest);
				}
			}
		}
		return amountMoney;
	}
	
	/**
	 * 根据当前期数和tenderid获取还款表
	 * @throws Exception 
	 */
	private FnTenderRecover getRecover(Long tenderId,int periodNo) throws Exception{
		QueryItem item = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("period_no", periodNo));
		item.setWhere(whereList);
		FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover;
	}
	
	/**
	 * 根据tenderid和当前期数获取之后已还款的还款表利息总和
	 */
	private BigDecimal getTotalInterest(Long tenderId,int periodNo) throws Exception{
		QueryItem item = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(new Where("period_no", periodNo,">"));
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("status", 1));
		item.setFields("sum(interest_yes) interest_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover == null?null:recover.getInterestYes();
	}
	/**
	 * 计算购买时的总待收本息
	 * @throws Exception 
	 */
	private BigDecimal getWaitPriInt(Integer period,Long loanId,Long tenderId) throws Exception{
		QueryItem  item = new QueryItem();
		item.getWhere().add(Where.eq("loan_id", loanId));
		item.getWhere().add(Where.eq("tender_id", tenderId));
		item.getWhere().add(new Where("period_no",period,">="));
		item.setFields("sum(principal + interest) amount");
		FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover == null ? BigDecimal.ZERO : recover.getAmount();
	}
}
