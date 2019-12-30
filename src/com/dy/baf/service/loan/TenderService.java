package com.dy.baf.service.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanCategory;
import com.dy.baf.entity.common.FnLoanInfo;
import com.dy.baf.entity.common.FnLoanRepayPeriod;
import com.dy.baf.entity.common.FnLoanRepayType;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 我的投资
 * @author 波哥
 * @date 2015年9月23日 上午10:16:23
 * @version V1.0
 */
@Service("mobileTenderService")
public class TenderService extends MobileService {

	@Autowired
	private BaseService baseService;

	public DyPhoneResponse isMyLoan(String login_token, String loan_id) throws Exception {
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("用户标识不能为空");
		}
		if (StringUtils.isBlank(loan_id)) {
			return errorJsonResonse("借款ID不能为空");
		}

		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		queryItem.getWhere().add(Where.eq("id", loan_id));
		queryItem.setFields("member_id");
		FnLoan fnLoan = this.baseService.getOne(queryItem, FnLoan.class);
		
		QueryItem loanInfoItem = new QueryItem();
		loanInfoItem.setFields("id,password,additional_status");
		loanInfoItem.getWhere().add(Where.eq("loan_id", loan_id));
		FnLoanInfo fnLoanInfo = this.getOneByEntity(loanInfoItem, Module.LOAN, Function.LN_LOANINFO, FnLoanInfo.class);
		Integer additionalStatus = null ;
		if(fnLoanInfo != null){
			additionalStatus = fnLoanInfo.getAdditionalStatus() ;
		}
		
