package com.dy.baf.service.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanRepayType;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.FnTenderTransfer;
import com.dy.baf.entity.common.MbGuaranteeCompany;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.custom.Tender;
import com.dy.baf.entity.custom.TenderCondition;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.FinanceService;

/**
 * 
 * 
 * @Description: 债权转让列表
 * @author 波哥
 * @date 2015年9月12日 下午3:32:09
 * @version V1.0
 */
@Service("mobileBuyTransferService")
public class BuyTransferService extends MobileService {
	@Autowired
	private BaseService baseService;
	@Autowired
	private FinanceService financeService;

	/**
	 * 债权转让列表
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Page buyTransferList(String account_status, String borrow_interestrate, String order, Integer page,String url) throws Exception {
		QueryItem transferItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("status", -1, "!="));
		transferItem.setOrders("status asc,add_time desc");
		if (order != null && order != "") {
			if ("account_up".equals(order)) {
				transferItem.setOrders("amount asc");
			} else if ("account_down".equals(order)) {
				transferItem.setOrders("amount desc");
			} else if ("apr_up".equals(order)) {
				transferItem.setOrders("apr asc");
			} else if ("apr_down".equals(order)) {
				transferItem.setOrders("apr desc");
			}
		}
		transferItem.setPage(page == null ? 1 : page);
		transferItem.setLimit(10);
		transferItem.setWhere(whereList);
 		Page pageObj = (Page) this.baseService.getPage(transferItem, FnTenderTransfer.class);
		List<FnTenderTransfer> transferList = pageObj.getItems();
		List<Map<String, Object>> tenderTransferList = new ArrayList<Map<String, Object>>();
		Date now = new Date();
		
		
		//查询标类型
		QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);//借款标类型
		loanCategoryItem.setFields("id,is_roam,pic");
		List<Map> cateList = (List<Map>) this.baseService.getList(loanCategoryItem);
		
		for(FnTenderTransfer transfer:transferList){
			//判断债权的更新时间是否是今天,否则重新计算债权价值
			FnTender tender = getTender(transfer.getTenderId());
			if(transfer.getStatus() == 1){
				int days = DateUtil.daysBetween(DateUtil.dateParse(transfer.getUpdateTime()),now);
				if(days >= 1 ){
					BigDecimal amountMoney = getAmoutMoney(tender);
					transfer.setAmountMoney(amountMoney);
					Double percent = Double.valueOf(transfer.getCoefficient())/100;
					BigDecimal amount = amountMoney.multiply(new BigDecimal(percent));
					transfer.setAmount(amount);
					transfer.setUpdateTime(DateUtil.getCurrentTime());
					this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
				}
			}
			Map<String, Object> map = new HashMap<String, Object>();
			Long loan_id = transfer.getLoanId();
			FnLoan loan = getLoan(loan_id);
			Long category_id = loan.getCategoryId();
			String loan_name = transfer.getLoanName();
			String member_name = transfer.getMemberName();
			Integer total_period = transfer.getTotalPeriod();
			Integer period = transfer.getPeriod();
			if(transfer.getStatus() == -1 || transfer.getStatus() == 1){
				period = tender.getRecoverCount() - tender.getRecoverCountYes();
			}else{
				period = transfer.getTotalPeriod() - transfer.getPeriod() +1;
			}
			
			BigDecimal apr = transfer.getApr();
			String debt_member_name = loan.getMemberName();
			BigDecimal wait_principal = transfer.getWaitPrincipal();
			BigDecimal wait_interest = transfer.getWaitInterest();
			BigDecimal amount = transfer.getAmount();
			Integer status = transfer.getStatus();
			String last_repay_time = DateUtil.dateFormat(tender.getExpireTime());
			Long id = transfer.getId();
			
			if(loan.getVouchCompanyId() != null && loan.getVouchCompanyId() != 0){
				MbGuaranteeCompany bondingCompany = null;
				QueryItem companyItem = new QueryItem(Module.MEMBER, Function.MB_VOUCHCOMPANY);
				companyItem.getWhere().add(Where.eq("id", String.valueOf(loan.getVouchCompanyId())));
				bondingCompany = this.baseService.getOne(companyItem,  MbGuaranteeCompany.class);
				bondingCompany.setCompanyLogo(PropertiesUtil.getImageHost() + bondingCompany.getCompanyLogo());
				map.put("vouch_company_name", bondingCompany.getName());
			}else{
				map.put("vouch_company_name", "南昌金融投资有限公司");
			}
			
			for (Map cate : cateList) {
				if(cate.get("id") !=null && loan.getCategoryId()!=null && cate.get("id").toString().equals(loan.getCategoryId().toString())){
					if(cate.get("pic") != null && StringUtils.isNotBlank(String.valueOf(cate.get("pic")))){
						map.put("pic", PropertiesUtil.getImageHost() + cate.get("pic"));
					}
					break;
				}
			}

			map.put("category_id", category_id);
			map.put("loan_id", loan_id);
			map.put("loan_name", loan_name);
			map.put("member_name", member_name);
			map.put("total_period", total_period);
			map.put("period", period);
			map.put("apr", apr);
			map.put("debt_member_name", debt_member_name);
			map.put("wait_principal", wait_principal);
			map.put("wait_interest", wait_interest);
			map.put("amount", amount);
			map.put("status", status);
			map.put("last_repay_time", last_repay_time);
			map.put("id", id);
			map.put("amount_money", transfer.getAmountMoney().setScale(2,BigDecimal.ROUND_HALF_UP));
			
			map.put("transfer_info_url", url+transfer.getId());
			
			//分享信息
			map.put("share_url", map.get("transfer_info_url"));
			map.put("share_content", map.get("loan_name"));
			map.put("share_title", map.get("loan_name"));
			tenderTransferList.add(map);
		}
		for (Iterator<FnTenderTransfer> iter = transferList.iterator(); iter.hasNext();) {
			FnTenderTransfer transfer = iter.next();
			FnTender tender = getTender(transfer.getTenderId());
			 //判断该标是否逾期未还
			 FnTenderRecover recover = getRecover(transfer.getTenderId(), tender.getRecoverCountYes() + 1);
			if(transfer.getStatus() == 1 && recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
				tender.setTransferStatus(-3);
				this.baseService.updateById(Module.LOAN, Function.LN_TENDER, tender);
				transfer.setStatus(-1);
				transfer.setCancelTime(DateUtil.getCurrentTime());
				transfer.setCancelRemark("逾期还款债权撤销");
				this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
				iter.remove();
			}
		}
		pageObj.setItems(tenderTransferList);
		return pageObj;
	}

	/**
	 * 购买信息数据
	 * 
	 * @throws Exception
	 */
	public Map<String,Object> getTransferBuyData(String id, String memberId) throws Exception {
		MbMember user = getMbMember(Long.valueOf(memberId));
		// 债权表
		FnTenderTransfer transfer = getTransfer(Long.valueOf(id));
		// 资金表
		FnAccount account = getAccount(user.getId());
		// 转让手续费
		BigDecimal transferFee = NumberUtils.mul(transfer.getAmount(), transfer.getTransferFee());
		//还款方式
		String repayType = getRepayTypeByLoanId(transfer.getLoanId());
		//借款期限
		FnLoan loan = getLoan(transfer.getLoanId());
		Integer loanPeriod = loan.getPeriod();
		// 最近还款时间
		String lastRepayTime = getRecoverTime(transfer.getTenderId(),transfer.getPeriod());
		//转让期数
		Integer period = transfer.getTotalPeriod() - transfer.getPeriod() + 1;
		//预期收益
		BigDecimal income = transfer.getWaitPrincipal().add(transfer.getWaitInterest()).subtract(transfer.getAmount());
		/*BigDecimal income = investInterest(transfer.getAmountMoney(),period,loan.getApr(),loan.getRepayType(),null);
		income = transfer.getAmountMoney().subtract(transfer.getAmount()).add(income);*/
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("loan_name", transfer.getLoanName());
		map.put("period", period);
		map.put("total_period", transfer.getTotalPeriod());
		map.put("amount", NumberUtils.format(transfer.getAmount(),NumberUtils.FORMAT));
		map.put("transfer_fee", transferFee);
		map.put("repay_type", repayType);
		map.put("loan_period", loanPeriod+"个月");//天标无法债权转让，
		map.put("recover_time", DateUtil.dateFormat(Long.valueOf(lastRepayTime)));
		map.put("next_repay_time",  DateUtil.dateFormat(Long.valueOf(lastRepayTime)));
		map.put("balance_amount", account.getBalanceAmount());
		map.put("income", income);
		return map;
	}

