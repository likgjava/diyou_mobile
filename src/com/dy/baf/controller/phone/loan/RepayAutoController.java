package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.dy.baf.entity.common.FnLoanAutoRepayConfig;
import com.dy.baf.entity.common.FnLoanRepay;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;

/**
 * 自动还款
 * @author Administrator
 */
@Controller
@RequestMapping(value="/loan")
public class RepayAutoController extends FrontBaseController {
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/autorepay/index", method=RequestMethod.GET)
	public ModelAndView toRepayAutoIndex() throws Exception {
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		member = getMember(member.getId());
		ModelAndView view = new ModelAndView();
		if(isTrust && StringUtils.isEmpty(member.getTrustAccount())) {
			view.setViewName("redirect:/trust/public/reg");
			return view;
		}
		try {
			SystemInfo system = new SystemInfo("loan/auto/autorepay.jsp");
			view.addObject("cur_nav", "自动还款");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	@ResponseBody
	@RequestMapping(value="/autorepay/getConfig",method=RequestMethod.POST)
	public DyResponse getAutoRepayConfig(HttpServletRequest request) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		MbMember member = (MbMember) getSessionAttribute(Constant.SESSION_USER);
		QueryItem configItem = new QueryItem();
		configItem.setWhere(Where.eq("member_id", member.getId()));
		FnLoanAutoRepayConfig fnLoanAutoRepayConfig = this.getOneByEntity(configItem, Module.LOAN, Function.LN_AUTOREPAYCONFIG, FnLoanAutoRepayConfig.class);
		Integer isAutoRepay = -1;
		if (fnLoanAutoRepayConfig != null) {
			isAutoRepay = fnLoanAutoRepayConfig.getIsAutoRepay();
		}
		
		QueryItem accountItem = new QueryItem();
		accountItem.setWhere(Where.eq("member_id", member.getId()));
		FnAccount account = this.getOneByEntity(accountItem, Module.FINANCE, Function.FN_ACCOUNT, FnAccount.class);
		result.put("balance_amount", NumberUtils.format(account.getBalanceAmount()));
		result.put("is_auto_repay", isAutoRepay);

		return createSuccessJsonResonse(result);
	}

	@ResponseBody
	@RequestMapping(value="/autorepay/update",method=RequestMethod.POST)
	public DyResponse updateAutoRepay(HttpServletRequest request, Integer is_auto_repay) throws Exception {
		MbMember member = (MbMember) getSessionAttribute(Constant.SESSION_USER);
		member = getMember(member.getId());
		if (member.getIsRealname() != 1 || member.getIsPhone() != 1 || member.getIsEmail() != 1 || member.getPaypassword() == null) {
			return createApproveError("请先进行认证!");
		}
		QueryItem configItem = new QueryItem();
		configItem.setWhere(Where.eq("member_id", member.getId()));
		FnLoanAutoRepayConfig fnLoanAutoRepayConfig = this.getOneByEntity(configItem, Module.LOAN, Function.LN_AUTOREPAYCONFIG, FnLoanAutoRepayConfig.class);
		if (fnLoanAutoRepayConfig == null) {
			if (1 == is_auto_repay) {
				fnLoanAutoRepayConfig = new FnLoanAutoRepayConfig();
				fnLoanAutoRepayConfig.setIsAutoRepay(is_auto_repay);
				fnLoanAutoRepayConfig.setMemberId(member.getId());
				fnLoanAutoRepayConfig.setMemberName(member.getName());
				fnLoanAutoRepayConfig.setAddTime(DateUtil.getCurrentTime());
				fnLoanAutoRepayConfig.setUpdTime(DateUtil.getCurrentTime());
				this.insert(Module.LOAN, Function.LN_AUTOREPAYCONFIG, fnLoanAutoRepayConfig);
			}
		} else {
			fnLoanAutoRepayConfig.setIsAutoRepay(is_auto_repay);
			fnLoanAutoRepayConfig.setUpdTime(DateUtil.getCurrentTime());
			this.updateById(Module.LOAN, Function.LN_AUTOREPAYCONFIG, fnLoanAutoRepayConfig);
		}

		return createSuccessJsonResonse(null);
	}
	
	@ResponseBody
	@RequestMapping(value="/autorepay/getLoanConfigList",method=RequestMethod.POST)
	public DyResponse getAutoRepayConfigList(HttpServletRequest request) throws Exception {
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		if (member == null || member.getId() == null) {
			return this.createErrorJsonResonse("您未登陆！");
		}
		int pageNum = 1;
		String pageNumStr = request.getParameter("page");
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}

		String startTime = request.getParameter("start_time");
		String endTime = request.getParameter("end_time");

		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,ind,serialno,name,amount,credited_amount,status,add_time,category_id,reverify_time,period,repay_type");
		queryItem.setWhere(Where.eq("member_id", member.getId()));
		List<NameValue> andList = new ArrayList<NameValue>();
		if (StringUtils.isNotBlank(startTime)) {
			andList.add(new NameValue("reverify_time", DateUtil.convert(startTime), ">="));
		}
		if (StringUtils.isNotBlank(endTime)) {
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(DateUtil.dateParse(endTime));
			endCal.add(Calendar.DAY_OF_MONTH, 1);
			andList.add(new NameValue("reverify_time", DateUtil.convert(endCal.getTime()), "<"));
		}
		if (andList.size() > 0) {
			queryItem.setWhere(Where.setAndList(andList));
		}
		queryItem.setWhere(Where.ge("status", 6));
		queryItem.setOrders("add_time desc");
		queryItem.setPage(pageNum);
		queryItem.setLimit(3);
		Page page = this.getPageByMap(queryItem, Module.LOAN, Function.LN_LOAN);
		if (page.getItems() != null && page.getItems().size() > 0) {
			List<Map<String, Object>> valueList = (List<Map<String, Object>>) page.getItems();
			String loanIds = "";
			Long expireTime = 0L;
			Long reverifyTime = 0L;
			for (Map<String, Object> map : valueList) {
				if (loanIds == "") {
					loanIds += map.get("id");
				} else {
					loanIds += "," + map.get("id");
				}
				
				Integer repayType = Integer.valueOf(map.get("repay_type").toString());
				Integer period = Integer.valueOf(map.get("period").toString());
				reverifyTime = Long.valueOf(map.get("reverify_time").toString());
				if (5 == repayType) {
					expireTime = DateUtil.addDay(reverifyTime, period);
				} else {
					expireTime = DateUtil.addMonth(reverifyTime, period);
				}
				map.put("expire_time", expireTime);
				map.put("is_auto_repay", -1); //自动还款状态
			}
			
			QueryItem tdQueryItem = new QueryItem();
			tdQueryItem.setFields("id,loan_id,amount_total");
			tdQueryItem.setWhere(Where.in("loan_id", loanIds));
			List<Map> tndList = this.getListByMap(tdQueryItem, Module.LOAN, Function.LN_REPAY);
			for (Map tndMap : tndList) {
				String loanId = tndMap.get("loan_id").toString();
				BigDecimal amountTotal = (BigDecimal) tndMap.get("amount_total");
				for (Map<String, Object> map : valueList) {
					if (loanId.equals(map.get("id").toString())) {
						map.put("amount_total", amountTotal);
						break;
					}
				}
			}
			
			QueryItem configItem = new QueryItem();
			configItem.setWhere(Where.eq("member_id", member.getId()));
			configItem.setWhere(Where.in("loan_id", loanIds));
			List<FnLoanAutoRepayConfig> repayConfigList = this.getListByEntity(configItem, Module.LOAN, Function.LN_AUTOREPAYCONFIG, FnLoanAutoRepayConfig.class);
			if (repayConfigList != null && repayConfigList.size() > 0) {
				for (FnLoanAutoRepayConfig repayConfig : repayConfigList) {
					String loanId = repayConfig.getLoanId().toString();
					for (Map<String, Object> map : valueList) {
						if (loanId.equals(map.get("id").toString())) {
							map.put("is_auto_repay", repayConfig.getIsAutoRepay());
							break;
						}
					}
				}
			}
			
			QueryItem tenderItem = new QueryItem();
			tenderItem.setFields("loan_id,sum(recover_amount_yes) recover_amount_yes");
			tenderItem.setWhere(Where.in("loan_id", loanIds));
			tenderItem.setGroup("loan_id");
			List<Map> tenderMapList = this.getListByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
			if (tenderMapList != null && tenderMapList.size() > 0) {
				for (Map tenderMap : tenderMapList) {
					String loanId = tenderMap.get("loan_id").toString();
					for (Map<String, Object> map : valueList) {
						if (loanId.equals(map.get("id").toString())) {
							map.put("amount_yes", new BigDecimal(tenderMap.get("recover_amount_yes").toString()));
							break;
						}
					}
				}
			}
			
		}
		return createSuccessJsonResonse(page);
	}
	
	
	@ResponseBody
	@RequestMapping(value="/autorepay/getLoanRepay",method=RequestMethod.POST)
	public DyResponse getLoanAutoRepayInfo(Long id) throws Exception {
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,ind,serialno,name,amount,credited_amount,status,add_time,category_id,reverify_time,period,repay_type");
		queryItem.setWhere(Where.eq("id", id));
		Map<String, Object> loanMap = this.getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);

