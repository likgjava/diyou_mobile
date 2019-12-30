package com.dy.baf.controller.phone.loan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.custom.RepayInfo;
import com.dy.baf.service.loan.MyLoanDataService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.trust.TrustManager;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.RepayService;

@Controller(value="appMyLoanDataController")
public class MyLoanDataController extends AppBaseController {
	
	@Autowired
	private MyLoanDataService myLoanDataService;
	
	@Autowired
	private RepayService repayService;
	
	@ResponseBody
	@RequestMapping(value="/loan/myLoanData")
	public DyPhoneResponse myLoanData(String xmdy, String diyou) throws Exception{
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String status = paramsMap.get("status");
			String pageNumStr = paramsMap.get("page");
			String epage = paramsMap.get("epage");
			String startTime = paramsMap.get("start_time");
			String endTime = paramsMap.get("end_time");
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("用户登录标识不能为空");
			}
			Page myLoanDataList = myLoanDataService.myLoanDataList(status, getMember(Long.parseLong(login_token)),pageNumStr, startTime, endTime, epage, isTrust);
			return successJsonResonse(new DataConvertUtil(myLoanDataList).setStatus("status_name", optionUtil.getBorrowStatus()).convert());
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	
	@ResponseBody
	@RequestMapping(value="/loan/getrepayINfo")
	public DyPhoneResponse getrepayINfo(String xmdy, String diyou) throws Exception{
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("id");
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("用户登录标识不能为空");
			}
			//借款信息
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id,category_id,category_type,serialno,name,repay_type,repay_type repay_type_name,period,apr,credited_amount");
			queryItem.setWhere(Where.eq("id", id));
			queryItem.getWhere().add(Where.eq("member_id", login_token));
			Map<String, Object> loanMap = getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);
			if(loanMap == null) return errorJsonResonse("您没有权限查看该借款标的还款记录");
			Map<String, Object> repayInfo = myLoanDataService.getrepayINfo(Long.parseLong(id), getMember(Long.parseLong(login_token)), loanMap);
			return successJsonResonse(repayInfo);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 还款信息
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	@ResponseBody
	@RequestMapping(value="/loan/repay/repaydata")
	public DyPhoneResponse getRepayData(String xmdy, String diyou) throws NumberFormatException, Exception {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			Long repayReriodId = Long.parseLong(paramsMap.get("id"));
			String login_token = paramsMap.get("login_token");
			String loanId = paramsMap.get("loan_id");
			String periodNo = paramsMap.get("period_no");
			if (repayReriodId == null || StringUtils.isBlank(loanId) || StringUtils.isBlank(periodNo)) {
				return this.errorJsonResonse("操作失败！");
			}
			MbMember member = getMember(Long.parseLong(login_token));
			if (member == null || member.getId() == null) {
				return this.errorJsonResonse("您未登陆！");
			}
			
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id");
			queryItem.setWhere(Where.eq("status", -1));
			queryItem.setWhere(Where.eq("loan_id", loanId));
			queryItem.setWhere(Where.eq("period_no", Integer.valueOf(periodNo) - 1));
			List<Map> repayList = getListByMap(queryItem, Module.LOAN, Function.LN_REPAYPERIOD);
			if(repayList != null && repayList.size() > 0){
				return this.errorJsonResonse("上一期未还款，请先还上一期");
			}
			
			DyResponse response = repayService.getRepayData(repayReriodId, member.getId());
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
				if (isTrust && TrustManager.CHINAPNR.equals(trustType)/* && !"post_provide_payment".equals(repayInfo.getOperateType()) */) {
					trustDirect = "1";
				}
				valueMap.put("trust_direct", trustDirect);
				response.setData(valueMap);
				
				QueryItem queryUser = new QueryItem();
				queryUser.setWhere(Where.eq("id", login_token));
				this.setSessionAtrribute(Constant.SESSION_USER, this.getOneByEntity(queryUser, Module.MEMBER, Function.MB_MEMBER, MbMember.class));
			}
			return successJsonResonse(response.getData());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.errorJsonResonse(e.getMessage());
		}
	}
	
}