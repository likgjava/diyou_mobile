package com.dy.baf.service.loan;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.custom.RepayInfo;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.trust.TrustManager;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.RepayService;

/**
 * 
 * 
 * @Description: 还款
 * @author 波哥
 * @date 2015年11月15日 上午9:44:57 
 * @version V1.0
 */
@Service("mobileRepayService")
public class RepayMobileService extends MobileService {

	@Autowired
	private RepayService repayService;

	
	/**
	 * 还款数据（单笔）
	 * @param id
	 * @param memberId
	 * @return
	 */
	public DyPhoneResponse getRepayData(String id, String memberId,Boolean isTrust,String trustType) throws Exception{
		Long repayReriodId = Long.parseLong(id);
		if (repayReriodId == null) {
			return this.errorJsonResonse("操作失败！");
		}
		DyResponse response = repayService.getRepayData(repayReriodId, Long.valueOf(memberId));
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
			if (isTrust && TrustManager.CHINAPNR.equals(trustType) && !"post_provide_payment".equals(repayInfo.getOperateType())) {
				trustDirect = "1";
			}
			valueMap.put("trust_direct", trustDirect);
			response.setData(valueMap);
		}
		
		return pcTurnApp(response);
	}
	
	/**
	 * 还款提交
	 * @return
	 */
	public DyPhoneResponse repaySubmit(String memberId,String id,String paypassword,Long ip)throws Exception{
		Long repayPeriodId = Long.parseLong(id);
		if (repayPeriodId == null) {
			return this.errorJsonResonse("参数错误！");
		}
		if (StringUtils.isBlank(paypassword)) {
			return this.errorJsonResonse("支付密码不能为空！");
		}
		//用户信息
		QueryItem mbQueryItem = new QueryItem();
		mbQueryItem.setFields("id,name,paypassword,pwd_attach");
		mbQueryItem.setWhere(Where.eq("id", memberId));
		MbMember memberInfo = this.getOneByEntity(mbQueryItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class);
		String pwd = SecurityUtil.md5(SecurityUtil.sha1(memberInfo.getPwdAttach() + paypassword));
		if (!pwd.equals(memberInfo.getPaypassword())) {
			return this.errorJsonResonse("支付密码错误");
		}
		return pcTurnApp( repayService.repaySubmit(repayPeriodId, Long.valueOf(memberId), ip));
	}
	
	
	/**
	 * 提前还款 还款信息
	 * @return
	 */
	public DyPhoneResponse getRepayAdvanceData(String id,String memberId,Boolean isTrust,String trustType)throws Exception {
		Long loanId = Long.parseLong(id);
		if (loanId == null) {
			return this.errorJsonResonse("操作失败！");
		}
		DyResponse response = repayService.getAdvanceRepayData(loanId, Long.valueOf(memberId));
		if (DyResponse.OK == response.getStatus()) {
			RepayInfo repayInfo = (RepayInfo) response.getData();
			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put("repay_id", repayInfo.getRepayPeriodId());
			valueMap.put("loan_id", repayInfo.getLoanId());
			valueMap.put("loan_name", repayInfo.getLoanName());
			valueMap.put("period_no", repayInfo.getPeriodNo());
			valueMap.put("late_day", repayInfo.getLateDay());
			valueMap.put("principal_total", repayInfo.getPrincipalTotal());
			valueMap.put("amount_total_all", repayInfo.getAmountTotalAll());
			valueMap.put("balance_amount", repayInfo.getBalanceAmount());
			valueMap.put("fee_list", repayInfo.getFeeList());
			valueMap.put("repay_list", repayInfo.getRepayPeriodList());
			valueMap.put("is_paypwd", repayInfo.getHasPayPwd());
			valueMap.put("trust_direct", (isTrust && TrustManager.CHINAPNR.equals(trustType)) ? "1" : "-1");
			response.setData(valueMap);
		}
		return pcTurnApp(response);
	}
	/**
	 * 还款提交(提前还款)
	 * @return
	 */
	public DyPhoneResponse repayAdvanceSubmit(String id,String memberId,String paypassword,Long ip) throws Exception{
		Long loanId = Long.parseLong(id);
		if (loanId == null) {
			return this.errorJsonResonse("参数错误！");
		}
		if (StringUtils.isBlank(paypassword)) {
			return this.errorJsonResonse("支付密码不能为空！");
		}
		//用户信息
		QueryItem mbQueryItem = new QueryItem();
		mbQueryItem.setFields("id,name,paypassword,pwd_attach");
		mbQueryItem.setWhere(Where.eq("id", memberId));
		MbMember memberInfo = this.getOneByEntity(mbQueryItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class);
		String pwd = SecurityUtil.md5(SecurityUtil.sha1(memberInfo.getPwdAttach() + paypassword));
		if (!pwd.equals(memberInfo.getPaypassword())) {
			return this.errorJsonResonse("支付密码错误");
		}
		return pcTurnApp(repayService.repayAdvanceSubmit(loanId, Long.valueOf(memberId), ip));

	}
}
