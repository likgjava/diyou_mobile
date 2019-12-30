package com.dy.baf.service.member;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnLoanAmount;
import com.dy.baf.entity.common.FnLoanAmountApply;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.OptionUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 额度管理
 * @author 波哥
 * @date 2015年11月12日 上午11:35:37 
 * @version V1.0
 */
@Service("mobileQuotaService")
public class QuotaService extends MobileService{

	@Autowired
	private BaseService baseService;
	
	@Autowired
	private OptionUtil optionUtil;
	
	
	/**
	 * 额度类型
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse amountTypeList() throws Exception{
		QueryItem amtTypeQryItem = new QueryItem(Module.FINANCE, Function.FN_LOANAMOUNTTYPE);
		amtTypeQryItem.setFields("id, name,remark");
		amtTypeQryItem.getWhere().add(Where.eq("status", 1));
		amtTypeQryItem.setOrders("id");
		Page pageResponse = this.baseService.getPage(amtTypeQryItem, Map.class);
		Map<String,Object> responseMap = new HashMap<String, Object>();
		return successJsonResonse(responseMap);
	}
	
	/**
	 * 额度申请
	 * @param request
	 * @return
	 */
	public DyPhoneResponse quotaApply(String memberId,String amount_type,String amount,String remark,Long ip) throws Exception {
		
		if (StringUtils.isBlank(memberId)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		if (StringUtils.isBlank(amount_type)) {
			return errorJsonResonse("申请类型不能为空");
		}
		if (StringUtils.isBlank(amount)) {
			return errorJsonResonse("申请额度不能为空");
		}
		if (StringUtils.isBlank(remark)) {
			return errorJsonResonse("详细说明不能为空");
		}
		Integer amountType = Integer.valueOf(amount_type);
		BigDecimal amountBig = new BigDecimal(amount);
		
		MbMember member = this.getMbMember(Long.valueOf(memberId));

		FnLoanAmountApply loanAmountApply = new FnLoanAmountApply();
		loanAmountApply.setMemberId(member.getId());
		loanAmountApply.setMemberName(member.getName());
		loanAmountApply.setAmount(amountBig);
		loanAmountApply.setIncomeAmount(BigDecimal.ZERO);
		loanAmountApply.setCategoryId(amountType);
		loanAmountApply.setRemark(remark);
		loanAmountApply.setStatus(-2);
		loanAmountApply.setType(1);
		loanAmountApply.setAddTime(DateUtil.getCurrentTime());
		loanAmountApply.setAddIp(ip);
		this.baseService.insert(Module.FINANCE, Function.FN_LOANAMOUNTAPPLY, loanAmountApply);
		return successJsonResonse("申请成功");
	}
	
	
	
	/**
	 * 额度记录
	 * @param request
	 * @return
	 */
	public DyPhoneResponse amountApplyList(String memberId,String page,String epage) throws Exception {
		
		if (StringUtils.isBlank(memberId)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		
		MbMember member = this.getMbMember(Long.valueOf(memberId));
		int pageNum = 1;
		if (StringUtils.isNotBlank(page)) {
			pageNum = Integer.valueOf(page);
		}
		QueryItem queryItem = new QueryItem(Module.FINANCE, Function.FN_LOANAMOUNTAPPLY);
		queryItem.setFields("id,add_time,category_id,amount,income_amount,remark,status,verify_time,verify_remark");
		queryItem.setWhere(new Where("member_id", member.getId()));
		queryItem.setOrders("add_time desc");
		queryItem.setLimit(5);
		queryItem.setPage(pageNum);
		
		Page pageOjbect = this.baseService.getPage(queryItem, Map.class);
		return successJsonResonse(new DataConvertUtil(pageOjbect).setStatus("category_id", optionUtil.getAmountCate()).setStatus("status", optionUtil.getApproveStatus()).convert());
	}
	
	
	
	/**
	 * 我的额度信息
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse myAmount(String memberId) throws Exception {
		if (StringUtils.isBlank(memberId)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		
		MbMember member = this.getMbMember(Long.valueOf(memberId));
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,member_id,member_name,credit_amount,vouch_amount,pawn_amount,roam_amount,credit_amount_freeze,vouch_amount_freeze,pawn_amount_freeze,roam_amount_freeze");
		queryItem.getWhere().add(Where.eq("member_id", member.getId()));
		queryItem.setOrders("id asc");
		FnLoanAmount myAmount = this.getOneByEntity(queryItem, Module.FINANCE, Function.FN_LOANAMOUNT, FnLoanAmount.class);

		QueryItem amtTypeQryItem = new QueryItem(Module.FINANCE, Function.FN_LOANAMOUNTTYPE);
		amtTypeQryItem.setFields("id, name,remark");
		amtTypeQryItem.getWhere().add(Where.eq("status", 1));
		amtTypeQryItem.setOrders("id");
		List<Map> amountTypeList = this.baseService.getList(amtTypeQryItem, Map.class);
		for (Map map : amountTypeList) {
			String remark=String.valueOf(map.get("remark"));
			if ("credit".equals(remark)) {//信用额度
				map.put("amount_total", myAmount.getCreditAmount());
				map.put("use_amount", NumberUtils.sub(myAmount.getCreditAmount(), myAmount.getCreditAmountFreeze()));
			} else if ("vouch".equals(remark)) { //担保额度
				map.put("amount_total", myAmount.getVouchAmount());
				map.put("use_amount", NumberUtils.sub(myAmount.getVouchAmount(), myAmount.getVouchAmountFreeze()));
			} else if ("pawn".equals(remark)) { //抵押额度
				map.put("amount_total", myAmount.getPawnAmount());
				map.put("use_amount", NumberUtils.sub(myAmount.getPawnAmount(), myAmount.getPawnAmountFreeze()));
			}else if ("roam".equals(remark)) { //流转额度
				map.put("amount_total", myAmount.getRoamAmount());
				map.put("use_amount", NumberUtils.sub(myAmount.getRoamAmount(), myAmount.getRoamAmountFreeze()));
			}
		}
		Map<String, Object> my_amount = new HashMap<String, Object>();
		my_amount.put("credit_amount", myAmount.getCreditAmount());
		my_amount.put("credit_amount_freeze", myAmount.getCreditAmountFreeze());
		my_amount.put("vouch_amount", myAmount.getVouchAmount());
		my_amount.put("vouch_amount_freeze", myAmount.getVouchAmountFreeze());
		my_amount.put("pawn_amount", myAmount.getPawnAmount());
		my_amount.put("pawn_amount_freeze", myAmount.getPawnAmountFreeze());
		my_amount.put("roam_amount", myAmount.getRoamAmount());
		my_amount.put("roam_amount_freeze", myAmount.getRoamAmountFreeze());
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("my_amount", my_amount);
		data.put("amount_type", amountTypeList);
		
		return successJsonResonse(data);
	}
}
