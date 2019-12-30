package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
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

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.loan.LoanMobileService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.utils.Constant;
import com.dy.core.utils.IpUtil;

/**
 * 
 * 
 * @Description: 借款
 * @author 波哥
 * @date 2015年9月8日 下午6:06:00
 * @version V1.0
 */
@Controller(value = "appLoanController")
public class LoanController extends AppBaseController {

	@Autowired
	private LoanMobileService loanService;

	/**
	 * 我的借款--统计
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/loan/myloandata", method = RequestMethod.POST)
	public DyResponse getMyLoanData() {
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if (member == null || member.getId() == null) {
				return this.createErrorJsonResonse("您未登陆！");
			}
			StringBuffer fields = new StringBuffer();
			fields.append("loan_member_id,");
			fields.append("sum(amount) principal_total_all,"); // 借款总额
			fields.append("sum(case when recover_status!=1 then recover_principal else 0 end) nr_recover_principal,"); // 未回款应回款总本金
			fields.append("sum(case when recover_status!=1 then recover_interest else 0 end) nr_recover_interest,"); // 未回款应回款总利息
			fields.append(
					"sum(case when recover_status!=1 then recover_principal_yes else 0 end) nr_recover_principal_yes,");// 未回款已收本金
			fields.append(
					"sum(case when recover_status!=1 then recover_interest_yes else 0 end) nr_interest_yes_total,");// 未回款已收利息
			fields.append("sum(recover_interest_yes) interest_yes_total,");// 已收利息
			fields.append("sum(award_amount) award_amount_yes");// 已收奖金
			QueryItem queryItem = new QueryItem("loan", "tender");
			queryItem.setFields(fields.toString());
			List<Where> whereList = new ArrayList<Where>();
			whereList.add(Where.eq("status", 1));
			whereList.add(Where.eq("loan_member_id", member.getId()));
			queryItem.setWhere(whereList);
			// queryItem.getWhere().add(Where.eq("loan_member_id",
			// member.getId()));
			queryItem.setGroup("loan_member_id");
			Map<String, Object> valueMap = (Map<String, Object>) this.getOne(queryItem);
			if (valueMap != null) {
				BigDecimal recoverPrincipal = valueMap.get("nr_recover_principal") != null
						? (BigDecimal) valueMap.get("nr_recover_principal") : BigDecimal.ZERO;
				BigDecimal recoverInterest = valueMap.get("nr_recover_interest") != null
						? (BigDecimal) valueMap.get("nr_recover_interest") : BigDecimal.ZERO;
				BigDecimal recoverPrincipalYes = valueMap.get("nr_recover_principal_yes") != null
						? (BigDecimal) valueMap.get("nr_recover_principal_yes") : BigDecimal.ZERO;
				BigDecimal recoverInterestYes = valueMap.get("nr_interest_yes_total") != null
						? (BigDecimal) valueMap.get("nr_interest_yes_total") : BigDecimal.ZERO;
				BigDecimal principalWaitTotal = recoverPrincipal.subtract(recoverPrincipalYes);
				BigDecimal interestWaitTotal = recoverInterest.subtract(recoverInterestYes);
				valueMap.put("principal_wait_total", principalWaitTotal);// 待还本金
				valueMap.put("interest_wait_total", interestWaitTotal);// 待还利息
			} else {
				valueMap = new HashMap<String, Object>();
				valueMap.put("principal_total_all", 0); // 借款总额
				valueMap.put("recover_principal", 0);// 应回款总本金
				valueMap.put("recover_interest", 0);// 应回款总利息
				valueMap.put("recover_principal_yes", 0);// 已收本金
				valueMap.put("interest_yes_total", 0); // 已收利息
				valueMap.put("award_amount_yes", 0);// 已收奖金
				valueMap.put("principal_wait_total", 0);// 待还本金
				valueMap.put("interest_wait_total", 0);// 待还利息
			}
			return this.createSuccessJsonResonse(valueMap);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createErrorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 我的借款--列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/myLoanList")
	public DyPhoneResponse getMyLoanList(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String pageNumStr = paramsMap.get("page");
			String status = paramsMap.get("status");
			String login_token = paramsMap.get("login_token");
			return this.loanService.getMyLoanList(login_token, pageNumStr, status, null, null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款标种类型列表
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanCategoryList")
	public DyPhoneResponse loanCategoryList(String xmdy, String diyou) {
		try {
			return this.loanService.loanCategoryList();
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 还款类型列表
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/getRepayTypeList")
	public DyPhoneResponse getRepayTypeList(String xmdy, String diyou) {
		try {
			return this.loanService.getRepayTypeList();
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款标种详情
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanCategoryInfo")
	public DyPhoneResponse loanCategoryInfo(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.loanService.loanCategoryInfo(id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款协议
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanProtocolData")
	public DyPhoneResponse loanProtocolData(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			return this.loanService.loanProtocolData();
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款申请第二步
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanSecond")
	public DyPhoneResponse loanSecond(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.loanService.loanSecond(id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款申请第三步(获取担保公司)
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanThird")
	public DyPhoneResponse loanThird(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.loanService.loanThird(id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款信息提交
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanSub")
	public DyPhoneResponse loanSub(String xmdy, String diyou) {
		try {

			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			return this.loanService.loanSub(paramsMap, isTrust, IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 我的借款详情
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/myLoanInfo")
	public DyPhoneResponse myLoanInfo(String xmdy, String diyou) {
		try {

			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.loanService.myLoanInfo(id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 借款标详情
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanInfo")
	public DyPhoneResponse loanInfo(String xmdy, String diyou) {
		try {

			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String loan_id = paramsMap.get("loan_id");
			return this.loanService.loanInfo(loan_id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 标撤销
	 * 
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("loan/loanCancel")
	public DyPhoneResponse loanCancel(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.loanService.loanCancel(Long.valueOf(id));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 图片上传
	 * 
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/loan/uploadFile")
	public DyPhoneResponse upload(String xmdy, String diyou, HttpServletRequest request) {
		try {
			return this.loanService.upload(request);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}

	}
}