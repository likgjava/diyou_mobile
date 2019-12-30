package com.dy.baf.controller.phone.finance;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.common.FnAccountRecharge;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.payment.BaoFoo;
import com.dy.core.payment.Ecpss;
import com.dy.core.payment.Heepay;
import com.dy.core.payment.PaymentConfig;
import com.dy.core.payment.SumaPay;
import com.dy.core.payment.Techsp;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.serializer.SerializerUtil;
import com.dy.httpinvoker.FinanceService;

/**
 * 第三方支付
 */
@Controller
public class PaymentController extends FrontBaseController {
	/**
	 * 汇付宝
	 */
	public static final String HEEPAY = "heepay";
	/**
	 * 宝付
	 */
	public static final String BAOFOO = "baofoo";
	/**
	 * 丰付
	 */
	public static final String SUMAPAY = "sumapay";
	/**
	 * 汇潮
	 */
	public static final String ECPSS = "ecpss";
	/**
	 * 通联
	 */
	public static final String TECHSP = "techsp";
	
	public static final String RESULT_OK = "Ok";
	public static final String RESULT_ERROR_SIGN = "Sign error";
	
	@Autowired
	private FinanceService financeService;
	
	/**
	 * 充值(生成第三方支付提交表单，并插入充值记录)
	 * @param request
	 * @param amount 操作金额
	 * @param paymentType 第三方支付类型
	 * @param memberId 用户ID
	 * @param valicode 验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/finance/recharge/recharge")
	public DyResponse recharge(HttpServletRequest request, BigDecimal amount, String paymentType, String valicode) throws Exception {
		Map<String, String> errorMsg = new HashMap<String, String>();
		//Validate parameter
		if(paymentType == null) return createErrorJsonResonse(getMessage("payment.type.null"));
		if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			errorMsg.put("code", "account");
			errorMsg.put("msg", getMessage("payment.amount.null"));
			return createErrorJsonResonse(errorMsg);
		}
		
		//Validate verify code
		String sessionValidateCode = getSessionAttribute(Constant.SESSION_VERIFY_CODE).toString();
		if(!sessionValidateCode.equalsIgnoreCase(valicode)) {
			errorMsg.put("code", "valicode");
			errorMsg.put("msg", getMessage("payment.validatecode.error"));
			return createErrorJsonResonse(errorMsg);
		}

		//Get system payment config from DB
		PaymentConfig paymentConfig = getSystemPaymentConfig(paymentType);
		if(paymentConfig == null || StringUtils.isEmpty(paymentConfig.getAccount()) || StringUtils.isEmpty(paymentConfig.getPassword()))
			return createErrorJsonResonse(getMessage("payment.account.error", new String[]{getMessage("payment.type." + paymentType)}));
		
		String ip = getIp();
		MbMember member = getMember();//用户
		String account = paymentConfig.getAccount();//商务号
		String password = paymentConfig.getPassword();//密钥
		String basePath = getSystemBasePath(request);//系统根路径
		String ind = com.dy.core.utils.StringUtils.genenrateUniqueInd();//唯一订单号
		
		//Get request parameter
		String payUrl = null;
		Map<String, Object> paramMap = null;
		if(HEEPAY.equals(paymentType)) {
			payUrl = Heepay.PAY_URL;
			paramMap = Heepay.builRequestMap(basePath, account, password, amount, ind, ip);
		} else if(BAOFOO.equals(paymentType)) {
			payUrl = BaoFoo.PAY_URL;
			paramMap = BaoFoo.builRequestMap(basePath, account, password, paymentConfig.getTerminalId(), amount, ind);
		} else if(SUMAPAY.equals(paymentType)) {
			payUrl = SumaPay.PAY_URL;
			paramMap = SumaPay.builRequestMap(basePath, account, password, amount, ind);
		} else if(ECPSS.equals(paymentType)) {
			payUrl = Ecpss.PAY_URL;
			paramMap = Ecpss.builRequestMap(basePath, account, password, amount, ind);
		} else if(TECHSP.equals(paymentType)) {
			payUrl = Techsp.PAY_URL;
			paramMap = Techsp.builRequestMap(basePath, account, password, amount, ind);
		} else {
			return createErrorJsonResonse(getMessage("payment.type.error"));
		}
		if(paramMap == null) return createErrorJsonResonse(getMessage("payment.url.error"));
		
		//Build form
		String strForm = buildForm(payUrl, paramMap);
		
		//Add recharge log
		addRechargeLog(paymentConfig, member, paymentType, amount, ind, ip);
		
		return createSuccessJsonResonse(strForm);
	}
	
	/**
	 * 汇付宝回调地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Heepay.URL_RETURN)
	public String heepayReturn(HttpServletRequest request) {
		return validateNotifySign(getRequestMap(request), HEEPAY) ? RESULT_OK : RESULT_ERROR_SIGN;
	}
	

	/**
	 * 汇付宝通知地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Heepay.URL_NOTIFY)
	public String heepayNotify(HttpServletRequest request) {
		Map<String, Object> paramMap = getRequestMap(request);
		if(!validateNotifySign(paramMap, HEEPAY)) return RESULT_ERROR_SIGN;
		
		String result = paramMap.get("result") == null ? "" : paramMap.get("result").toString();
		String ind = paramMap.get("agent_bill_id") == null ? "" : paramMap.get("agent_bill_id").toString();
		
		//Update recharge log
		return updateRechargeLog(result.equals("1") ? 1 : -1, ind);
	}
	
	/**
	 * 宝付回调地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=BaoFoo.URL_RETURN)
	public String baofooReturn(HttpServletRequest request) {
		return validateNotifySign(getRequestMap(request), BAOFOO) ? RESULT_OK : RESULT_ERROR_SIGN;
	}
	
	/**
	 * 宝付通知地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=BaoFoo.URL_NOTIFY)
	public String baofooNotify(HttpServletRequest request) {
		Map<String, Object> paramMap = getRequestMap(request);
		if(!validateNotifySign(paramMap, BAOFOO)) return RESULT_ERROR_SIGN;
		
		String ind = paramMap.get("TransID") == null ? "" : paramMap.get("TransID").toString();
		String result = paramMap.get("Result") == null ? "" : paramMap.get("Result").toString();
		
		//Update recharge log
		return updateRechargeLog(result.equals("1") ? 1 : -1, ind);
	}
	
	/**
	 * 丰付回调地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=SumaPay.URL_RETURN)
	public String sumapayReturn(HttpServletRequest request) {
		return validateNotifySign(getRequestMap(request), SUMAPAY) ? RESULT_OK : RESULT_ERROR_SIGN;
	}
	
	/**
	 * 丰付通知地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=SumaPay.URL_NOTIFY)
	public String sumapayNotify(HttpServletRequest request) {
		Map<String, Object> paramMap = getRequestMap(request);
		if(!validateNotifySign(paramMap, SUMAPAY)) return RESULT_ERROR_SIGN;
		
		String result = paramMap.get("status") == null ? "" : paramMap.get("status").toString();
		String ind = paramMap.get("requestId") == null ? "" : paramMap.get("requestId").toString();
		
		//Update recharge log
		return updateRechargeLog(result.equals("2") ? 1 : -1, ind);
	}
	
	/**
	 * 汇潮回调地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Ecpss.URL_RETURN)
	public String ecpssReturn(HttpServletRequest request) {
		return validateNotifySign(getRequestMap(request), ECPSS) ? RESULT_OK : RESULT_ERROR_SIGN;
	}
	
	/**
	 * 汇潮通知地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Ecpss.URL_NOTIFY)
	public String ecpssNotify(HttpServletRequest request) {
		Map<String, Object> paramMap = getRequestMap(request);
		if(!validateNotifySign(paramMap, ECPSS)) return RESULT_ERROR_SIGN;
		
		String ind = paramMap.get("BillNo") == null ? "" : paramMap.get("BillNo").toString();
		String result = paramMap.get("Succeed") == null ? "" : paramMap.get("Succeed").toString();
		
		//Update recharge log
		return updateRechargeLog(result.equals("88") ? 1 : -1, ind);
	}
	
	/**
	 * 通联回调地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Techsp.URL_RETURN)
	public String techspReturn(HttpServletRequest request) {
		return validateNotifySign(getRequestMap(request), TECHSP) ? RESULT_OK : RESULT_ERROR_SIGN;
	}
	
	/**
	 * 通联通知地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value=Techsp.URL_NOTIFY)
	public String techspNotify(HttpServletRequest request) {
		Map<String, Object> paramMap = getRequestMap(request);
		if(!validateNotifySign(paramMap, TECHSP)) return RESULT_ERROR_SIGN;
		
		String ind = paramMap.get("orderNo") == null ? "" : paramMap.get("orderNo").toString();
		String result = paramMap.get("payResult") == null ? "" : paramMap.get("payResult").toString();
		
		//Update recharge log
		return updateRechargeLog(result.equals("1") ? 1 : -1, ind);
	}
	
	/**
	 * 更新充值记录
	 * @param status 状态 1：成功 -1：失败
	 * @param ind 充值订单号
	 * @return
	 */
	private String updateRechargeLog(Integer status, String ind) {
		int result = financeService.recharge(null, null, null, null, ind, status, IpUtil.ipStrToLong(getIp()));
		
		return result <= 0 ? "Update recharge log fail" : "Ok";
	}
	
