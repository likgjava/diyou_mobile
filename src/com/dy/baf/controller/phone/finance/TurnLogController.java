package com.dy.baf.controller.phone.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.FnAccountPaymentConfig;
import com.dy.baf.entity.common.FnAccountTurn;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;

@Controller
@RequestMapping(value="/finance")
public class TurnLogController extends FrontBaseController {
	@RequestMapping(value="/turn/log", method=RequestMethod.GET)
	public ModelAndView withdrawLog() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("finance/turn/turn_log.jsp");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 提现记录列表
	 */
	@ResponseBody
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/turn/turnlog", method=RequestMethod.POST)
	public Page withdrawlog(Integer page, Integer status, Date start_time, Date end_time) throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,ind,payment_nid,amount,add_time,status,remark");
		queryItem.setPage(page == null ? 1 : page);
		queryItem.setOrders("add_time desc");
		queryItem.setLimit(10);
		queryItem.setOrders("id desc");
		List<Where> whereList = new ArrayList<Where>();
		addWhereCondition(whereList, "status", status);
		addWhereCondition(whereList, "member_id", user.getId());
		addAndWhereCondition(whereList, "add_time", start_time, end_time);
		queryItem.setWhere(whereList);
		
		BigDecimal amountTotal = new BigDecimal(0);
		Map<String, String> paymentMap = new HashMap<String, String>();
		Page<FnAccountTurn> turnLogPage = getPageByEntity(queryItem, Module.FINANCE, Function.FN_TURN, FnAccountTurn.class);
		List<FnAccountTurn> newItems = new ArrayList<FnAccountTurn>();
		for(FnAccountTurn accountTurn : turnLogPage.getItems()){
			String paymentType = paymentMap.get(accountTurn.getPaymentNid());
			if(paymentType == null) {
				paymentType = getPayName(accountTurn.getPaymentNid());
				paymentMap.put(accountTurn.getPaymentNid(), paymentType);
			}
			
			accountTurn.setPaymentNid(paymentType);
			newItems.add(accountTurn);
			
			amountTotal = amountTotal.add(accountTurn.getAmount());
		}
		turnLogPage.setItems(newItems);
		turnLogPage.setParams(amountTotal);
		
		return turnLogPage;
	}
	
	/**
	 * 根据nid获取支付方式
	 * @throws Exception 
	 */
	private String getPayName(String nid) throws Exception{
		if("admin".equals(nid))return "后台充值";
		
		QueryItem queryItem  = new QueryItem();
		queryItem.setFields("nid,name");
		queryItem.getWhere().add(Where.eq("nid", nid));
		FnAccountPaymentConfig payment = getOneByEntity(queryItem, Module.FINANCE, Function.FN_PAYMENT, FnAccountPaymentConfig.class);
		
		return payment.getName();
	}
}