		QueryItem tdQueryItem = new QueryItem();
		tdQueryItem.setFields("id,loan_id,member_id,amount_total");
		tdQueryItem.setWhere(Where.eq("loan_id", id));
		FnLoanRepay loanRepay = this.getOneByEntity(tdQueryItem, Module.LOAN, Function.LN_REPAY, FnLoanRepay.class);
		loanMap.put("amount_total", NumberUtils.format(loanRepay.getAmountTotal()));
	
		QueryItem accountItem = new QueryItem();
		accountItem.setWhere(Where.eq("member_id", loanRepay.getMemberId()));
		FnAccount account = this.getOneByEntity(accountItem, Module.FINANCE, Function.FN_ACCOUNT, FnAccount.class);
		loanMap.put("balance_amount", NumberUtils.format(NumberUtils.sub(account.getBalanceAmount(), account.getFreezeAmount())));
		
		QueryItem configItem = new QueryItem();
		configItem.setWhere(Where.eq("member_id", loanRepay.getMemberId()));
		configItem.setWhere(Where.eq("loan_id", id));
		FnLoanAutoRepayConfig fnLoanAutoRepayConfig = this.getOneByEntity(configItem, Module.LOAN, Function.LN_AUTOREPAYCONFIG, FnLoanAutoRepayConfig.class);
		Integer isAutoRepay = -1;
		if (fnLoanAutoRepayConfig != null) {
			isAutoRepay = fnLoanAutoRepayConfig.getIsAutoRepay();
		}
		loanMap.put("is_auto_repay", isAutoRepay);
		
		
		return createSuccessJsonResonse(loanMap);
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/repayauto/loglist",method=RequestMethod.POST)
	public Page<?> getRepayAutoLog(Integer page) throws Exception {
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,member_name,loan_id,loan_name,period_no,principal_yes,interest_yes,amount_yes,balance_amount,repay_time,repay_time_yes,status");
		queryItem.setOrders("id desc");
		queryItem.setLimit(5);
		queryItem.setPage(page != null ? page : 1);
		queryItem.setWhere(Where.eq("member_id", member.getId()));
		Page<Map> repayLogResult = getPageByMap(queryItem, Module.LOAN, Function.LN_AUTOREPAYLOG);
		if (repayLogResult == null || repayLogResult.getItems() == null || repayLogResult.getItems().size() < 1) {
			return repayLogResult;
		}
		return (Page)dataConvert(repayLogResult, "status:getAutoRepayStatus", "repay_time,repay_time_yes");
	}
}