		//判断是否是新手标
		if(null != additionalStatus && 1 == additionalStatus){
			//流转标
			if("3".equals(fnLoan.getCategoryType())){
				QueryItem tenderItem = new QueryItem();
				tenderItem.getWhere().add(Where.eq("member_id", login_token));
				tenderItem.getWhere().add(Where.notEq("status", -1));
				List<FnTender> tenders = this.getListByEntity(tenderItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
				if(null != tenders && 0 < tenders.size()){
					return errorJsonResonse("不是新手无法对新手标进行投标!");
				}
			}else{
				QueryItem tenderItem = new QueryItem();
				//tenderItem.setFields("count(-1) row_count");
				tenderItem.getWhere().add(Where.eq("member_id", login_token));
				tenderItem.getWhere().add(Where.notEq("loan_id", loan_id));
				tenderItem.getWhere().add(Where.eq("status", 1));
				List<FnTender> tenders = this.getListByEntity(tenderItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
				if (null != tenders && 0 < tenders.size()) {
					return errorJsonResonse("不是新手无法对新手标进行投标");
				}
			}
		}
		
		Map<String, String> responseMap = new HashMap<String, String>();
		if (login_token.equals(String.valueOf(fnLoan.getMemberId()))) {
			responseMap.put("is_myloan", "1");
		} else {
			responseMap.put("is_myloan", "-1");
		}
		return successJsonResonse(responseMap);
	}

	/**
	 * 我的投资统计
	 * 
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> myTenderData(String login_token) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		QueryItem recoverItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		recoverItem.getWhere().add(Where.eq("tender_member_id", user.getId()));
		recoverItem.getWhere().add(Where.eq("transfer_member_id", 0));
		List<FnTenderRecover> tenderRecoverList = this.baseService.getList(recoverItem, FnTenderRecover.class);
		BigDecimal amount = new BigDecimal(0);// 投资总金额
		BigDecimal recoverPrincipal = new BigDecimal(0);// 应回款总本金
		BigDecimal recoverPrincipalYes = new BigDecimal(0);// 已收本金
		BigDecimal recoverInterestYes = new BigDecimal(0);// 已收利息
		BigDecimal principalWaitTotal = new BigDecimal(0);// 待收本金
		BigDecimal waitInterest = new BigDecimal(0);// 待收利息
		for (FnTenderRecover tender : tenderRecoverList) {
			amount = amount.add(tender.getAmount());
			recoverPrincipal = recoverPrincipal.add(tender.getPrincipal());
			recoverPrincipalYes = recoverPrincipalYes.add(tender.getPrincipalYes());
			recoverInterestYes = recoverInterestYes.add(tender.getInterestYes());
			if (tender.getStatus() != 1) {
				principalWaitTotal = principalWaitTotal.add(tender.getPrincipal());
				waitInterest = waitInterest.add(tender.getInterest());
			}
		}

		// 投资奖励金额
		QueryItem tenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		tenderItem.setFields("sum(award_amount) sum_award");
		tenderItem.getWhere().add(Where.eq("member_id", user.getId()));
		tenderItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> awardMap = this.baseService.getOne(tenderItem);
		BigDecimal awardAmountTotal = new BigDecimal(0);
		if (awardMap != null && awardMap.get("sum_award") != null) {
			awardAmountTotal = new BigDecimal(awardMap.get("sum_award").toString());
		}

		BigDecimal recoverAmountYes = recoverInterestYes.add(awardAmountTotal); // 累计收益=利息+奖励
		// 累计收益率
		BigDecimal yield = new BigDecimal(0);
		if (recoverPrincipalYes.compareTo(new BigDecimal(0)) > 0) {
			yield = NumberUtils.div(recoverInterestYes.multiply(NumberUtils.ONE_HUNDRED), recoverPrincipalYes, 2);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("amount_yes_total", recoverAmountYes);
		map.put("award_amount_total", awardAmountTotal);
		map.put("interest_wait_total", waitInterest);
		map.put("principal_wait_total", principalWaitTotal);
		map.put("interest_award", recoverAmountYes);
		map.put("yield", yield);
		return map;

	}

	/**
	 * 我的投资记录列表
	 * 
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse mytenderlist(String login_token, Integer page, String status, String start_time, String end_time)
			throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		// 投资总额，总代收本息，总已收本息
		BigDecimal tender_amount_total = new BigDecimal(0);// 投资总额
		BigDecimal recover_amount_total = new BigDecimal(0);// 总待收本息
		BigDecimal recover_amount_yes_total = new BigDecimal(0);// 总已收本息
		BigDecimal recover_amount_wait_total = new BigDecimal(0);// 总待收利息
		QueryItem totalItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		List<Where> where = new ArrayList<Where>();
		where.add(new Where("member_id", user.getId()));
		String isStartTime = "no";

		totalItem.setWhere(where);
		List<FnTender> tenderList = this.baseService.getList(totalItem, FnTender.class);
		if (tenderList != null) {
			for (FnTender tender : tenderList) {
				tender_amount_total = tender_amount_total.add(tender.getAmount());
				FnTenderRecover recover = getRecoverAmount(tender.getId());// 已收
				FnTenderRecover waitRecover = getRecoverMoney(tender.getId());// 未收
				if (recover != null) {
					recover_amount_yes_total = recover_amount_yes_total.add(recover.getPrincipalYes().add(recover.getInterestYes()));
				}
				if (waitRecover != null) {
					recover_amount_total = recover_amount_total.add(waitRecover.getAmount().subtract(waitRecover.getAmountYes()));
					recover_amount_wait_total = recover_amount_wait_total.add(waitRecover.getInterest().subtract(waitRecover.getInterestYes()));
				}
			}
		}
		// 投资列表
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		queryItem.setPage(page == null ? 1 : page);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id", user.getId()));

		queryItem.setWhere(whereList);
		queryItem.setLimit(10);
		queryItem.setOrders("add_time desc");
		Page pageObj = this.baseService.getPage(queryItem, Map.class);
		List<Map<String, Object>> tenderLogList = pageObj.getItems();
		for (Map<String, Object> map : tenderLogList) {
			map.put("loanId", map.get("loan_id"));
			map.put("tenderId", map.get("id"));
			map.put("success_time", map.get("success_time"));
			map.put("expire_time", map.get("expire_time"));
			map.put("loan_name", map.get("loan_name"));
			map.put("amount", map.get("amount"));
			FnTenderRecover recover = getRecoverAmount(Long.valueOf(map.get("id").toString()));// 已收
			FnTenderRecover waitRecover = getRecoverMoney(Long.valueOf(map.get("id").toString()));// 未收
			BigDecimal recover_interest = BigDecimal.ZERO;
			if (recover != null) {
				recover_interest = recover.getInterestYes();
			}

			map.put("award_interest", new BigDecimal(map.get("recover_interest").toString()).add(new BigDecimal(map.get("award_amount").toString())));
			map.put("recover_interest_yes", recover_interest);
			String status_name = null;

			if ("1".equals(map.get("status").toString()) && "-1".equals(map.get("recover_status").toString()) && !"1".equals(map.get("transfer_status").toString())) {
				status_name = "回款中";
			} else if ("-2".equals(map.get("status").toString())) {// 投标中
				status_name = "投标中";
				map.put("success_time", null);
				map.put("expire_time", null);
			} else if ("1".equals(map.get("recover_status").toString()) && !"1".equals(map.get("transfer_status").toString())) {// 已结清
				status_name = "已结清";
			} else if ("-1".equals(map.get("status").toString())) {// 已流标
				status_name = "已流标";
				map.put("success_time", null);
				map.put("expire_time", null);
			} else if ("1".equals(map.get("transfer_status").toString())) {
				status_name = "已转让";
				map.put("success_time", null);
				map.put("expire_time", null);
			}
			BigDecimal recover_amount = BigDecimal.ZERO;
			if (waitRecover != null) {
				recover_amount = waitRecover.getAmount().subtract(waitRecover.getAmountYes());// 待收本息
			}
			map.put("recover_amount", recover_amount);
			map.put("status_name", status_name);
		}
		Map<String, Object> totalMap = new HashMap<String, Object>();
		totalMap.put("recover_amount_total", recover_amount_total);
		totalMap.put("recover_amount_wait_total", recover_amount_wait_total);
		totalMap.put("recover_amount_yes_total", recover_amount_yes_total);
		totalMap.put("tender_amount_total", tender_amount_total);
		totalMap.put("isStartTime", isStartTime);
		totalMap.put("epage", pageObj.getEpage());
		totalMap.put("items", pageObj.getItems());
		totalMap.put("page", pageObj.getPage());
		totalMap.put("total_items", pageObj.getTotal_items());
		totalMap.put("total_pages", pageObj.getTotal_pages());
		return successJsonResonse(totalMap);
	}

	/**
	 * 收款详情获取数据
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> tenderinfodata(Long id) throws Exception {
		FnTender tender = getTender(id);
		Map<String, Object> tenderMap = new HashMap<String, Object>();
		tenderMap.put("amount", tender.getAmount());
		tenderMap.put("recover_income_all", tender.getRecoverInterest().add(tender.getAwardAmount()));
		tenderMap.put("wait_principal", tender.getRecoverPrincipal().subtract(tender.getRecoverPrincipalYes())) ;
		tenderMap.put("recover_income", tender.getAwardAmount().add(tender.getRecoverInterestYes())) ;
		tenderMap.put("award_amount", tender.getAwardAmount()) ;
		tenderMap.put("loan_id", tender.getLoanId()) ;
		
		
		
		BigDecimal recoverPrincipalWait = BigDecimal.ZERO;
		BigDecimal recoverInterestYes = BigDecimal.ZERO;
		List<FnTenderRecover> tenderRecoverList = getTenderRecover(tender.getId());
		if(tenderRecoverList!=null && !tenderRecoverList.isEmpty()){
			for (FnTenderRecover fnTenderRecover : tenderRecoverList) {
				if(fnTenderRecover.getTransferMemberId() == 0){
					if(fnTenderRecover.getRecoverType() == null || fnTenderRecover.getRecoverType() != 4){
						recoverPrincipalWait = recoverPrincipalWait.add(fnTenderRecover.getPrincipal().subtract(fnTenderRecover.getPrincipalYes()));
					}
					recoverInterestYes  = recoverInterestYes.add(fnTenderRecover.getInterestYes());
				}
			}
		}
		tenderMap.put("wait_principal", recoverPrincipalWait) ;
		tenderMap.put("recover_income", recoverInterestYes.add(tender.getAwardAmount())) ;
		// 借款标详情
		FnLoan loan = getLoan(tender.getLoanId());
		Map<String, Object> loanMap = new HashMap<String, Object>();
		loanMap.put("id", loan.getId());
		loanMap.put("category_type", loan.getCategoryType());
		loanMap.put("name", loan.getName());
		loanMap.put("serialno", loan.getSerialno());
		loanMap.put("apr", loan.getApr());
		loanMap.put("repay_type_name", getRepayStatus(loan.getRepayType()));
		loanMap.put("verify_time", DateUtil.dateFormat(loan.getVerifyTime()));
		loanMap.put("overdue_time", DateUtil.dateFormat(loan.getOverdueTime()));
		loanMap.put("status", loan.getStatus());
		Integer repayType = loan.getRepayType();
		if (repayType == 5) {
			loanMap.put("period_name", loan.getPeriod() + "天");
			loanMap.put("expire_time", DateUtil.addDay(loan.getReverifyTime(), loan.getPeriod()));
		} else {
			loanMap.put("period_name", loan.getPeriod() + "个月");
			loanMap.put("expire_time", DateUtil.addMonth(loan.getReverifyTime(), loan.getPeriod()));
		}
		loanMap.put("period", loan.getPeriod());
		loanMap.put("repay_type", repayType);
		loanMap.put("reverify_time", loan.getReverifyTime());
		Integer status = loan.getStatus() ;
		String statusName = "";
		String statusTenderName = "";
		if (tender.getRecoverType() != null && "1".equals(tender.getRecoverType().toString())) {
			statusTenderName =  "正常还款";
		} else if (tender.getRecoverType() != null && "2".equals(tender.getRecoverType().toString())) {
			statusTenderName = "逾期还款";
		} else if (tender.getRecoverType() != null && "3".equals(tender.getRecoverType().toString())) {
			statusTenderName = "提前还款";
		} else if (tender.getRecoverType() != null && "4".equals(tender.getRecoverType().toString())) {
			statusTenderName = "网站垫付";
			/*tenderMap.put("wait_principal", 0) ;*///5621
		} else if (tender.getRecoverType() != null && "5".equals(tender.getRecoverType().toString())) {
			statusTenderName = "未还款";
		} else {
			if (tender.getStatus() != null && "1".equals(tender.getStatus().toString())) {
				statusTenderName = "已还款";
			} else {
				statusTenderName = "未还款";
			}
		}
		if (tender.getTransferMemberId() == null || !"0".equals(tender.getTransferMemberId().toString())) {
			statusTenderName = "已转让";
		}
		tenderMap.put("status_tender_name", statusTenderName) ;
		if (3 == status) {
			if("3".equals(loan.getCategoryType())){
				BigDecimal creditedAmount=loan.getCreditedAmount();
				BigDecimal borrowAmount=loan.getAmount();
				if(creditedAmount.compareTo(borrowAmount)==0){
					statusName = "回购中";
				}else{
					statusName = "流转中";	
				}
				
			}else{
				statusName = "借款中";
			}
		} else if (4 == status) {
			statusName = "满标复审";
		} else if (5 == status || 6 == status) {
			if("3".equals(loan.getCategoryType())){
				statusName = "回购中";
			}else{
				statusName = "还款中";
			}
		} else if (7 == status) {
			if("3".equals(loan.getCategoryType())){
				statusName = "回购完";
			}else{
				statusName = "已还完";
			}
		}
		tenderMap.put("status_name", statusName) ;
		// 还款详情
		List<Map> recoverList = getRecoverList(tender.getId());
		List<Map> newList = new ArrayList<Map>();
		for (Map map : recoverList) {
			if (map.get("recover_type") != null && "1".equals(map.get("recover_type").toString())) {
				map.put("status_name", "正常还款");
			} else if (map.get("recover_type") != null && "2".equals(map.get("recover_type").toString())) {
				map.put("status_name", "逾期还款");
			} else if (map.get("recover_type") != null && "3".equals(map.get("recover_type").toString())) {
				map.put("status_name", "提前还款");
			} else if (map.get("recover_type") != null && "4".equals(map.get("recover_type").toString())) {
				map.put("status_name", "网站垫付");
			} else if (map.get("recover_type") != null && "5".equals(map.get("recover_type").toString())) {
				map.put("status_name", "未还款");
			} else {
				if (map.get("status") != null && "1".equals(map.get("status").toString())) {
					map.put("status_name", "已还款");
				} else {
					map.put("status_name", "未还款");
				}
			}
			if (map.get("transfer_member_id") == null || !"0".equals(map.get("transfer_member_id").toString())) {
				map.put("status_name", "已转让");
				map.put("principal_yes", 0);
				map.put("interest_yes", 0);
			}
			map.put("recover_time", DateUtil.dateFormat(Long.valueOf(map.get("recover_time").toString())));
			newList.add(map);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("loan_info", loanMap);
		map.put("recover_info", newList);
		map.put("tender_info", tenderMap);
		return map;
	}

