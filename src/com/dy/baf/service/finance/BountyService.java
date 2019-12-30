package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.common.FnBountyCategory;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.StringUtils;

/**
 * 我的红包
 * @author Administrator
 *
 */
@Service("mobileBountyService")
public class BountyService extends  MobileService {
	
	@Autowired
	private BaseService baseService;

	/**
	 * 我的红包获取数据
	 * @param page
	 * @param loginToken
	 * @param epage
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page bountyList(String page,String loginToken,String epage) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(loginToken));
		//红包记录
		QueryItem bountyItem = new QueryItem(Module.FINANCE,Function.FN_BOUNTY);
		bountyItem.setPage(StringUtils.isBlank(page) ? 1 : Integer.valueOf(page));
		bountyItem.setLimit(StringUtils.isBlank(epage) ? 10 : Integer.valueOf(epage));
		bountyItem.setWhere(Where.eq("member_id", user.getId()));
		bountyItem.setWhere(Where.notEq("status", -1));
		bountyItem.setOrders("FIELD(`red_status`, 1, 2, 3, -1)");
		Page pageObj = (Page)this.baseService.getPage(bountyItem, Map.class);
		List<Map<String,Object>> bountyList = pageObj.getItems();
		for(Map<String,Object> map:bountyList){
			map.put("category_name", getBountyName(map.get("category_id").toString()));
			
			String redStatusName = "";
			if("-1".equals(map.get("red_status").toString())){
				redStatusName = "已过期";
			}else if("1".equals(map.get("red_status").toString())){
				redStatusName = "未使用";
			}else if("2".equals(map.get("red_status").toString())){
				redStatusName = "已冻结";
			}else if("3".equals(map.get("red_status").toString())){
				redStatusName = "已使用";
			}
			
			map.put("red_status_name", redStatusName);
		}
		//pageObj = (Page) this.dataConvert(pageObj,"status:getBountyStatus",null);
		return pageObj;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page bountyUseableList(String page, String epage, String amount, String loginToken, String loanId) throws Exception {

		QueryItem queryLoan = new QueryItem();
		queryLoan.setWhere(Where.eq("id", loanId));
		FnLoan fnLoan = this.getOneByEntity(queryLoan, Module.LOAN, Function.LN_LOAN, FnLoan.class);

		// 获取用户账户
		MbMember user = this.getMbMember(Long.valueOf(loginToken));
		QueryItem queryItem = new QueryItem(Module.FINANCE, Function.FN_BOUNTY);
		queryItem.setWhere(Where.eq("red_status", 1));
		queryItem.setWhere(Where.eq("member_id", user.getId()));
		queryItem.setWhere(Where.notEq("status", -1));
		List<NameValue> andList = new ArrayList<NameValue>();
		andList.add(new NameValue("end_time", DateUtil.getCurrentTime(), GT));
		andList.add(new NameValue("end_time", null, NULL, true));
		andList.add(Where.expression("end_time=add_time", true));
		queryItem.setWhere(Where.setAndList(andList));
		queryItem.setPage(StringUtils.isBlank(page) ? 1 : Integer.parseInt(page));
		queryItem.setLimit(StringUtils.isBlank(epage) ? 10 : Integer.valueOf(epage));
		Page pageObj = (Page)this.baseService.getPage(queryItem, Map.class);
		List<Map<String,Object>> bountyList = pageObj.getItems();
		List<Map<String,Object>> bountys = new ArrayList<Map<String, Object>>();
		
		for (Map<String, Object> map : bountyList) {
			String loanType = (String)map.get("loan_type");
			BigDecimal amountMin = new BigDecimal(map.get("amount_min").toString());
			
			//手动赠送红包处理(针对适合标类型)
			if(!StringUtils.isBlank(loanType) && loanType.contains(fnLoan.getCategoryId().toString()) && new BigDecimal(amount).compareTo(amountMin) >= 0){
				bountys.add(map);;
			}else if(StringUtils.isBlank(loanType) && new BigDecimal(amount).compareTo(amountMin) >= 0){
				bountys.add(map);
			}else{
				continue;
			}
			
			map.put("category_name", getBountyName(map.get("category_id").toString()));
			String redStatusName = "";
			if ("-1".equals(map.get("red_status").toString())) {
				redStatusName = "已过期";
			} else if ("1".equals(map.get("red_status").toString())) {
				redStatusName = "未使用";
			} else if ("2".equals(map.get("red_status").toString())) {
				redStatusName = "已使用";
			}
			map.put("red_status_name", redStatusName);
		}
		
		pageObj.setItems(bountys);
		return pageObj;
	}
	
	
	/**
	 * 根据category_id获取名称
	 * @throws Exception 
	 */
	private String getBountyName(String categotyId) throws Exception{
		QueryItem item = new QueryItem(Module.FINANCE, Function.FN_BOUNTYTYPE);
		item.setFields("name");
		item.getWhere().add(Where.eq("id", categotyId));
		FnBountyCategory cate = this.baseService.getOne(item,FnBountyCategory.class);
		
		return cate==null?"":cate.getName();
	}
	
}
