package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnAccountPaymentConfig;
import com.dy.baf.entity.common.FnAccountRecharge;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.payment.PaymentConfig;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;
import com.dy.core.utils.serializer.SerializerUtil;

/**
 * 
 * 
 * @Description: 充值
 * @author 波哥
 * @date 2015年9月10日 下午9:31:23 
 * @version V1.0
 */
@Service("mobileRechargeService")
public class RechargeService extends MobileService {
	
	@Autowired
	private BaseService baseService;
	
	/**
	 * 支付方式列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getlist(String page,String memberId) throws Exception {
		if(StringUtils.isBlank(memberId))return errorJsonResonse("用户标识不能为空");
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		String isTrust = PropertiesUtil.getProperty("trust.enable");
		
		if("0".equals(isTrust)) {
			//非托管版本才做以下校验
			if(user.getIsRealname() !=1)return errorJsonResonse("请先进行实名认证");
			if(user.getIsPhone() != 1)return errorJsonResonse("请先进行手机认证");
			if(user.getIsEmail() != 1)return errorJsonResonse("请先进行邮箱认证");
			if(user.getPaypassword() == null)return errorJsonResonse("请先设置支付密码");
		}else{
			
			
		}
		
		QueryItem payItem = new QueryItem(Module.FINANCE,Function.FN_PAYMENT);
		payItem.setPage(page == null ? 1 : Integer.valueOf(page));
		payItem.setWhere(new Where("status", 1));
		payItem.setFields("id,name,nid,thumbs");
		payItem.setOrders("sort_index desc");
		payItem.setLimit(10);
		List<Where> whereList = new ArrayList<Where>();

		Page pageObj = (Page)this.baseService.getPage(payItem, Map.class);
		if (pageObj.getItems() != null && pageObj.getItems().size() > 0){
			pageObj.setParams(true);
			List<Map> list = pageObj.getItems();
			if("1".equals(isTrust)) {
				for (Map map : list) {
					map.put("trust_account", user.getTrustAccount());
				}
			}
		}
		return successJsonResonse(pageObj);
	}
	
	/**
	 * 充值手续费计算
	 */
	public DyPhoneResponse rechargefee(BigDecimal amount,String payment_nid,String memberId) throws Exception {
		if (StringUtils.isBlank(memberId))
			return errorJsonResonse("用户标识不能为空");

		PaymentConfig paymentConfig = getSystemPaymentConfig(payment_nid);
		if (paymentConfig == null || StringUtils.isBlank(paymentConfig.getAccount())) {
			return null;
		}

		BigDecimal rechargeFee = BigDecimal.ZERO;
		if (!"free".equals(paymentConfig.getFeeType())) {
			paymentConfig.setAllInnerMoney(
					paymentConfig.getAllInnerMoney() == null ? BigDecimal.ZERO : paymentConfig.getAllInnerMoney());
			paymentConfig.setAllInnerMoneyFee(paymentConfig.getAllInnerMoneyFee() == null ? BigDecimal.ZERO
					: paymentConfig.getAllInnerMoneyFee());
			paymentConfig.setAllEveryOverMoney(paymentConfig.getAllEveryOverMoney() == null ? BigDecimal.ZERO
					: paymentConfig.getAllEveryOverMoney());

			if (amount.compareTo(paymentConfig.getAllInnerMoney()) <= 0)
				rechargeFee = paymentConfig.getAllInnerMoneyFee();
			else
				rechargeFee = NumberUtils.mul(amount,
						NumberUtils.div(paymentConfig.getAllEveryOverMoney(), new BigDecimal("100")));
		}

		Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
		result.put("fee", rechargeFee.setScale(2,BigDecimal.ROUND_HALF_UP));
		result.put("income", NumberUtils.sub(amount, rechargeFee).setScale(2,BigDecimal.ROUND_HALF_UP));
		return successJsonResonse(result);
		
	}
	