	/**
	 * 电子合同获取数据
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse getAgreeInfo(Long loanId) throws Exception {
		FnLoan loan = getLoan(loanId);
		QueryItem cateItem = new QueryItem();
		cateItem.getWhere().add(Where.eq("id", loan.getCategoryId()));
		cateItem.setFields("nid,agreement_id");
		FnLoanCategory loanCate = this.getOneByEntity(cateItem, Module.LOAN, Function.LN_CATEGORY,FnLoanCategory.class);
		// 查询协议书
		QueryItem agreeItem = new QueryItem(Module.CONTENT, Function.CT_AGREEMENT);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("id", loanCate.getAgreementId()));
		//whereList.add(Where.eq("type", loanCate.getNid()));
		agreeItem.setWhere(whereList);
		CtAgreement agreement = this.baseService.getOne(agreeItem, CtAgreement.class);
		// 借款信息
		Map<String, Object> loanMap = new HashMap<String, Object>();
		Long serialno = loan.getSerialno();
		String member_name = loan.getMemberName();
		String repay_type = getRepayStatus(loan.getRepayType());
		Long reverify_time = loan.getReverifyTime() == null ? loan.getAddTime() : loan.getReverifyTime();
		String web_name = getSysConfig("site_name");
		String borrow_period_name = "";
		Long repay_last_time = null;
		Integer repayType = loan.getRepayType();
		if (repayType == 5) {
			borrow_period_name = loan.getPeriod() + "天";
			repay_last_time = DateUtil.addDay(reverify_time, loan.getPeriod());
		} else {
			borrow_period_name = loan.getPeriod() + "个月";
			repay_last_time = DateUtil.addMonth(reverify_time, loan.getPeriod());
		}
		BigDecimal apr = loan.getApr();
		BigDecimal credited_amount = new BigDecimal(0);
		BigDecimal repayAmount = new BigDecimal(0);
		// 出借人列表
		List<FnTender> tenderList = getTenderList(loan.getId());
		for (FnTender tender : tenderList) {
			credited_amount = credited_amount.add(tender.getAmount());
			repayAmount = repayAmount.add(tender.getRecoverAmount());
		}
		// 债券转让明细
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		queryItem.setWhere(new Where("loan_id", loanId));
		queryItem.setWhere(new Where("status", 2));
		queryItem.setFields("member_name,buy_member_name,amount,success_time");
		List<Map> item = (List<Map>) (this.baseService.getList(queryItem));
		// 还款明细
		List<FnLoanRepayPeriod> repayList = getRepayList(loan.getId());
		loanMap.put("serialno", serialno);
		loanMap.put("member_name", member_name);
		loanMap.put("reverify_time", reverify_time);
		loanMap.put("web_name", web_name);
		loanMap.put("repay_type", repay_type);
		loanMap.put("borrow_period_name", borrow_period_name);
		loanMap.put("repay_last_time", repay_last_time);
		loanMap.put("apr", apr);
		loanMap.put("credited_amount", credited_amount);
		loanMap.put("repayAmount", repayAmount);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("agreement", agreement);
		map.put("loan_info", loanMap);
		map.put("tender", tenderList);
		map.put("repay", repayList);
		if (item != null && item.size() > 0) {
			map.put("transfre", item);
		}
		return successJsonResonse(map);
	}

	/**
	 * 根据id获取tender
	 * 
	 * @throws Exception
	 */
	private FnTender getTender(Long id) throws Exception {
		QueryItem tenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		tenderItem.getWhere().add(Where.eq("id", id));
		FnTender tender = this.baseService.getOne(tenderItem, FnTender.class);
		return tender;
	}

