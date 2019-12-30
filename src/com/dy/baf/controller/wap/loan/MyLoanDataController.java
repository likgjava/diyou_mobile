package com.dy.baf.controller.wap.loan;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.custom.RepayInfo;
import com.dy.baf.service.loan.MyLoanDataService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.trust.TrustManager;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DataConvertUtil;
import com.dy.httpinvoker.RepayService;

@Controller(value="wapMyLoanDataController")
public class MyLoanDataController extends WapBaseController {
	
	@Autowired
	private MyLoanDataService myLoanDataService;
	
	@Autowired
	private RepayService repayService;
	
	@ResponseBody
	@RequestMapping(value="/loan/myLoanData", method=RequestMethod.GET)
	public DyResponse myLoanData(HttpServletRequest request) throws Exception{
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if (member == null || member.getId() == null) {
				return this.createErrorJsonResonse("您未登陆！");
			}
			String pageNumStr = request.getParameter("pageNumSt");
			String startTime = request.getParameter("startTime");
			String endTime = request.getParameter("endTime");
			String epage = request.getParameter("epage");
			String status = request.getParameter("status");
			Page myLoanDataList = myLoanDataService.myLoanDataList(status, member,pageNumStr, startTime, endTime, epage, isTrust);
			return createSuccessJsonResonse(new DataConvertUtil(myLoanDataList).setStatus("status_name", optionUtil.getBorrowStatus())
					.setDate("add_time", "yyyy-MM-dd").convert());
		} catch (Exception e) {
			logger.error(e);
			return createErrorJsonResonse(e.getMessage());
		}
	}

	
	@ResponseBody
	@RequestMapping(value="/loan/getrepayINfo", method=RequestMethod.GET)
	public DyResponse getrepayINfo(HttpServletRequest request) throws Exception{
		try {
			String id = request.getParameter("id");
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if (member == null || member.getId() == null) {
				return this.createErrorJsonResonse("您未登陆！");
			}
			//借款信息
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id,category_id,category_type,serialno,name,repay_type,repay_type repay_type_name,period,apr,credited_amount");
			queryItem.setWhere(Where.eq("id", id));
			queryItem.getWhere().add(Where.eq("member_id", member.getId()));
			Map<String, Object> loanMap = getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);
			if(loanMap == null) return createErrorJsonResonse("您没有权限查看该借款标的还款记录");
			return createSuccessJsonResonse(myLoanDataService.getrepayINfo(Long.parseLong(id),member, loanMap));
		} catch (Exception e) {
			logger.error(e);
			return createErrorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 还款信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/loan/repay/repaydata", method=RequestMethod.GET)
	public DyResponse getRepayData(HttpServletRequest request) {
		try {
			String repayReriodId = request.getParameter("id");
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if (member == null || member.getId() == null) {
				return this.createErrorJsonResonse("您未登陆！");
			}
			DyResponse response = repayService.getRepayData(Long.parseLong(repayReriodId), member.getId());
			if (DyResponse.OK == response.getStatus()) {
				RepayInfo repayInfo = (RepayInfo) response.getData();
				Map<String, Object> valueMap = new HashMap<String, Object>();
				valueMap.put("id", repayReriodId);
				valueMap.put("repay_id", repayInfo.getRepayPeriodId());
				valueMap.put("loan_id", repayInfo.getLoanId());
				valueMap.put("loan_name", repayInfo.getLoanName());
				valueMap.put("period_no", repayInfo.getPeriodNo());
				valueMap.put("late_day", repayInfo.getLateDay());
				valueMap.put("amount_total", repayInfo.getAmountTotal());
				valueMap.put("amount_total_all", repayInfo.getAmountTotalAll());
				valueMap.put("balance_amount", repayInfo.getBalanceAmount());
				valueMap.put("fee_list", repayInfo.getFeeList());
				valueMap.put("is_paypwd", repayInfo.getHasPayPwd());
				String trustDirect = "-1";
				if (isTrust && TrustManager.CHINAPNR.equals(trustType)/* && !"post_provide_payment".equals(repayInfo.getOperateType())*/) {
					trustDirect = "1";
				}
				valueMap.put("trust_direct", trustDirect);
				response.setData(valueMap);
			}
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createErrorJsonResonse(e.getMessage());
		}
	}
	
}