	/**
	 * 验证返回结果
	 * @param paramMap
	 * @param paymentType
	 * @return
	 */
	private boolean validateNotifySign(Map<String, Object> paramMap, String paymentType) {
		String key = getKey(paymentType);
		
		boolean valiResult = false;
		if(HEEPAY.equals(paymentType)) {
			valiResult = Heepay.validateNotifySign(key, paramMap);
		} else if(BAOFOO.equals(paymentType)) {
			valiResult = BaoFoo.validateNotifySign(key, paramMap);
		} else if(SUMAPAY.equals(paymentType)) {
			valiResult = SumaPay.validateNotifySign(key, paramMap);
		} else if(ECPSS.equals(paymentType)) {
			valiResult = Ecpss.validateNotifySign(key, paramMap);
		} else if(TECHSP.equals(paymentType)) {
			valiResult = Techsp.validateNotifySign(key, paramMap);
		}
		
		return valiResult;
	}
	
	/**
	 * 构建表单
	 * @param payUrl 
	 * @param paramMap
	 */
	private String buildForm(String payUrl, Map<String, Object> paramMap) throws Exception {
		StringBuffer strForm = new StringBuffer();
		strForm.append("<form name='applyForm' id='applyForm' target='_blank' method='post' action='" + payUrl + "'>");
		for(String key : paramMap.keySet()) {
			strForm.append("<input name='" + key + "' type='hidden' value=\"" + paramMap.get(key) + "\"/>");
		}
		strForm.append("</form>");
		strForm.append("<script>document.forms['applyForm'].submit();</script>");
		
		return strForm.toString();
	}
	
