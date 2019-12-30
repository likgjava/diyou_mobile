package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemLinkage;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.FeeUtil;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.OptionUtil;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.httpinvoker.FinanceService;
import com.sun.org.apache.bcel.internal.generic.ISTORE;

/**
 * 提现
 * @author Administrator
 *
 */
@Service("mobileWithdrawService")
public class WithdrawService extends  MobileService {
	@Autowired
	private BaseService baseService;
	@Autowired
	private FinanceService financeService;
	@Autowired
	private FeeUtil feeUtil;
	@Autowired
	private OptionUtil optionUtil;
	
	public DyPhoneResponse cashSubmit(BigDecimal money,String paypassword,String login_token, boolean isTrust) throws Exception {
		if(money.compareTo(BigDecimal.ZERO) <= 0) return errorJsonResonse("提现金额必须大于0");
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		QueryItem item = new QueryItem(Module.FINANCE, Function.FN_ACCOUNT);
		item.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount account = this.baseService.getOne(item,FnAccount.class);
		//判断是否实名认证，是否设置支付密码
		if(user.getIsRealname() == -1)return errorJsonResonse("请先进行实名认证");
		if("".equals(user.getPaypassword()))return errorJsonResonse("请先设置支付密码");
		//判断金额
		if(money==null || money.compareTo(new BigDecimal(0))<0)return errorJsonResonse("输入的金额有错");
		//判断可用余额是否足够
		if(money.compareTo(account.getBalanceAmount()) > 0)return errorJsonResonse("可用余额不足");
		//验证支付密码
		paypassword = SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword));
		if(!paypassword.equals(user.getPaypassword()))return errorJsonResonse("支付密码错误");
		//计算提现费用
		DyResponse response = this.getFee(money,user, isTrust);
		BigDecimal feeAmount;
		if(response.getStatus() == 200){
			feeAmount = (BigDecimal) response.getData();//提现费用
		}else{
			return errorJsonResonse(response.getDescription());
		}
		BigDecimal balanceAmount = money.subtract(feeAmount);//实际到账金额
		//添加提现信息，添加资金记录，冻结用户资金，更新用户资产
		financeService.getCash(user,money,feeAmount,balanceAmount,IpUtil.ipStrToLong(GetUtils.getRemoteIp()));
		return successJsonResonse("提现成功");
	}
	/**
	 * 提現記錄
	 * @param page
	 * @param status
	 * @param start_time
	 * @param end_time
	 * @param login_token
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse withdrawlog(Integer page,Integer status,Date start_time,Date end_time,String login_token) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		//提现记录
		QueryItem cashItem = new QueryItem("finance","withdraw");
		cashItem.setPage(page == null ? 1 : page);
		cashItem.setOrders("add_time desc");
		cashItem.setLimit(10);
		if(status !=null){
		cashItem.setWhere(Where.eq("status",status));
		}
		cashItem.setWhere(Where.eq("member_id",user.getId()));
		
		List<NameValue> andList = new ArrayList<NameValue>();
		if (start_time != null) {
			andList.add(new NameValue("add_time", DateUtil.convert(start_time), ">="));
		}
		if (end_time!= null) {
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(DateUtil.dateParse(end_time.getTime()));
			endCal.add(Calendar.DAY_OF_MONTH, 1);
			andList.add(new NameValue("add_time", DateUtil.convert(endCal.getTime()), "<"));
		}
		
		if (andList.size() > 0) {
			cashItem.setWhere(Where.setAndList(andList));
		}
		
		BigDecimal amountTotal = new BigDecimal(0);
		
		Page pageObj = this.baseService.getPage(cashItem,Map.class);
		List<Map<String,Object>> list = pageObj.getItems();
		for(Map<String,Object> map : list){
			if(map.get("bank_id") != null){
				map.put("bank_name", getBankName(map.get("bank_id").toString()));
			}
			map.put("account", map.get("account").toString().substring(0,5)+"*********"+map.get("account").toString().substring(map.get("account").toString().length()-2));
			amountTotal = amountTotal.add(BigDecimal.valueOf(Double.valueOf(map.get("amount").toString())));
			map.put("status_name", map.get("status"));
		}
		pageObj.setParams(amountTotal);
		return successJsonResonse(new DataConvertUtil(pageObj).setStatus("status_name", optionUtil.getCashStatus()).convert());
	}
	
	/**
	 * 獲取提現費用
	 * @param account
	 * @param login_token
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getCashFee(BigDecimal account,String login_token, boolean isTrust) throws Exception {
		if(account.compareTo(BigDecimal.ZERO) <= 0) return errorJsonResonse("提现金额必须大于0");
		//查询可提现金额
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount fnAccount = this.baseService.getOne(accountItem, FnAccount.class);
		BigDecimal balanceAmount = fnAccount.getBalanceAmount();
		//判断金额
		if(account==null){
			return errorJsonResonse("请输入正确的金额");
		}
		if(account.compareTo(balanceAmount)>0 )return errorJsonResonse("账号余额不足");
		
		DyResponse response = feeUtil.getWithdrawFee(account, user.getId(), isTrust);
		if(response.getStatus() != response.OK ){
			return errorJsonResonse(response.getDescription());
		}
		BigDecimal fee = new BigDecimal(response.getData().toString());
		Map<String,Object> responseMap = new HashMap<String, Object>();
		
		responseMap.put("account_yes", account);//外扣
		responseMap.put("fee", fee);
		return successJsonResonse(responseMap);
	}
	
	
	/**
	 * 提现费用工具
	 * @throws Exception 
	 */
	public DyResponse getFee(BigDecimal account,MbMember user, boolean isTrust) throws Exception{
		return feeUtil.getWithdrawFee(account, user.getId(), isTrust);
	}
	/**
	 * 根据bankid获取银行名称
	 * @throws Exception 
	 */
	private String getBankName(String id) throws Exception{
		QueryItem bankItem = new QueryItem(Module.SYSTEM, Function.SYS_LINKAGE);
		bankItem.getWhere().add(Where.eq("id", id));
		SysSystemLinkage linkage = this.baseService.getOne(bankItem, SysSystemLinkage.class);
		return linkage == null ? null : linkage.getName();
	}
}
