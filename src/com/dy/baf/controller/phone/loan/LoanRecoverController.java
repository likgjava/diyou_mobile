package com.dy.baf.controller.phone.loan;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;

@Controller
@RequestMapping("/loan")
public class LoanRecoverController extends FrontBaseController {
	
	/**
	 * 我的收款计划页面
	 * @return
	 */
	@RequestMapping("/recover/index")
	public ModelAndView recover(Model model) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("loan/recover/index.jsp");
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 收款计划获取数据
	 */
	@ResponseBody
	@RequestMapping("/recover/myrecoverlist")
	public Page myrecoverlist(Integer page,Date start_time,Date end_time,String keyword) throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		QueryItem reoverItem = new QueryItem();
		reoverItem.setPage(page == null ? 1 : page);
		reoverItem.setLimit(10);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_member_id", user.getId()));
		whereList.add(Where.notEq("status", 1));
		if (keyword != null) {
			whereList.add(Where.likeAll("loan_name", keyword));
		}
		addAndWhereCondition(whereList, "recover_time", start_time, end_time);
		reoverItem.setWhere(whereList);
		Page pageObj = (Page) this.dataConvert(this.getPageByMap(reoverItem, "loan", "recover"));
		//对数据进行转换
		List<Map<String,Object>> recoverList = pageObj.getItems();
		String loan_id;//借款标id
		String loan_name;//借款标名称
		String recover_time;//应还款时间
		String loan_member_name;//借款人用户名
		String period_name;//第几期/总期数
		Double amount;//应收总额
		Double principal;//应收本金
		Double interest;//应收利息
		String late_day;//逾期天数
		List<Map<String,Object>> logList = new ArrayList<Map<String,Object>>();
		for(int i = 0;i<recoverList.size();i++){
			String status_name="";//状态
			Map<String,Object> map = new HashMap<String,Object>();
			loan_id = (String) recoverList.get(i).get("loan_id");
			loan_name = (String) recoverList.get(i).get("loan_name");
			recover_time = (String) recoverList.get(i).get("recover_time");
			loan_member_name = (String) recoverList.get(i).get("loan_member_name");
			period_name = (String) recoverList.get(i).get("period_no")+"/"+(String) recoverList.get(i).get("period");
			amount = Double.valueOf((String) recoverList.get(i).get("amount"));
			principal = Double.valueOf((String)recoverList.get(i).get("principal"));
			interest =  Double.valueOf((String)recoverList.get(i).get("interest"));
			late_day = (String) recoverList.get(i).get("overdue_days");
			if("-1".equals(recoverList.get(i).get("status"))){
				status_name = "未还款";
			}
			if("1".equals(recoverList.get(i).get("recover_type"))){
				status_name = "正常还款";
			}else if("2".equals(recoverList.get(i).get("recover_type"))){
				status_name = "逾期还款";
			}else if("3".equals(recoverList.get(i).get("recover_type"))){
				status_name = "提前还款";
			}else if("4".equals(recoverList.get(i).get("recover_type"))){
				status_name = "网站垫付";
			}else if("5".equals(recoverList.get(i).get("transfer_status"))){
				status_name = "未还款";
			}
			map.put("loan_id", loan_id);
			map.put("loan_name", loan_name);
			map.put("recover_time", recover_time);
			map.put("loan_member_name", loan_member_name);
			map.put("period_name", period_name);
			map.put("amount", amount);
			map.put("principal", principal);
			map.put("interest", interest);
			map.put("late_day", late_day);
			map.put("status_name", status_name);
			logList.add(map);
		}
		pageObj.setItems(logList);
		return pageObj;
	}
}