	/**
	 * 根据id获取loan
	 * 
	 * @throws Exception
	 */
	private FnLoan getLoan(Long id) throws Exception {
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		loanItem.getWhere().add(Where.eq("id", id));
		FnLoan loan = this.baseService.getOne(loanItem, FnLoan.class);
		return loan;
	}

	/**
	 * 更加id查询我的投资回款表
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private List<FnTenderRecover> getTenderRecover(Long tenderId) throws Exception {
		QueryItem recoverItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		recoverItem.getWhere().add(Where.eq("tender_id", tenderId));
		return this.baseService.getList(recoverItem, FnTenderRecover.class);
	}
	/**
	 * 根据tenderid获取recover
	 * 
	 * @throws Exception
	 */
	private List<Map> getRecoverList(Long tenderId) throws Exception {
		QueryItem recoverItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		recoverItem.getWhere().add(Where.eq("tender_id", tenderId));
		List<Map> recoverList = this.baseService.getList(recoverItem, Map.class);
		return recoverList;

	}

	/**
	 * 根据id获取还款方式
	 * 
	 * @throws Exception
	 */
	private String getRepayStatus(Integer id) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		item.setFields("name");
		item.getWhere().add(Where.eq("id", id));
		FnLoanRepayType repayType = this.baseService.getOne(item, FnLoanRepayType.class);
		return repayType.getName();
	}

	/**
	 * 获取系统配置
	 * 
	 * @throws Exception
	 */
	private String getSysConfig(String type) throws Exception {
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		item.getWhere().add(Where.eq("nid", type));
		SysSystemConfig config = this.baseService.getOne(item, SysSystemConfig.class);
		return config.getValue();
	}

	/**
	 * 根据loanId查询tenderList
	 * 
	 * @throws Exception
	 */
	private List<FnTender> getTenderList(Long loanId) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_TENDER);
		item.getWhere().add(Where.eq("loan_id", loanId));
		List<FnTender> tenderList = this.baseService.getList(item, FnTender.class);
		return tenderList;
	}

	/**
	 * 根据loanID获取借款明细
	 * 
	 * @throws Exception
	 */
	private List<FnLoanRepayPeriod> getRepayList(Long loanId) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_REPAYPERIOD);
		item.getWhere().add(Where.eq("loan_id", loanId));
		List<FnLoanRepayPeriod> repayList = this.baseService.getList(item, FnLoanRepayPeriod.class);
		return repayList;
	}

	/**
	 * 获取投资提示信息
	 * 
	 * @return
	 */
	public DyPhoneResponse getLoanTip(Long id) throws Exception {
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		queryItem.setFields("loan_id,loan_name,add_time,repay_type");
		queryItem.setWhere(Where.eq("id", id));
		FnTender fnTender = this.baseService.getOne(queryItem, FnTender.class);

		queryItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		queryItem.setFields("id,ind,serialno,name,repay_type,apr,category_id");
		queryItem.setWhere(Where.eq("id", fnTender.getLoanId()));
		FnLoan fnLoan = this.baseService.getOne(queryItem, FnLoan.class);

		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "项目名称");
		map.put("content", fnTender.getLoanName());
		dataList.add(map);

		if (false) {
			map = new HashMap<String, Object>();
			map.put("title", "保障机构");
			map.put("content", "厦门帝网有限公司");
			dataList.add(map);
		}

		map = new HashMap<String, Object>();
		map.put("title", "收益率");
		map.put("content", fnLoan.getApr() + "%");
		dataList.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "还款方式");
		queryItem = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		queryItem.setFields("id,name");
		queryItem.setWhere(Where.eq("id", fnLoan.getRepayType()));
		FnLoanRepayType repayType = this.baseService.getOne(queryItem, FnLoanRepayType.class);
		map.put("content", repayType.getName());
		dataList.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "投标时间");
		map.put("content", DateUtil.dateFormat(fnTender.getAddTime()));
		dataList.add(map);
		return successJsonResonse(dataList);
	}

	/**
	 * 根据tenderId获取待收本金，待收利息，待收总额
	 */
	private FnTenderRecover getRecoverMoney(Long tenderId) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("transfer_member_id", 0));
		whereList.add(Where.eq("status", -1));
		item.setFields(
				"sum(amount) amount,sum(amount_yes) amount_yes,sum(interest) interest,sum(interest_yes) interest_yes,sum(principal) principal ,sum(principal_yes) principal_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover;
	}

	/**
	 * 根据tenderId获取已收本金，已收利息，已收总额
	 */
	private FnTenderRecover getRecoverAmount(Long tenderId) throws Exception {
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("transfer_member_id", 0));
		whereList.add(Where.eq("status", 1));
		item.setFields(
				"sum(amount) amount,sum(amount_yes) amount_yes,sum(interest) interest,sum(interest_yes) interest_yes,sum(principal) principal ,sum(principal_yes) principal_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover;
	}
}
