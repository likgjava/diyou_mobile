package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnFee;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;

/**
 * 交易记录
 * 
 * @author Administrator
 *
 */
@Service("mobileAccountLogService")
public class AccountLogService extends MobileService {

	@Autowired
	private BaseService baseService;

	/**
	 * 资金历史记录列表
	 */
	public DyPhoneResponse accountLog(Long memberId, Integer page) throws Exception {
		// 资金记录
		QueryItem accountLogItem = new QueryItem();
		accountLogItem.setPage(page == null ? 1 : page);
		accountLogItem.setOrders("add_time desc,id desc");
		accountLogItem.setLimit(10);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", memberId));
		accountLogItem.setWhere(whereList);// 设置where条件

		Page pageObj = (Page) this.getPageByMap(accountLogItem, Module.FINANCE, Function.FN_ACCOUNTLOG);
		List<Map<String, Object>> accountLogList = pageObj.getItems();
		BigDecimal new_income;
		BigDecimal new_expend;
		BigDecimal new_freeze;
		BigDecimal un_freeze;
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> log : accountLogList) {
			Map<String, Object> map = new HashMap<String, Object>();
			new_income = new BigDecimal(log.get("income").toString())
					.subtract(new BigDecimal(log.get("pre_income").toString()));
			new_expend = new BigDecimal(log.get("expend").toString())
					.subtract(new BigDecimal(log.get("pre_expend").toString()));
			BigDecimal freezeTemp = new BigDecimal(log.get("freeze").toString())
					.subtract(new BigDecimal(log.get("pre_freeze").toString()));
			if (freezeTemp.compareTo(BigDecimal.ZERO) == -1) {
				un_freeze = freezeTemp.abs();
				new_freeze = new BigDecimal(0);
			} else {
				new_freeze = freezeTemp;
				un_freeze = new BigDecimal(0);
			}

			String add_time = MapUtils.getString(log, "add_time");
			String fee_name = MapUtils.getString(log, "fee_name");
			String remark = MapUtils.getString(log, "remark");
			Double balance = MapUtils.getDouble(log, "balance");
			Double preBalance = MapUtils.getDouble(log, "pre_balance");
			int firstIdx = remark.indexOf(">") + 1;
			if (0 < firstIdx) {
				remark = remark.substring(firstIdx);
				firstIdx = remark.indexOf("<");
				remark = remark.substring(0, firstIdx);
				map.put("loan_name", remark);
			}

			map.put("new_income", new_income);
			map.put("new_expend", new_expend);
			map.put("new_freeze", new_freeze);
			map.put("un_freeze", un_freeze);

			map.put("money", log.get("amount_money"));
			map.put("add_time", add_time);
			map.put("fee_name", fee_name);
			map.put("balance", balance);
			map.put("money_type", preBalance - balance > 0 ? "expend" : "income");
			
			map.put("amount_money",log.get("amount_money"));
			map.put("income",log.get("income"));
			map.put("pre_income",log.get("pre_expend"));
			
			map.put("expend",log.get("expend"));
			map.put("pre_expend",log.get("pre_expend"));
			
			map.put("pre_freeze",log.get("pre_freeze"));
			map.put("freeze",log.get("freeze"));
			logList.add(map);
		}
		pageObj.setItems(logList);
		return this.successJsonResonse(pageObj);
	}

	/**
	 * 获取交易分类
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse getfeelist() throws Exception {
		QueryItem item = new QueryItem(Module.FINANCE, Function.FN_FEE);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.notEq("pid", 0));
		whereList.add(Where.eq("status", 1));

		item.setWhere(whereList);
		item.setFields("id,name");
		List<FnFee> feeList = this.baseService.getList(item, FnFee.class);
		return successJsonResonse(feeList);
	}
}
