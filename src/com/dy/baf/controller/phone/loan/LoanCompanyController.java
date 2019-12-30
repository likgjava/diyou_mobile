package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanRepay;
import com.dy.baf.entity.common.FnLoanRepayPeriod;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbGuaranteeCompany;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.serializer.SerializerUtil;

@Controller
@RequestMapping(value="/loan")
public class LoanCompanyController extends FrontBaseController{

	
	/**
	 * 担保公司详情页
	 */
	@RequestMapping(value="/company/vouch", method=RequestMethod.GET)
	public ModelAndView vouch() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("loan/loan/vouch.jsp");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 获取担保公司关联标种信息
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/company/list")
	public Page getVouchData(Long id,Integer page,String order) throws Exception{
		QueryItem loanItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("vouch_company_id", id));
		addAndWhereCondition(whereList, "status", 2, 7);
		loanItem.setWhere(whereList);
		loanItem.setPage(page == null ? 1 : page);
		String orders = "status asc,additional_status desc,add_time desc";
		if(order !=null && order!=""){
			if("amount_up".equals(order)){
				loanItem.setOrders("amount asc," + orders);
			}else if("amount_down".equals(order)){
				loanItem.setOrders("amount desc," + orders);
			}else if("apr_up".equals(order)){
				loanItem.setOrders("apr asc," + orders);
			}else if("apr_down".equals(order)){
				loanItem.setOrders("apr desc," + orders);
			}else if("period_up".equals(order)){
				loanItem.setOrders("period asc," + orders);
			}else if("period_down".equals(order)){
				loanItem.setOrders("period desc," + orders);
			}else if("progress_up".equals(order)){
				loanItem.setOrders("progress asc," + orders);
			}else if("progress_down".equals(order)){
				loanItem.setOrders("progress desc," + orders);
			}
		}else{
			loanItem.setOrders(orders);
		}
		loanItem.setLimit(5);
		Page pageObj = this.getPageByMap(loanItem, Module.LOAN, Function.LN_LOAN);
		List<Map<String,Object>> list = pageObj.getItems();
		for(Map<String,Object> map : list){
			if("3".equals(map.get("status").toString())){
				map.put("status_name", "借款中");//status_name = "借款中";
			}else if("4".equals(map.get("status").toString())){
				map.put("status_name", "满标复审");//status_name="满标复审";
			}else if("5".equals(map.get("status").toString())||"6".equals(map.get("status").toString())){
				map.put("status_name", "还款中");//status_name="还款中";
			}else if("7".equals(map.get("status").toString())){
				map.put("status_name", "已还完");//status_name="已还完";
			}
			if("7".equals(map.get("status").toString())||"6".equals(map.get("status").toString())){
				map.put("period_all", getPeriodNum(map.get("id").toString(),"all"));
				map.put("period_back_all", getPeriodNum(map.get("id").toString(),"payed"));
				map.put("back_amount", getRepayAmount(map.get("id").toString()));
			}
		}
		return pageObj;
	}
	
	/**
	 * 获取担保公司信息
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/company/vouchInfo")
	public Map<String,Object> getvouchInfo(Long id) throws Exception{
		QueryItem companyItem = new QueryItem();
		companyItem.getWhere().add(Where.eq("id", id));
		MbGuaranteeCompany company = this.getOneByEntity(companyItem, Module.MEMBER, Function.MB_VOUCHCOMPANY, MbGuaranteeCompany.class);
		FnLoan inLoan = getLoanByVouchId(id,"inloan");//正在担保中
		FnLoan waitPayLoan = getLoanByVouchId(id,"waitpay");//待还项目
		FnLoan payedLoan = getLoanByVouchId(id,"payed");//已还项目
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("inLoanAmount", getAmountByVouchId(id,"inloan"));//正在担保金额
		map.put("waitAmount", getAmountByVouchId(id,"waitpay"));//待还金额
		map.put("payedAmount", getAmountByVouchId(id,"payed"));//已还金额
		map.put("inLoanNum", inLoan == null ? 0 : inLoan.getId());
		map.put("waitNum", waitPayLoan == null ? 0 : waitPayLoan.getId());
		map.put("payedNum", payedLoan == null ? 0 : payedLoan.getId());
		map.put("contents", company == null ? null : company.getContents());
		SerializerUtil serializerUtil = new SerializerUtil();
		//获取图片服务器地址
		String imageHost = PropertiesUtil.getImageHost();
		List<Map<String, Object>> configList = (List<Map<String, Object>>) serializerUtil.unserialize(company.getCompanyMaterials().getBytes());
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (configList != null) {
			for (int i = 0; i < configList.size(); i++) {
				if (configList.get(i) != null) {
					Map<String, Object> urlMap = new HashMap<String, Object>();
					urlMap.put("title", configList.get(i).get("title"));
					urlMap.put("imgurl", imageHost + configList.get(i).get("imgurl"));
					urlMap.put("minimg", configList.get(i).get("minimg"));
					list.add(urlMap);
				}
			}
		}
		map.put("companyMaterials", list);
		map.put("inPeriodNum", getPeriod(id,"inloan"));//正在担保中
		map.put("waitPeriodNum", getPeriod(id,"waitpay"));//待还
		map.put("payedPeriodNum", getPeriod(id,"payed"));//已还项目
		//公司Logo和名称,详情背景
		map.put("companyName", company.getName());
		map.put("logo", imageHost + company.getCompanyLogo());
		map.put("companyIntro", imageHost + company.getCompanyIntro());
		return map;
	}
	
	/**
	 * 根据担保公司获取担保标信息
	 * @throws Exception 
	 */
	private FnLoan getLoanByVouchId(Long id,String type) throws Exception{
		QueryItem loanItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("vouch_company_id", id));
		if("inloan".equals(type)){//正在担保
			addAndWhereCondition(whereList, "status", 2, 6);
		}else if("waitpay".equals(type)){//待还项目
			whereList.add(Where.eq("status", 6));
		}else if("payed".equals(type)){//已还项目
			whereList.add(Where.eq("status", 7));
		}
		loanItem.setWhere(whereList);
		loanItem.setFields("sum(amount) amount,count(id) id");
		FnLoan loan = this.getOneByEntity(loanItem, Module.LOAN, Function.LN_LOAN, FnLoan.class);
		if(loan.getAmount() == null) loan.setAmount(BigDecimal.ZERO);
		return loan;
	}
	
	/**
	 * 根据担保公司获取担保金额，待还金额，已还金额
	 * @throws Exception 
	 */
	private BigDecimal getAmountByVouchId(Long id,String type) throws Exception{
		QueryItem loanItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("vouch_company_id", id));
		if("inloan".equals(type)){//正在担保
			addAndWhereCondition(whereList, "status", 2, 6);
		}else if("waitpay".equals(type)){//待还项目
			whereList.add(Where.eq("status", 6));
		}else if("payed".equals(type)){//已还项目
			addAndWhereCondition(whereList, "status", 6, 7);
		}
		loanItem.setWhere(whereList);
		loanItem.setFields("id,status,amount");
		List<FnLoan> loanList = this.getListByEntity(loanItem, Module.LOAN, Function.LN_LOAN, FnLoan.class);
		BigDecimal amount = BigDecimal.ZERO;
		for(FnLoan loan : loanList){
			if(loan.getStatus() < 6){
				amount = amount.add(loan.getAmount());
			}
			QueryItem item = new QueryItem();
			item.getWhere().add(Where.eq("loan_id", loan.getId()));
			if("inloan".equals(type)){//正在担保
				item.getWhere().add(Where.eq("status", -1));
			}else if("waitpay".equals(type)){//待还项目
				whereList.add(Where.eq("status", -1));
			}else if("payed".equals(type)){//已还项目
				whereList.add(Where.eq("status", 1));
			}
			item.setFields("sum(amount) amount,sum(amount_yes) amount_yes");
			FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
			if("inloan".equals(type) || "waitpay".equals(type)){
				amount = amount.add(NumberUtils.sub(recover.getAmount(), recover.getAmountYes()));
			}else{
				amount = amount.add(recover.getAmountYes());
			}
		}
		return amount;
	}
	
	
	/**
	 * 根据担保标公司获取担保标期数
	 * @throws Exception 
	 */
	private Long getPeriod(Long id,String type) throws Exception{
		QueryItem loanItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("vouch_company_id", id));
		if("inloan".equals(type)){//正在担保
			addAndWhereCondition(whereList, "status", 2, 6);
		}else if("waitpay".equals(type)){//待还项目
			whereList.add(Where.eq("status", 6));
		}else if("payed".equals(type)){//已还项目
			addAndWhereCondition(whereList, "status", 6, 7);
		}else if("all".equals(type)){
		}
		loanItem.setWhere(whereList);
		loanItem.setFields("id,period,status");
		List<FnLoan> loanList = this.getListByEntity(loanItem, Module.LOAN, Function.LN_LOAN, FnLoan.class);
		Long num = 0L;
		for(FnLoan loan : loanList){
			if(loan.getStatus() < 6){
				num = num + loan.getPeriod();
			}
			QueryItem item = new QueryItem();
			List<Where> where = new ArrayList<Where>();
			if("inloan".equals(type)){//正在担保
				where.add(Where.eq("status", -1));
			}else if("waitpay".equals(type)){//待还项目
				where.add(Where.eq("status", -1));
			}else if("payed".equals(type)){//已还项目
				where.add(Where.eq("status", 1));
			}
			where.add(Where.eq("loan_id", loan.getId()));
			item.setWhere(where);
			item.setFields("count(id) id");
			FnLoanRepayPeriod period = this.getOneByEntity(item, Module.LOAN, Function.LN_REPAYPERIOD, FnLoanRepayPeriod.class);
			if(period !=null){
				num = num + period.getId();
			}
		}
		return num;
	}
	
	/**
	 * 根据loanId获取担保标期数
	 * @throws Exception 
	 */
	private Integer getPeriodNum(String id,String type) throws Exception{
		QueryItem item = new QueryItem();
		List<Where> where = new ArrayList<Where>();
		if("all".equals(type)){//共几期
		}else if("payed".equals(type)){//已还项目
			where.add(Where.eq("status", 1));
		}
		item.getWhere().add(Where.eq("loan_id", id));
		item.setFields("period,period_yes");
		FnLoanRepay repay = this.getOneByEntity(item, Module.LOAN, Function.LN_REPAY, FnLoanRepay.class);
		if("all".equals(type)) return repay == null ? 0 : repay.getPeriod();
		if("payed".equals(type)) return repay == null ? 0 : repay.getPeriodYes();
		return 0;
	}
	
	/**
	 * 根据loanid获取总还款金额
	 * @throws Exception 
	 */
	private BigDecimal getRepayAmount(String id) throws Exception{
		QueryItem recoverItem = new QueryItem();
		recoverItem.getWhere().add(Where.eq("loan_id", id));
		recoverItem.getWhere().add(Where.eq("status", 1));
		recoverItem.setFields("sum(amount_yes) amount_yes");
		FnTenderRecover recover = this.getOneByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover == null ? BigDecimal.ZERO : recover.getAmountYes();
	}
}
