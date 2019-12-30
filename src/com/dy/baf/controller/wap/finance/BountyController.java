package com.dy.baf.controller.wap.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.common.FnBounty;
import com.dy.baf.entity.common.FnBountyCategory;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;

/**
 * 我的红包(投资红包列表)
 * @author panxh
 * add at 2016年9月5日
 */
@Controller(value="wapBountyController")
public class BountyController extends WapBaseController{

	
	/**
	 * 获取当前登录用户的可用红包列表
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/bounty/useableList", method = RequestMethod.POST)
	public DyResponse getRedBag(BigDecimal amount) throws Exception {
		String id = this.getRequest().getParameter("loanId");
		if (amount == null)
			return null;
		
		QueryItem queryLoan = new QueryItem();
		queryLoan.setWhere(Where.eq("id", id));
		FnLoan fnLoan = this.getOneByEntity(queryLoan, Module.LOAN, Function.LN_LOAN, FnLoan.class);
		
		// 获取用户账户
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		QueryItem queryItem = new QueryItem();
		queryItem.setWhere(Where.eq("red_status", 1));
		queryItem.setWhere(Where.eq("member_id", user.getId()));
		List<NameValue> andList = new ArrayList<NameValue>();
		andList.add(new NameValue("end_time", DateUtil.getCurrentTime(), GT));
		andList.add(new NameValue("end_time", null, NULL, true));
		andList.add(Where.expression("end_time=add_time", true));
		queryItem.setWhere(Where.setAndList(andList));
		Map<String, Object> redBag = new HashMap<String, Object>();
		try {
			// 红包记录
			List<FnBounty> bountyList = this.getListByEntity(queryItem, Module.FINANCE, Function.FN_BOUNTY, FnBounty.class);
			List<Map<String, Object>> bounty = new ArrayList<Map<String, Object>>();
			if (bountyList != null && bountyList.size() > 0) {
				for (FnBounty fnBounty : bountyList) {
					Map<String, Object> map = new HashMap<String, Object>();
					//手动赠送红包处理(针对适合标类型)
					if(!StringUtils.isEmpty(fnBounty.getLoanType()) && fnBounty.getLoanType().contains(String.valueOf(fnLoan.getCategoryId())) && amount.compareTo(fnBounty.getAmountMin()) >= 0){
						map.put("red", fnBounty);
					}else if(StringUtils.isEmpty(fnBounty.getLoanType()) && amount.compareTo(fnBounty.getAmountMin()) >= 0){
						map.put("red", fnBounty);
					}else{
						continue;
					}
					map.put("bountyType", getBountyName(String.valueOf(fnBounty.getCategoryId())));
					bounty.add(map);
				}
			}
			redBag.put("redbag", bounty);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createSuccessJsonResonse(redBag);
	}
	
	/**
	 * 根据category_id获取名称
	 * @throws Exception 
	 */
	private String getBountyName(String categotyId) throws Exception{
		QueryItem item = new QueryItem();
		item.setFields("name");
		item.getWhere().add(Where.eq("id", categotyId));
		FnBountyCategory cate = this.getOneByEntity(item, Module.FINANCE, Function.FN_BOUNTYTYPE, FnBountyCategory.class);
		
		return cate==null?"":cate.getName();
	}
}