	/**
	 * 充值
	 * @param query_type
	 * @param type
	 * @param payment
	 * @param amount
	 * @param valicode
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse recharge(String query_type,String type,String payment,BigDecimal amount,String valicode,String memberId) throws Exception {
		if(StringUtils.isBlank(memberId))return errorJsonResonse("用户标识不能为空");
		
		//判断是否实名认证，手机验证，是否设置支付密码
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		if(user.getIsRealname() !=1)return errorJsonResonse("请先进行实名认证");
		if(user.getIsPhone() !=1)return errorJsonResonse("请先绑定手机");
		if("".equals(user.getPaypassword()))return errorJsonResonse("请先设置支付密码");
		
		//判断金额是否正确
		if(amount==null || amount.compareTo(new BigDecimal(0))<=0)return errorJsonResonse("金额输入错误");
		//插入充值信息
		FnAccountRecharge  recharge = new FnAccountRecharge();
		recharge.setInd(StringUtils.genenrateUniqueInd());
		recharge.setMemberId(user.getId());
		recharge.setMemberName(user.getName());
		recharge.setAmount(amount);
		BigDecimal fee = this.getFee(amount,payment,user.getId());
		recharge.setAmountIncome(amount.subtract(fee));
		recharge.setFee(fee);
		recharge.setPaymentNid(payment);
		recharge.setType("online");
		recharge.setAddTime(DateUtil.getCurrentTime());
		recharge.setAddIp(IpUtil.ipStrToLong(GetUtils.getRemoteIp()));
		recharge.setStatus(-2);
		this.baseService.insert(Module.FINANCE, "recharge", recharge);
		//跳转至第三方
		String form = this.getForm(payment,amount,recharge.getInd());
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> desMap = new HashMap<String,Object>();
		desMap.put("code", true);
		desMap.put("message", form);
		map.put("status", 200);
		map.put("description", desMap);
		return successJsonResonse(map);
	}
	
	
	/**
	 * 充值记录列表
	 */
	public Page rechargelog(Integer page,Integer status,String memberId) throws Exception {
MbMember user = this.getMbMember(Long.valueOf(memberId));
		
		//充值记录
		QueryItem rechargeItem = new QueryItem(Module.FINANCE,Function.FN_RECHARGE);
		rechargeItem.setFields("ind,payment_nid,type,type type_name,amount,add_time,status,status status_name,verify_remark");
		rechargeItem.setPage(page == null ? 1 : page);
		rechargeItem.setOrders("add_time desc");
		rechargeItem.setLimit(10);
		if(status !=null){
			rechargeItem.setWhere(Where.eq("status",status));
		}
		rechargeItem.setWhere(Where.eq("member_id",user.getId()));
		List<FnAccountRecharge> rechargeList = (List<FnAccountRecharge>) this.baseService.getList(rechargeItem, FnAccountRecharge.class);
		BigDecimal amountTotal = new BigDecimal(0);
		for(FnAccountRecharge recharge:rechargeList){
			amountTotal = amountTotal.add(recharge.getAmount());
		}
		
		Page pageObj = (Page)this.baseService.getPage(rechargeItem, Map.class);
		List<Map> map = pageObj.getItems();
		for(Map newmap:map){
			newmap.put("payment_name", getPayName(newmap.get("payment_nid").toString()));
		}
		pageObj.setItems(map);
		pageObj.setParams(amountTotal);
		return pageObj;
	}
	