	/**
	 * 添加充值记录
	 * @param paymentConfig
	 * @param member
	 * @param paymentType
	 * @param amount
	 * @param ind
	 * @param ip
	 */
	private void addRechargeLog(PaymentConfig paymentConfig, MbMember member, String paymentType, BigDecimal amount, String ind, String ip) throws Exception {
		Map<String, BigDecimal> feeMap = getRechargeFee(paymentType, amount);
		
		FnAccountRecharge  recharge = new FnAccountRecharge();
		recharge.setInd(ind);
		recharge.setMemberId(member.getId());
		recharge.setMemberName(member.getName());
		recharge.setAmount(amount);
		recharge.setAmountIncome(feeMap == null ? BigDecimal.ZERO : feeMap.get("rechargeAmount"));
		recharge.setFee(feeMap == null ? BigDecimal.ZERO : feeMap.get("rechargeFee"));
		recharge.setPaymentNid(paymentType);
		recharge.setType("online");
		recharge.setAddTime(DateUtil.getCurrentTime());
		recharge.setAddIp(IpUtil.ipStrToLong(ip));
		recharge.setStatus(-2);//待审核
		
		insert(Module.FINANCE, Function.FN_RECHARGE, recharge);
	}
	
	/**
	 * 计算充值手续费
	 * @param paymentType
	 * @param amount
	 * @return
	 */
	private Map<String, BigDecimal> getRechargeFee(String paymentType, BigDecimal amount) {
		PaymentConfig paymentConfig = getSystemPaymentConfig(paymentType);
		if(paymentConfig == null || StringUtils.isEmpty(paymentConfig.getAccount())) {
			return null;
		}
		
		BigDecimal rechargeFee = BigDecimal.ZERO;
		if(!"free".equals(paymentConfig.getFeeType())) {
			paymentConfig.setAllInnerMoney(paymentConfig.getAllInnerMoney() == null ? BigDecimal.ZERO : paymentConfig.getAllInnerMoney());
			paymentConfig.setAllInnerMoneyFee(paymentConfig.getAllInnerMoneyFee() == null ? BigDecimal.ZERO : paymentConfig.getAllInnerMoneyFee());
			paymentConfig.setAllEveryOverMoney(paymentConfig.getAllEveryOverMoney() == null ? BigDecimal.ZERO : paymentConfig.getAllEveryOverMoney());
			
			if(amount.compareTo(paymentConfig.getAllInnerMoney()) <= 0) 
				rechargeFee = paymentConfig.getAllInnerMoneyFee();
			else
				rechargeFee = NumberUtils.mul(amount, NumberUtils.div(paymentConfig.getAllEveryOverMoney(), new BigDecimal("100")));
		}
		
		Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
		result.put("rechargeFee", rechargeFee);
		result.put("rechargeAmount", NumberUtils.sub(amount, rechargeFee));
		
		return result;
	}
	
