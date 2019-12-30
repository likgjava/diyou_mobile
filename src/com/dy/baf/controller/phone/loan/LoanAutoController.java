package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanAutoConfig;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
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
import com.dy.core.utils.serializer.SerializerUtil;
import com.google.gson.Gson;

@Controller
public class LoanAutoController extends FrontBaseController {
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/loan/loan/auto", method=RequestMethod.GET)
	public ModelAndView toLoanIndex() {
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		
		ModelAndView view = new ModelAndView();
		if(isTrust && StringUtils.isEmpty(member.getTrustAccount())) {
			view.setViewName("redirect:/trust/public/reg");
			return view;
		}
		
		try {
			SystemInfo system = new SystemInfo("loan/auto/autoloan.jsp");
			view.addObject("cur_nav", "自动投标");
			
			Map result = new HashMap();
			
			
			//自动投标比例
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("value");
			queryItem.setWhere(new Where("nid", "borrow_auto_proportion"));
			SysSystemConfig systemConfig = getOneByEntity(queryItem, Module.SYSTEM, Function.SYS_CONFIG, SysSystemConfig.class);
			result.put("autoLoanProportion", systemConfig == null ? null : systemConfig.getValue());
			
			//账户余额
			queryItem = new QueryItem();
			queryItem.setFields("balance_amount");
			queryItem.setWhere(new Where("member_id", member.getId()));
			FnAccount account = getOneByEntity(queryItem, Module.FINANCE, Function.FN_ACCOUNT, FnAccount.class);
			result.put("balanceAmount", account.getBalanceAmount() == null ? "0.00" : NumberUtils.round(account.getBalanceAmount(), 2));
			
			//自动投标设置
			FnLoanAutoConfig loanAutoConfig = getLoanAutoConfig(member.getId());
			result.put("config", loanAutoConfig);
			result.put("rankList", generateMapList(2, ""));
			result.put("periodArea", generateMapList(30, "个月"));
			result.put("aprArea", generateMapList(24, "%"));
			result.put("loanCategorys", getLoanCategorys(loanAutoConfig.getLoanCategoryIds()));
			result.put("loanCategoryId", loanAutoConfig.getLoanCategoryIds() == null ? null : loanAutoConfig.getLoanCategoryIds().split(","));
			
			//排名
			if(loanAutoConfig.getStatus() != null && loanAutoConfig.getStatus() == 1)
				result.put("ranking", loanAutoConfig.getRanking());
			else
				result.put("ranking", "--");
			
			view.addObject("setting", new Gson().toJson(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	@ResponseBody
	@RequestMapping(value="/member/loan/auto",method=RequestMethod.POST)
	public DyResponse editAutoLoanConfig(FnLoanAutoConfig loanAutoConfig, HttpServletRequest request) throws Exception{
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		member = getMember(member.getId());
		if(member.getIsRealname() !=1 || member.getIsPhone() != 1 || member.getIsEmail() !=1 || (!isTrust && member.getPaypassword() == null)){
			return createApproveError("请先进行认证!");
		}
		
		String loanCategoryIds = "";
		Object obj = request.getParameterMap().get("loanCategoryId[]");
		if(obj != null) {
			for(String tmp : (String[])obj) {
				if(StringUtils.isEmpty(tmp)) continue;
				loanCategoryIds += ',' + tmp;
			}
			if(StringUtils.isEmpty(loanCategoryIds))
				return createErrorJsonResonse("请至少勾选一个标种"); 
			loanCategoryIds = loanCategoryIds.substring(1);
		} else {
			return createErrorJsonResonse("请至少勾选一个标种"); 
		}
		loanAutoConfig.setLoanCategoryIds(loanCategoryIds);
		
		//Validate
		StringBuffer errorMsg = new StringBuffer();
		if(loanAutoConfig.getAmount() == null) errorMsg.append(",借出金额");
		if(loanAutoConfig.getAccountAmountBalance() == null) errorMsg.append(",账户保留金额");
		if(loanAutoConfig.getAwardStatus() == null) errorMsg.append(",是否奖励");
		if(loanAutoConfig.getLoanCategoryIds() == null) errorMsg.append(",标种选择");
		if(loanAutoConfig.getValidateMin() == null) errorMsg.append(",最低借款期限");
		if(loanAutoConfig.getValidateMax() == null) errorMsg.append(",最高借款期限");
		if(loanAutoConfig.getAprMin() == null) errorMsg.append(",最低利率选项");
		if(loanAutoConfig.getAprMax() == null) errorMsg.append(",最高利率选项");
		if(StringUtils.isNotEmpty(errorMsg.toString())) return createErrorJsonResonse(errorMsg.toString().substring(1) + "不能为空");
		
		if(loanAutoConfig.getAmount()%10 != 0) return createErrorJsonResonse("输入金额必须为10的倍数");
		if(loanAutoConfig.getAccountAmountBalance().compareTo(BigDecimal.ZERO) <= 0) return createErrorJsonResonse("账户保留金额必须为大于0的整数");
		if(loanAutoConfig.getValidateMin() > loanAutoConfig.getValidateMax()) return createErrorJsonResonse("最低借款期限不能大于最高借款期限");
		if(loanAutoConfig.getAprMin() > loanAutoConfig.getAprMax()) return createErrorJsonResonse("最低利率选项不能大于最高利率选项");
		
		loanAutoConfig.setUpdateTime(DateUtil.getCurrentTime());
		if(loanAutoConfig.getId() == null) {
			loanAutoConfig.setMemberId(member.getId());
			loanAutoConfig.setMemberName(member.getName());
			insert(Module.LOAN, Function.LN_AUTOCONFIG, loanAutoConfig);
		} else
			update(Module.LOAN, Function.LN_AUTOCONFIG, loanAutoConfig);
		
		Map<String, Object> log = new HashMap<String, Object>();
		log.put("member_id", loanAutoConfig.getMemberId());
		log.put("member_name", loanAutoConfig.getMemberName());
		log.put("add_time", loanAutoConfig.getUpdateTime());
		log.put("add_ip", IpUtil.ipStrToLong(getRemoteIp()));
		log.put("operate_data", new SerializerUtil().serialize(convertEntityToMap(loanAutoConfig)));
		insert(Module.LOAN, Function.LN_AUTOCONFIGLOG, log);
		
		return createSuccessJsonResonse(null);
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/member/auto/autoLog",method=RequestMethod.POST)
	public Page<?> getAutoLog(Integer page) throws Exception{
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		
		//Get auto tender info
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("loan_id,loan_name,amount,status");
		queryItem.setOrders("id desc");
		queryItem.setLimit(5);
		queryItem.setPage(page);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("auto_status", 1));
		whereList.add(new Where("member_id", member.getId()));
		queryItem.setWhere(whereList);
		Page<FnTender> tenderResult = getPageByEntity(queryItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
		if(tenderResult == null || tenderResult.getItems() == null) return tenderResult;
		
		String loanIds = "";
		for(FnTender tender : tenderResult.getItems()) {
			loanIds += "," + tender.getLoanId(); 
		}
		
		//Get loan info
		List<FnLoan> loanList = null;
		if(StringUtils.isNotEmpty(loanIds)) {
			queryItem = new QueryItem();
			queryItem.setFields("id,member_name,category_id,apr,period,amount,credited_amount");
			queryItem.setWhere(Where.in("id", loanIds.substring(1)));
			loanList = getListByEntity(queryItem, Module.LOAN, Function.LN_LOAN, FnLoan.class);
		}
		List<Map> tenderList = new ArrayList<Map>();
		if(loanList != null && loanList.size() >= 0) {
			for(FnTender tender : tenderResult.getItems()) {
				for(FnLoan loan : loanList) {
					Map map = new HashMap();
					map.put("loan_id", tender.getLoanId());
					map.put("loan_name", tender.getLoanName());
					map.put("status", tender.getStatus());
					map.put("amount", tender.getAmount());
					map.put("apr", loan.getApr());
					map.put("period", loan.getPeriod());
					map.put("category_name", loan.getCategoryId());
					map.put("borrow_member", loan.getMemberName());
					BigDecimal progress = NumberUtils.div(NumberUtils.mul(loan.getCreditedAmount(), NumberUtils.ONE_HUNDRED), loan.getAmount());
					map.put("progress", NumberUtils.round(progress));
					if(loan.getId().equals(tender.getLoanId())) {
						tenderList.add(map);
					}
				}
			}
		}
		Page newPage = new Page();
		newPage.setTotal_items(tenderResult.getTotal_items());
		newPage.setTotal_pages(tenderResult.getTotal_pages());
		newPage.setEpage(tenderResult.getEpage());
		newPage.setPage(tenderResult.getPage());
		newPage.setItems(tenderList);
		
		return (Page)dataConvert(newPage, "category_name:getBorrowType");
	}
	
	@SuppressWarnings("unchecked")
	private List<Map> getLoanCategorys(String loanCategoryIds) {
		if(loanCategoryIds == null) loanCategoryIds = ",";
		else loanCategoryIds = ',' + loanCategoryIds;
		
		List<Map> result = new ArrayList<Map>();
		Map map = new HashMap();
		map.put("id", 1);
		map.put("name", "信用标");
		map.put("checked", loanCategoryIds.contains(",1"));
		result.add(map);
		
		map = new HashMap();
		map.put("id", 4);
		map.put("name", "担保标");
		map.put("checked", loanCategoryIds.contains(",4"));
		result.add(map);
		
		map = new HashMap();
		map.put("id", 5);
		map.put("name", "抵押标");
		map.put("checked", loanCategoryIds.contains(",5"));
		result.add(map);
		
		return result;
	}
	
	private List<Map<String, Object>> generateMapList(int size, String unit) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for(int i=0;i<size;i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", (i + 1));
			map.put("value", (i + 1) + unit);
			
			result.add(map);
		}
		return result;
	}
	
	private FnLoanAutoConfig getLoanAutoConfig(Long id) throws Exception {
		QueryItem queryItem = new QueryItem();
		queryItem.setAddRanking(true);
		queryItem.setOrders("status desc,update_time,id");
		queryItem.setWhere(new Where("member_id", id));
		FnLoanAutoConfig loanAutoConfig = getOneByEntity(queryItem, Module.LOAN, Function.LN_AUTOCONFIG, FnLoanAutoConfig.class);
		if(loanAutoConfig == null) loanAutoConfig = new FnLoanAutoConfig();
		
		return loanAutoConfig;
	}
	
	/**
	 * 自动投标修改页
	 * @return
	 */
	@RequestMapping(value="/loan/auto/repaypopup", method=RequestMethod.GET)
	public ModelAndView repaypopup() {
		
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("loan/auto/repaypopup.jsp");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
}