	/**
	 * 开始购买
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse buySubmit(String transferId, String loanId, String paypassword, String memberId, String ip)
			throws Exception {
		if (StringUtils.isBlank(memberId))return errorJsonResonse("用户未登录");
		// 验证支付密码是否为空
		if ("".equals(paypassword))
			return errorJsonResonse("支付密码不能为空");
		// 当前债权
		FnTenderTransfer transfer = getTransfer(Long.valueOf(transferId));
		// 判断当前债权是否转让中
		if (transfer.getStatus() != 1)
			return errorJsonResonse("该债券无法购买");
		// 验证购买人的认证信息
		MbMember user = getMbMember(Long.valueOf(memberId));
		if (user.getIsRealname() != 1)
			return errorJsonResonse("尚未实名认证");
		if (user.getIsPhone() != 1)
			return errorJsonResonse("尚未绑定手机");
		if ("".equals(user.getPaypassword()))
			return errorJsonResonse("尚未设置支付密码");
		// 判断可用余额
		FnAccount account = getAccount(user.getId());
		if (account.getBalanceAmount().compareTo(transfer.getAmount()) < 0)
			return errorJsonResonse("可用余额不足");
		// 判断支付密码是否正确
		String raw = SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach() + paypassword));
		if (!raw.equals(user.getPaypassword()))
			return errorJsonResonse("支付密码不正确");
		// 判断债权的更新时间是否是今天,否则重新计算债权价值
		Date now = new Date();
		int days = DateUtil.daysBetween(DateUtil.dateParse(transfer.getUpdateTime()), now);
		if (days >= 1) {
			FnTender tender = getTender(transfer.getTenderId());
			BigDecimal amountMoney = getAmoutMoney(tender);
			transfer.setAmountMoney(amountMoney);
			Double percent = Double.valueOf(transfer.getCoefficient()) / 100;
			BigDecimal amount = amountMoney.multiply(new BigDecimal(percent))
					.subtract(amountMoney.multiply(transfer.getTransferFee()));
			transfer.setAmount(amount);
			transfer.setUpdateTime(DateUtil.getCurrentTime());
			this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
		}
		financeService.buyTransfer(Long.valueOf(transferId), user.getId(), IpUtil.ipStrToLong(ip));
		return successJsonResonse("购买成功");
	}

	/**
	 * 是否是自己的债权转让
	 * @param login_token
	 * @param transfer_id
	 * @return
	 * @throws DyServiceException
	 */
	public DyPhoneResponse isMyTransfer(String login_token, String transfer_id) throws DyServiceException{
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.expression("( member_id = "+ login_token +" or loan_member_id = "+login_token+")", false));
		whereList.add(Where.eq("id", transfer_id));
		queryItem.setWhere(whereList);
		queryItem.setFields("id,member_id,loan_member_id");
		List<Map> list = this.baseService.getList(queryItem,Map.class);
		Map<String, String> responseMap = new HashMap<String, String>();
		if(list != null && list.size() > 0){
			responseMap.put("is_myloan", "1");
			if(login_token.equals(list.get(0).get("member_id").toString())){
				responseMap.put("tip_message", "不能购买自己的债权");
			}else{
				responseMap.put("tip_message", "不能购买自己的债务");
			}
		}else{
			responseMap.put("is_myloan", "-1");
		}
		return successJsonResonse(responseMap);
	}
	
	/**
	 * 根据id获取借款标
	 * 
	 * @throws Exception
	 * 
	 */
	private FnLoan getLoan(Long loanId) throws Exception {
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		loanItem.getWhere().add(Where.eq("id", loanId));
		FnLoan loan = this.baseService.getOne(loanItem, FnLoan.class);
		return loan;
	}

	/**
	 * 根据id获取投标
	 * 
	 * @throws Exception
	 * 
	 */
	private FnTender getTender(Long tenderId) throws Exception {
		QueryItem tenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		tenderItem.getWhere().add(Where.eq("id", tenderId));
		FnTender tender = this.baseService.getOne(tenderItem, FnTender.class);
		return tender;
	}

	/**
	 * 根据id获取债权表
	 * 
	 * @throws Exception
	 */
	private FnTenderTransfer getTransfer(Long id) throws Exception {
		QueryItem transferItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		transferItem.getWhere().add(Where.eq("id", id));
		FnTenderTransfer transfer = this.baseService.getOne(transferItem, FnTenderTransfer.class);
		return transfer;
	}

	/**
	 * 根据tenderid获取上期还息日
	 * 
	 * @throws Exception
	 */
	private String getLastRepayTime(Long tenderId) throws Exception {
		QueryItem tenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		tenderItem.getWhere().add(Where.eq("id", tenderId));
		FnTender tender = this.baseService.getOne(tenderItem, FnTender.class);
		if (tender.getRecoverCountYes() == 0 || tender.getRecoverCountYes() == null) {
			return "未还款";
		}
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tender.getId()));
		whereList.add(Where.eq("period_no", tender.getRecoverCountYes()));
		item.setWhere(whereList);
		item.setLimit(1);
		List<FnTenderRecover> recover = this.baseService.getList(item, FnTenderRecover.class);
		String lastRepayTime = "";
		if (recover != null && recover.size() > 0) {
			lastRepayTime = DateUtil.dateFormat(recover.get(0).getRecoverTime());
		}
		return lastRepayTime;
	}

	/**
	 * 根据memberid获取账户资金
	 * 
	 * @throws Exception
	 */
	private FnAccount getAccount(Long memberId) throws Exception {
		QueryItem accountItem = new QueryItem(Module.FINANCE, Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", memberId));
		FnAccount account = this.baseService.getOne(accountItem, FnAccount.class);
		return account;
	}

	/**
	 * 计算债权价值
	 * 
	 * @throws Exception
	 */
	private BigDecimal getAmoutMoney(FnTender tender) throws Exception{
		FnLoan loan = getLoan(tender.getLoanId());
		//计算剩余本金
		BigDecimal waitPrincipal = NumberUtils.sub(tender.getRecoverPrincipal(), tender.getRecoverPrincipalYes());
		//获取复审时间
		Long successTime = tender.getSuccessTime();
		Date successDate = DateUtil.dateParse(successTime);
		Calendar successCal = Calendar.getInstance();
		successCal.setTime(successDate);
		int successYear = successCal.get(Calendar.YEAR);// 得到年
		int successMonth = successCal.get(Calendar.MONTH) + 1;// 得到月
		//获取当前时间
		Date currentDate = new Date();
		Calendar date = Calendar.getInstance();
		int currentYear = date.get(Calendar.YEAR);// 得到年
		int currentMonth = date.get(Calendar.MONTH) + 1;// 得到月
		//判断复审时间是否是当日
		String currentMonthFirst = null;
		String currentMonthLast = null;
		if(successYear == currentYear && successMonth == currentMonth){
			//获取当月时间
			currentMonthFirst = currentYear+ "-" + (successMonth+1) + "-01 00:00:00";
			//获取当月最后一天
			Date temp = DateUtil.addDay(DateUtil.addMonth(DateUtil.dateParse(currentMonthFirst), 1), -1);
			currentMonthLast = DateUtil.dateFormat(temp);
			currentMonthLast = currentMonthLast + "23:59:59";
		}
		//获取本期利息
		QueryItem recoverItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		recoverItem.setWhere(new Where("tender_id", tender.getId()));
		
		if(tender.getRepayType() != 3){
			List<NameValue> andList = new ArrayList<NameValue>();
			andList.add(new NameValue("recover_time", DateUtil.convert(currentMonthFirst), ">="));
			andList.add(new NameValue("add_time", DateUtil.convert(currentMonthLast), "<"));
			recoverItem.setWhere(Where.setAndList(andList));
		}
		
		
		recoverItem.setWhere(whereList);
		recoverItem.setOrders("period_no");
		List<FnTenderRecover> recoverList = this.baseService.getList(recoverItem, FnTenderRecover.class);
		FnTenderRecover currentRecover = recoverList.size() == 0 ? null : recoverList.get(0); 
		//获取上一期时间
		QueryItem lastRecoverItem = new QueryItem( Module.LOAN, Function.LN_RECOVER);
		lastRecoverItem.getWhere().add(Where.eq("tender_id", tender.getId()));
		lastRecoverItem.getWhere().add(Where.eq("period_no", currentRecover == null ? 1 : currentRecover.getPeriodNo() - 1));
		lastRecoverItem.setFields("recover_time");
		FnTenderRecover lastRecover = this.baseService.getOne(lastRecoverItem,FnTenderRecover.class);
		Long lastTime = lastRecover == null ? successTime : lastRecover.getRecoverTime();
		//获取第一期时间
		if(currentRecover.getPeriodNo() == 1){
			lastTime = successTime;
		}
		if(tender.getRepayType() == 6 && currentRecover.getPeriodNo() == 2){
			lastTime = successTime;
		}
		//转让时，当期未还
		//剩余未还本金 + 当期利息*min{成交日期 - 上期还款对应的应还款日期,30} / 30
		BigDecimal amount = BigDecimal.ZERO;
		if(currentRecover.getStatus() == -1){
			int days = DateUtil.daysBetween(DateUtil.dateParse(lastTime), currentDate);
			if(days > 30)days = 30;
			if(days < 0){
				days = Math.abs(days);
				if(days > 30)days = 30;
			}
			amount = waitPrincipal.add(currentRecover.getInterest().multiply(new BigDecimal(days/30)));
			//到期还本还息
			//剩余未还本金 + 当期利息 /(借款期限 *30)*持有天数
			if(tender.getRepayType() == 3){
				amount = waitPrincipal.add(currentRecover.getInterest().divide(new BigDecimal((Double.valueOf(loan.getPeriod()) / Double.valueOf(30 * days))),10,BigDecimal.ROUND_HALF_DOWN));
            }
            return amount;
		}
		//获取当前已还利息和次数
		QueryItem hasRepayItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		hasRepayItem.getWhere().add(Where.eq("tender_id", tender.getId()));
		hasRepayItem.getWhere().add(Where.eq("status", 1));
		hasRepayItem.getWhere().add(new Where("recover_time", DateUtil.convert(currentMonthFirst),">="));
		hasRepayItem.setFields("sum(interest) interest,count(id) id");
		FnTenderRecover hasRecover = this.baseService.getOne(hasRepayItem, FnTenderRecover.class);
		//转让时，当期已还
        //待收本金 - 当期利息*min{成交日期所在期的应还日期 - 成交日期,30} / 30
		int days = DateUtil.daysBetween(currentDate, DateUtil.dateParse(currentRecover.getRecoverTime()));
		if(days > 30)days = 30;
		if(days < 0){
			days = Math.abs(days);
			if(days > 30)days = 30;
		}
        if(currentRecover.getStatus() == 1 && hasRecover.getId() == 1){
            return waitPrincipal.subtract(currentRecover.getInterest().multiply(new BigDecimal(days/30)));
        }else{
            //转让时，当期及之后的N期已还，但还未还清
            //待收本金 - 当期利息*min{成交日期所在期的应还日期 - 成交日期,30} / 30 - 之后N期利息总和
            return NumberUtils.sub(waitPrincipal.subtract(currentRecover.getInterest().multiply(new BigDecimal(days/30))),(NumberUtils.sub(hasRecover.getInterest(),currentRecover.getInterest())));
        }
	}

	/**
	 * 根据当前期数和tenderid获取还款表
	 * 
	 * @throws Exception
	 */
	private FnTenderRecover getRecover(Long tenderId, int periodNo) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("period_no", periodNo));
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover;
	}

	/**
	 * 根据tenderid和当前期数获取之后已还款的还款表利息总和
	 */
	private BigDecimal getTotalInterest(Long tenderId, int periodNo) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(new Where("period_no", periodNo, ">"));
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("status", 1));
		item.setFields("sum(interest_yes) interest_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover == null ? null : recover.getInterestYes();
	}

	/**
	 * 根据loanId获取还款方式
	 * @param loanId
	 * @return
	 * @throws Exception 
	 */
	private String getRepayTypeByLoanId(Long loanId) throws Exception {
		FnLoan loan = getLoan(loanId);
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_REPAYTYPE);
		item.getWhere().add(Where.eq("id", loan.getRepayType()));
		FnLoanRepayType repayType = this.baseService.getOne(item, FnLoanRepayType.class);
		return repayType == null ? "" : repayType.getName();
	}
	
	/**
	 * 计算预期收益
	 */
	public BigDecimal investInterest(BigDecimal amount,Integer period,BigDecimal apr,Integer repayType,Double awardScale) {
		BigDecimal interestTotal = BigDecimal.ZERO;//预期收益
		BigDecimal interest = BigDecimal.ZERO;//利息
		BigDecimal awardAmount = BigDecimal.ZERO;//奖励
		if (awardScale != null) {
			awardScale = awardScale / 100;
		} else {
			awardScale = 0D;
		}
		if (amount != null) {
			TenderCondition repayCondtion = new TenderCondition();
			repayCondtion.setAmount(amount);
			repayCondtion.setApr(apr);
			repayCondtion.setCurrentTime(DateUtil.getCurrentTime());
			repayCondtion.setPeriod(period);
			repayCondtion.setRepayType(repayType);
			Tender tender = RepayUtil.getRepayInfo(repayCondtion);
			if (tender != null) {
				interest = tender.getInterestAll();
			}
			awardAmount = NumberUtils.mul(amount, new BigDecimal(awardScale));
			interestTotal = interest.add(awardAmount);

		}
		return interestTotal;
	}
	
	/**
	 * 根据tenderid查询最后还款时间
	 * @throws DyServiceException 
	 */
	private String getRecoverTime(Long tenderId,Integer period) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_RECOVER);
		item.getWhere().add(Where.eq("tender_id", tenderId));
		item.getWhere().add(Where.eq("period_no", period));
		FnTenderRecover recover = (FnTenderRecover)this.getOne(Module.LOAN,Function.LN_RECOVER, item, FnTenderRecover.class);
		String recoverTime = null;
		if(recover != null){
			return recover.getRecoverTime().toString() ;
//			recoverTime = DateUtil.dateFormat(recoverList.get(0).getRecoverTime());
		}
		return recoverTime;
	}
}