	/**
	 * 获取当前登陆用户信息
	 * @return
	 */
	private MbMember getMember() {
		Object obj = getSessionAttribute(Constant.SESSION_USER);
		
		return obj == null ? null : (MbMember) obj;
	}
	
	/**
	 * 生成唯一订单号(yyyyMMddHHmmssSSS+3位随机数+memberId)
	 * @return
	 */
	@SuppressWarnings("unused")
	private String generateUniqueInd(Long memberId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String ind = dateFormat.format(new Date());
		for(int j=0; j<3; j++) ind += RandomUtils.nextInt(9);
		
		return ind + memberId;
	}
	
	/**
	 * 获取用户登陆IP
	 * @return
	 */
	private String getIp() {
		String ip = null;
		try {
			ip = getRemoteIp();
		} catch (Exception e) {
		}
		if(StringUtils.isEmpty(ip)) ip = "127.0.0.1";
		
		return ip;
	}
	
	/**
	 * 获取系统根路径
	 * @param request
	 * @return
	 */
	private String getSystemBasePath(HttpServletRequest request) {
		StringBuffer basePath = new StringBuffer();
		basePath.append(request.getScheme())
				.append("://")
				.append(request.getServerName())
				.append(":")
				.append(request.getServerPort() == 80 ? "" : request.getServerPort())
				.append(request.getContextPath());
		return basePath.toString();
	}
	
	/**
	 * 获取密钥
	 * @param paymentType
	 * @return
	 */
	private String getKey(String paymentType) {
		PaymentConfig paymentConfig = getSystemPaymentConfig(paymentType);
		
		return paymentConfig == null ? "" : paymentConfig.getPassword();
	}
	
	/**
	 * 获取第三方支付配置信息
	 * @param paymentType 第三方支付类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private PaymentConfig getSystemPaymentConfig(String paymentType) {
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("config,fee_type,all_inner_money,all_inner_money_fee,all_every_over_money");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("status", 1));
		whereList.add(new Where("nid", paymentType));
		queryItem.setWhere(whereList);
		
		PaymentConfig paymentConfig = null;
		try {
			paymentConfig = getOneByEntity(queryItem, Module.FINANCE, Function.FN_PAYMENT, PaymentConfig.class);
			if(paymentConfig == null || StringUtils.isEmpty(paymentConfig.getConfig())) return null;
			
			Map configMap = (Map) new SerializerUtil().unserialize(paymentConfig.getConfig().getBytes());
			paymentConfig.setAccount(configMap.get("Account") == null ? "" : configMap.get("Account").toString());
			paymentConfig.setPassword(configMap.get("password") == null ? "" : configMap.get("password").toString());
			paymentConfig.setTerminalId(configMap.get("merAcct") == null ? "" : configMap.get("merAcct").toString());
		} catch (Exception e) {
		}
		
		return paymentConfig;
	}
	
	/**
	 * Controller不绑定参数，从HttpServletRequest获取提交的所有参数，默认情况下String类型的数据会以String[]形式提交
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getRequestMap(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Map<String, Object> paramMap = request.getParameterMap();
		for(String key : paramMap.keySet()) {
			Object value = paramMap.get(key);
			if(value == null) continue;
			
			if(value instanceof String[]) {
				String[] valueArray = (String[]) value;
				
				if(valueArray != null && valueArray.length == 1)
					result.put(key, valueArray[0]);
				else
					result.put(key, value);
			} else {
				result.put(key, value);
			}
		}
		return result;
	}
}