	/**
	 * 充值资金总额
	 */
	public DyPhoneResponse rechargecount(String memberId) throws Exception {
		if(StringUtils.isBlank(memberId))return errorJsonResonse("用户标识不能为空");
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		//充值记录
		QueryItem rechargeItem = new QueryItem(Module.FINANCE,Function.FN_RECHARGE);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id",user.getId()));
		whereList.add(new Where("status",1));
		rechargeItem.setWhere(whereList);
		List<FnAccountRecharge> rechargeList = (List<FnAccountRecharge>) this.baseService.getList(rechargeItem, FnAccountRecharge.class);
		BigDecimal amountTotal = new BigDecimal(0);
		for(FnAccountRecharge recharge:rechargeList){
			amountTotal = amountTotal.add(recharge.getAmount());
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("amount_total", amountTotal);
		return successJsonResonse(map);
	}
	
	/**
	 * 充值费用工具
	 * @throws Exception 
	 */
	public BigDecimal getFee(BigDecimal account,String payment_nid,Long memberId) throws Exception{
		//获取所选择的支付方式
		QueryItem payItem = new QueryItem(Module.FINANCE,Function.FN_RECHARGE);
		payItem.getWhere().add(Where.eq("nid", payment_nid));
		FnAccountPaymentConfig paymentConfig = (FnAccountPaymentConfig) this.baseService.getOne(payItem, FnAccountPaymentConfig.class);
		
		MbMember user = this.getMbMember(memberId);
		BigDecimal vip_inner_money_fee = paymentConfig.getVipInnerMoneyFee();//vip在多少金额内手续费
		BigDecimal vip_every_over_money = paymentConfig.getVipEveryOverMoney();//vip超过基础费用每超过多少钱
		BigDecimal vip_proportion = paymentConfig.getVipProportion();//vip手续费比例
		BigDecimal vip_everyday_money_fee_max = paymentConfig.getVipEverydayMoneyFeeMax();//vip每天总手续费最大金额
		BigDecimal vip_inner_money = paymentConfig.getVipInnerMoney();//vip在多少金额内
		BigDecimal vip_every_over_money_fee = paymentConfig.getVipEveryOverMoneyFee();//vip超过基础费用每超过对应的费用
		BigDecimal all_everyday_money_fee_max = paymentConfig.getAllEverydayMoneyFeeMax();//会员每天总手续费最大金额
		BigDecimal all_inner_money_fee = paymentConfig.getAllInnerMoneyFee();//会员在多少金额内手续费
		BigDecimal all_proportion = paymentConfig.getAllProportion();//除vip外其他手续费比例
		BigDecimal all_every_over_money_fee =paymentConfig.getAllEveryOverMoneyFee();//vip超过基础费用每超过对应的费用
		BigDecimal all_every_over_money = paymentConfig.getAllEveryOverMoney();//会员超过基础费用每超过多少钱
		BigDecimal all_inner_money = paymentConfig.getAllInnerMoney();//会员在多少金额内
		String fee_type = paymentConfig.getFeeType();//手续费类型 按金额收费money  按比例scale收费 免费free
		//判断当前用户是否vip
		BigDecimal scale;//比例
		BigDecimal inner_money;//固定金额内
		BigDecimal inner_money_fee;//固定金额内费用
		BigDecimal every_over_money;//每超过金额
		BigDecimal every_over_money_fee;//每超过every_over_money金额扣去费用
		BigDecimal everyday_money_fee_max;//当笔扣除最大费用
		if(user.getIsVip() == 1){
			scale = vip_proportion;
			inner_money = vip_inner_money;
			inner_money_fee = vip_inner_money_fee;
			every_over_money = vip_every_over_money;
			every_over_money_fee = vip_every_over_money_fee;
			everyday_money_fee_max = vip_everyday_money_fee_max;
		}else{
			scale = all_proportion;
			inner_money = all_inner_money;
			inner_money_fee = all_inner_money_fee;
			every_over_money = all_every_over_money;
			every_over_money_fee = all_every_over_money_fee;
			everyday_money_fee_max = all_everyday_money_fee_max;
		}
		BigDecimal feeAmount = null;//充值费用
		//判断扣费方式
		if("free".equals(fee_type)){//免费
			feeAmount = new BigDecimal(0);
		}else if("scale".equals(fee_type)){//按比例金额扣费
			feeAmount = account.multiply(scale).multiply(new BigDecimal(0.01));
		}else if("money".equals(fee_type)){//按金额扣费
			if(account.compareTo(inner_money)<=0){//小于固定金额情况
				feeAmount = inner_money_fee;
			}else{//大于固定金额
				BigDecimal moreAmount = account.subtract(inner_money);//超出的金额
				int times =moreAmount.divideToIntegralValue(every_over_money).intValue();//超过的次数
				BigDecimal fee = every_over_money_fee.multiply(new BigDecimal(times));//超出金额的提现费用
				//判断费用是否大于最大充值费用
				if(fee.compareTo(everyday_money_fee_max)>0){
					feeAmount = everyday_money_fee_max;
				}else{
					feeAmount = fee.add(inner_money_fee);
				}
			}
		}
		BigDecimal fee =  feeAmount.setScale(2,BigDecimal.ROUND_HALF_UP);
		return fee;
	}
	
	/**
	 * 构造第三方接口表单
	 * @throws Exception 	
	 */
	public String getForm(String payment,BigDecimal amount,String ind) throws Exception{
		//获取所选择的支付方式
		QueryItem payItem = new QueryItem(Module.FINANCE, Function.FN_PAYMENT);
		payItem.getWhere().add(Where.eq("nid", payment));
		FnAccountPaymentConfig paymentConfig = (FnAccountPaymentConfig) this.baseService.getOne(payItem, FnAccountPaymentConfig.class);
		String config = paymentConfig.getConfig();
		SerializerUtil serializerUtil = new SerializerUtil();
		Map configMap = (Map)serializerUtil.unserialize(config.getBytes());
		if("weibopay".equals(payment)){
			WeiboPay weiboPay = new WeiboPay();
			String form = weiboPay.getParams(configMap,amount,ind);
			return form;
		}else if("sumapay".equals(payment)){
			SumaPay sumaPay = new SumaPay();
			String form = sumaPay.getParams(configMap,amount,ind);
			return form;
		}
		return null;
	}
	
	/**
	 * 根据nid获取支付方式
	 * @throws Exception 
	 */
	private String getPayName(String nid) throws Exception{
		if("admin".equals(nid))return "后台充值";
		QueryItem queryItem  = new QueryItem(Module.FINANCE, Function.FN_PAYMENT);
		queryItem.setFields("nid,name");
		queryItem.getWhere().add(Where.eq("nid", nid));
		FnAccountPaymentConfig payment = this.baseService.getOne(queryItem,  FnAccountPaymentConfig.class);
		return payment.getName();
	}
	
	
	/**
	 * 获取第三方支付配置信息
	 * @param paymentType 第三方支付类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private PaymentConfig getSystemPaymentConfig(String paymentType) {
		QueryItem queryItem = new QueryItem( Module.FINANCE, Function.FN_PAYMENT);
		queryItem.setFields("config,fee_type,all_inner_money,all_inner_money_fee,all_every_over_money");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("status", 1));
		whereList.add(new Where("nid", paymentType));
		queryItem.setWhere(whereList);
		
		PaymentConfig paymentConfig = null;
		try {
			paymentConfig = this.baseService.getOne(queryItem, PaymentConfig.class);
			if(paymentConfig == null || StringUtils.isBlank(paymentConfig.getConfig())) return null;
			
			Map configMap = (Map) new SerializerUtil().unserialize(paymentConfig.getConfig().getBytes());
			paymentConfig.setAccount(configMap.get("Account") == null ? "" : configMap.get("Account").toString());
			paymentConfig.setPassword(configMap.get("password") == null ? "" : configMap.get("password").toString());
			paymentConfig.setTerminalId(configMap.get("merAcct") == null ? "" : configMap.get("merAcct").toString());
		} catch (Exception e) {
		}
		
		return paymentConfig;
	}
}
