package com.dy.baf.service.loan;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnBounty;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanCategory;
import com.dy.baf.entity.common.FnLoanInfo;
import com.dy.baf.entity.common.FnLoanRepayPeriod;
import com.dy.baf.entity.common.FnLoanRepayType;
import com.dy.baf.entity.common.FnLoanRoam;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.MbGuaranteeCompany;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.entity.custom.Tender;
import com.dy.baf.entity.custom.TenderCondition;
import com.dy.baf.entity.custom.TenderVo;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.FinanceService;

@Service("mobileLoanTenderService")
public class LoanTenderService extends MobileService {

	@Autowired
	private BaseService baseService;
	@Autowired
	private FinanceService financeService;
	
	/**
	 * 投资列表
	 * @param page
	 * @param loan_type
	 * @param amount_search
	 * @param apr_search
	 * @param period_search
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public Page getTenderIndexList(Integer page,String loan_type,String amount_search,String apr_search,String period_search,String order,String url) throws Exception {
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		List<Where> whereList = new ArrayList<Where>();
//		List<NameValue> andList = new ArrayList<NameValue>();
//		andList.add(new NameValue("overdue_time", DateUtil.getCurrentTime(), Condition.GE));
//		andList.add(new NameValue("overdue_time", 0, Condition.EQ,true));
//		andList.add(new NameValue("status", 4, Condition.EQ, true));
//		whereList.add(Where.setAndList(andList));
		whereList.add(new Where("status",2,">"));
		whereList.add(Where.expression("((case when status = 3 then (overdue_time >="+ DateUtil.getCurrentTime() +") else (1=1) end) or overdue_time = 0 or status = 4)", false));
		whereList.add(new Where("status",2,">"));
		whereList.add(new Where("hidden_status",1));
		whereList.add(Where.notEq("member_id", -1));
		loanItem.setFields("id,serialno,name,additional_status,apr,period,amount,repay_type,repay_type repay_type_name,status,status status_name,progress,category_id,category_id category_name,tender_count,vouch_company_id,credited_amount,category_type");

		//搜索条件
		if(!org.apache.commons.lang.StringUtils.isEmpty(loan_type))whereList.add(new Where("category_id",loan_type));
		String orders = "status asc,additional_status desc,progress asc,add_time desc";
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
			}else if("scale_up".equals(order)){
				loanItem.setOrders("progress asc," + orders);
			}else if("scale_down".equals(order)){
				loanItem.setOrders("progress desc," + orders);
			}
		}else{
			loanItem.setOrders(orders);
		}
		//查询流转标类型
		QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);//借款标类型
		loanCategoryItem.setFields("id,is_roam,pic");
		List<Map> cateList = (List<Map>) this.baseService.getList(loanCategoryItem);
				
		
		loanItem.setPage(page == null ? 1 : page);
		loanItem.setLimit(10);
		loanItem.setWhere(whereList);
		Page pageObj = (Page) this.baseService.getPage(loanItem, Map.class);
		List<Map<String,Object>> loanList = pageObj.getItems();
		List<Map<String,Object>> tenderList = new ArrayList<Map<String,Object>>();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		for(int i = 0;i<loanList.size();i++){
			FnLoanInfo loanInfo = this.getLoanInfo((Long)loanList.get(i).get("id"));
			Map<String,Object> map = loanList.get(i);
			map.put("award_status", loanInfo.getAwardStatus());
			
			if(map.get("vouch_company_id").toString() != null && !"0".equals(map.get("vouch_company_id").toString())){
				MbGuaranteeCompany bondingCompany = null;
				QueryItem companyItem = new QueryItem(Module.MEMBER, Function.MB_VOUCHCOMPANY);
				companyItem.getWhere().add(Where.eq("id", map.get("vouch_company_id").toString()));
				bondingCompany = this.baseService.getOne(companyItem,  MbGuaranteeCompany.class);
				bondingCompany.setCompanyLogo(PropertiesUtil.getImageHost() + bondingCompany.getCompanyLogo());
				map.put("vouch_company_name", bondingCompany.getName());
			}else{
				map.put("vouch_company_name", "南昌金融投资有限公司");
			}
			for (Map cate : cateList) {
				if(cate.get("id") !=null && loanList.get(i).get("category_id")!=null && cate.get("id").toString().equals(loanList.get(i).get("category_id").toString())){
					map.put("is_roam", cate.get("is_roam"));
					if(cate.get("pic") != null && StringUtils.isNotBlank(String.valueOf(cate.get("pic")))){
						map.put("pic", PropertiesUtil.getImageHost() + cate.get("pic"));
					}
					break;
				}
			}
			String status = map.get("status").toString() ;
			String status_name = "";
			if ("3".equals(status)) {
				if(MapUtils.getInteger(map, "is_roam",0)==1){
					BigDecimal creditedAmount=new BigDecimal(String.valueOf(loanList.get(i).get("credited_amount")));
					BigDecimal borrowAmount=new BigDecimal(String.valueOf(loanList.get(i).get("amount")));
					if(creditedAmount.compareTo(borrowAmount)==0){
						status_name = "回购中";
					}else{
						status_name = "流转中";	
					}
					
				}else{
					status_name = "借款中";
				}
			} else if ("4".equals(status)) {
				status_name = "满标复审";
			} else if ("5".equals(status) || "6".equals(status)) {
				if((Integer)map.get("is_roam")==1){
					status_name = "回购中";
				}else{
					status_name = "还款中";
				}
			} else if ("7".equals(status)) {
				if((Integer)map.get("is_roam")==1){
					status_name = "回购完";
				}else{
					status_name = "已还完";
				}
			}
			//查询流转标信息
			if(MapUtils.getInteger(map, "is_roam",0)==1){
				QueryItem loanRoamItem = new QueryItem(Module.LOAN, Function.LN_ROAM);//借款标类型
				loanRoamItem.setFields("amount as min_amount,portion_total,portion_yes");
				loanRoamItem.setWhere(Where.eq("loan_id", loanList.get(i).get("id")));
				Map roamMap = this.baseService.getOne(loanRoamItem);
				map.putAll(roamMap);
			}
			
			map.put("status_name", status_name);
			map.put("url", url==null?""+map.get("id"):url+map.get("id"));
			//分享信息
			map.put("share_url", map.get("url"));
			map.put("share_content", loanInfo.getContents());
			map.put("share_title", loanList.get(i).get("name"));
			
			//协议
			/*map.put("agreement_title", "协议书");
			map.put("agreement_url", this.getWebDomain(request, "wap/loan/loanProtocol"));*/
			
			//新手专享附加信息
			if("1".equals(map.get("additional_status").toString())){
				map.put("additional_name", loanInfo.getAdditionalName() + "+" + loanInfo.getAdditionalApr() + "%");
				map.put("additional_apr", loanInfo.getAdditionalApr());
				map.put("additional_amount_max", loanInfo.getAdditionalAmountMax());
			}
			
		}
		return pageObj;
	}
	
	/**
	 * 投资列表(不含新手标)
	 * @param page
	 * @param loan_type
	 * @param amount_search
	 * @param apr_search
	 * @param period_search
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public Page getTenderNoNewHandList() throws Exception {
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		List<Where> whereList = new ArrayList<Where>();
//		List<NameValue> andList = new ArrayList<NameValue>();
//		andList.add(new NameValue("overdue_time", DateUtil.getCurrentTime(), Condition.GE));
//		andList.add(new NameValue("overdue_time", 0, Condition.EQ,true));
//		andList.add(new NameValue("status", 4, Condition.EQ, true));
//		whereList.add(Where.setAndList(andList));
		whereList.add(new Where("status",2,">"));
		whereList.add(Where.expression("((case when status = 3 then (overdue_time >="+ DateUtil.getCurrentTime() +") else (1=1) end) or overdue_time = 0 or status = 4)", false));
		whereList.add(new Where("status",2,">"));
		whereList.add(new Where("additional_status",1,"!="));
		whereList.add(new Where("hidden_status",1));
		whereList.add(Where.notEq("member_id", -1));
		loanItem.setFields("id,serialno,name,additional_status,apr,period,amount,repay_type,repay_type repay_type_name,status,status status_name,progress,category_id,category_id category_name,tender_count,vouch_company_id,credited_amount,category_type,marker_type");
		
		//搜索条件
		String orders = "status asc,additional_status desc,progress asc,add_time desc";
		loanItem.setOrders(orders);
		//查询流转标类型
		QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);//借款标类型
		loanCategoryItem.setFields("id,is_roam,pic,nid");
		List<Map> cateList = (List<Map>) this.baseService.getList(loanCategoryItem);
		
		loanItem.setLimit(2);
		loanItem.setWhere(whereList);
		Page pageObj = (Page) this.baseService.getPage(loanItem, Map.class);
		
		//判断天标和月标
		List<Map> listMap = pageObj.getItems();
		for(Map map:listMap){
			QueryItem liQueryItem = new QueryItem(Module.LOAN, Function.LN_LOANINFO);
			liQueryItem.setFields("id,contents");
			liQueryItem.setWhere(Where.eq("loan_id", map.get("id")));
			FnLoanInfo loanInfo = this.baseService.getOne(liQueryItem,FnLoanInfo.class);
			
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();	
			String url = this.getWebDomain(request,"wap/loan/appinfoview#?id=");
			map.put("url", url+map.get("id"));
			
			//分享信息
			map.put("share_url", url+map.get("id"));
			map.put("share_content", loanInfo.getContents());
			map.put("share_title",map.get("name"));
			
			int  categoryId= (Integer) map.get("category_id");
			DecimalFormat df =new DecimalFormat("######0.00");
			map.put("progress",df.format(map.get("progress")));
			map.put("apr",df.format(map.get("apr")) );
			for(Map categoryMap:cateList){
				if(Integer.valueOf(categoryMap.get("id").toString())==categoryId){					
					map.put("toDayAndMonth", categoryMap.get("nid").equals("day")?map.get("period")+"天":map.get("period")+"个月");
				};
			}
		}
		
		
		
		/*List<Map<String,Object>> loanList = pageObj.getItems();
		List<Map<String,Object>> tenderList = new ArrayList<Map<String,Object>>();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		for(int i = 0;i<loanList.size();i++){
			FnLoanInfo loanInfo = this.getLoanInfo((Long)loanList.get(i).get("id"));
			Map<String,Object> map = loanList.get(i);
			map.put("award_status", loanInfo.getAwardStatus());
			
			if(map.get("vouch_company_id").toString() != null && !"0".equals(map.get("vouch_company_id").toString())){
				MbGuaranteeCompany bondingCompany = null;
				QueryItem companyItem = new QueryItem(Module.MEMBER, Function.MB_VOUCHCOMPANY);
				companyItem.getWhere().add(Where.eq("id", map.get("vouch_company_id").toString()));
				bondingCompany = this.baseService.getOne(companyItem,  MbGuaranteeCompany.class);
				bondingCompany.setCompanyLogo(PropertiesUtil.getImageHost() + bondingCompany.getCompanyLogo());
				map.put("vouch_company_name", bondingCompany.getName());
			}
			for (Map cate : cateList) {
				if(cate.get("id") !=null && loanList.get(i).get("category_id")!=null && cate.get("id").toString().equals(loanList.get(i).get("category_id").toString())){
					map.put("is_roam", cate.get("is_roam"));
					if(cate.get("pic") != null && StringUtils.isNotBlank(String.valueOf(cate.get("pic")))){
						map.put("pic", PropertiesUtil.getImageHost() + cate.get("pic"));
					}
					break;
				}
			}
			String status = map.get("status").toString() ;
			String status_name = "";
			if ("3".equals(status)) {
				if(MapUtils.getInteger(map, "is_roam",0)==1){
					BigDecimal creditedAmount=new BigDecimal(String.valueOf(loanList.get(i).get("credited_amount")));
					BigDecimal borrowAmount=new BigDecimal(String.valueOf(loanList.get(i).get("amount")));
					if(creditedAmount.compareTo(borrowAmount)==0){
						status_name = "回购中";
					}else{
						status_name = "流转中";	
					}
					
				}else{
					status_name = "借款中";
				}
			} else if ("4".equals(status)) {
				status_name = "满标复审";
			} else if ("5".equals(status) || "6".equals(status)) {
				if((Integer)map.get("is_roam")==1){
					status_name = "回购中";
				}else{
					status_name = "还款中";
				}
			} else if ("7".equals(status)) {
				if((Integer)map.get("is_roam")==1){
					status_name = "回购完";
				}else{
					status_name = "已还完";
				}
			}
			//查询流转标信息
			if(MapUtils.getInteger(map, "is_roam",0)==1){
				QueryItem loanRoamItem = new QueryItem(Module.LOAN, Function.LN_ROAM);//借款标类型
				loanRoamItem.setFields("amount as min_amount,portion_total,portion_yes");
				loanRoamItem.setWhere(Where.eq("loan_id", loanList.get(i).get("id")));
				Map roamMap = this.baseService.getOne(loanRoamItem);
				map.putAll(roamMap);
			}
			
			map.put("status_name", status_name);
			map.put("url", url+map.get("id"));
			//分享信息
			map.put("share_url", map.get("url"));
			map.put("share_content", loanInfo.getContents());
			map.put("share_title", loanList.get(i).get("name"));
			
			//协议
			map.put("agreement_title", "协议书");
			map.put("agreement_url", this.getWebDomain(request, "wap/loan/loanProtocol"));
			
			//新手专享附加信息
			if("1".equals(map.get("additional_status").toString())){
				map.put("additional_name", loanInfo.getAdditionalName() + "+" + loanInfo.getAdditionalApr() + "%");
				map.put("additional_apr", loanInfo.getAdditionalApr());
				map.put("additional_amount_max", loanInfo.getAdditionalAmountMax());
			}
			
		}*/
		return pageObj;
	}
	
	/**
	 * 投资详情页面
	 * @param id
	 * @param login_token
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse investdata(String id,Integer login_token) throws Exception {
		if(login_token == null){
			return errorJsonResonse("登陆标示不能为空");
		}
		if(StringUtils.isBlank(id)){
			return errorJsonResonse("借款标ID不能为空");
		}
		
		//获取用户账户
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount fnAccount = this.baseService.getOne(accountItem, FnAccount.class);
		//获取借款标
		QueryItem loanItem = new QueryItem(Module.LOAN,Function.LN_LOAN);
		loanItem.setFields("id,name,amount,credited_amount,period,category_id,apr,repay_type,category_type");
		loanItem.getWhere().add(Where.eq("id", id));
		FnLoan fnLoan = this.baseService.getOne(loanItem, FnLoan.class);
		
		
		
		//获取借款明细表
		QueryItem loanInfoItem = new QueryItem(Module.LOAN,Function.LN_LOANINFO);
		loanInfoItem.getWhere().add(Where.eq("loan_id", fnLoan.getId()));
		FnLoanInfo fnLoanInfo = this.baseService.getOne(loanInfoItem, FnLoanInfo.class);
		String loanPassword = fnLoanInfo.getPassword();
		String is_password = "no";
		if(loanPassword !=null && !"".equals(loanPassword))is_password = "yes";//是否设置借款密码
		String is_paypwd = "no";
		if(user.getPaypassword() !=null && !"".equals(user.getPaypassword()))is_paypwd = "yes";//是否设置支付密码
		BigDecimal wait_amount = fnLoan.getAmount().subtract(fnLoan.getCreditedAmount());//当前可投金额
		BigDecimal balance_amount = fnAccount.getBalanceAmount();//账户总余额
		Integer period = fnLoan.getPeriod();//借款期限
		Long category_id = fnLoan.getCategoryId();//借款标类型
		//借款类型
		QueryItem queryItem  = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		queryItem.setFields("id,name");
		queryItem.setWhere(Where.eq("status", 1));
		queryItem.setWhere(Where.eq("id", fnLoan.getRepayType()));
		queryItem.setOrders("id");
		FnLoanRepayType repayType = (FnLoanRepayType) this.baseService.getOne(queryItem,FnLoanRepayType.class);
		
		Map roamMap = null;
		//查询流转标信息
		if("3".equals(fnLoan.getCategoryType())){
			QueryItem loanRoamItem = new QueryItem(Module.LOAN, Function.LN_ROAM);//借款标类型
			loanRoamItem.setFields("amount,portion_total,portion_yes");
			loanRoamItem.setWhere(Where.eq("loan_id", fnLoan.getId()));
			roamMap = this.baseService.getOne(loanRoamItem, Map.class);
		}	
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> memberMap = new HashMap<String,Object>();
		memberMap.put("is_paypwd", is_paypwd);
		memberMap.put("balance_amount", balance_amount);
		map.put("is_password", is_password);
		map.put("wait_amount", wait_amount);
		map.put("name", fnLoan.getName()) ;
		map.put("category_id", category_id);
		map.put("apr", fnLoan.getApr());
		map.put("category_type", fnLoan.getCategoryType());
		map.put("repay_type", fnLoan.getRepayType());
		map.put("award_scale",fnLoanInfo.getAwardAmount());
		map.put("award_proportion",fnLoanInfo.getAwardProportion());
		map.put("member", memberMap);
		map.put("roam_info", roamMap);
		map.put("tender_amount_min", fnLoanInfo.getTenderAmountMin());
		map.put("tender_amount_max", fnLoanInfo.getTenderAmountMax());
		map.put("repay_type_name", repayType.getName());
		map.put("period", period);
		map.put("award_status", fnLoanInfo.getAwardStatus());
		map.put("additional_apr", fnLoanInfo.getAdditionalApr()) ;
		map.put("additional_status", fnLoanInfo.getAdditionalStatus()) ;
		map.put("loan_id", id) ;
		if (repayType.getId() == 5) {
			map.put("period_name", period + "天");
		}else{
			map.put("period_name", period + "个月");
		}
		return successJsonResonse(map);
	}
	
	/**
	 * 投资收益
	 * @param amount
	 * @param period
	 * @param apr
	 * @param repay_type
	 * @param award_scale
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse investInterest(BigDecimal amount,Integer period,BigDecimal apr,Integer repay_type,Double award_scale,Integer additional_status, BigDecimal additional_apr, String login_token, String loan_id) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		BigDecimal interest_total = BigDecimal.ZERO;//预期收益
		BigDecimal interest_award = BigDecimal.ZERO;//利息
		BigDecimal award_amount = BigDecimal.ZERO;//奖励
		BigDecimal additionalAmount = BigDecimal.ZERO;//新手奖励
		if(award_scale !=null){
			award_scale = award_scale/100;
		}else{
			award_scale = 0D;
		}
		if(amount !=null){
			TenderCondition repayCondtion = new TenderCondition();
			repayCondtion.setAmount(amount);
			repayCondtion.setApr(apr);
			repayCondtion.setCurrentTime(DateUtil.getCurrentTime());
			repayCondtion.setPeriod(period);
			repayCondtion.setRepayType(repay_type);
			Tender tender = RepayUtil.getRepayInfo(repayCondtion);
			if(tender !=null){
				interest_award = tender.getInterestAll();
			}
			award_amount = NumberUtils.mul(amount, new BigDecimal(award_scale));
			interest_total = interest_award.add(award_amount);
			if (null!=additional_status&&additional_status == 1) {
				repayCondtion.setApr(additional_apr);
				tender = RepayUtil.getRepayInfo(repayCondtion);
				if (tender != null) {
					additionalAmount = tender.getInterestAll();
				}
			}
		}
		map.put("interest_total", interest_total.setScale(2, BigDecimal.ROUND_HALF_UP));
		map.put("interest_award", interest_award.setScale(2, BigDecimal.ROUND_HALF_UP));
		map.put("award_amount", award_amount.setScale(2, BigDecimal.ROUND_HALF_UP));
		map.put("additional_amount", additionalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
		
		//符号条件的红包个数
		map.put("red_num", bountyNum(amount, login_token, loan_id));
		
		return successJsonResonse(map);
	}
	
	/**
	 * 符号条件的红包个数
	 * @param amount
	 * @param loginToken
	 * @param loanId
	 * @return
	 * @throws Exception
	 */
	public Integer bountyNum(BigDecimal amount, String loginToken, String loanId) throws Exception {
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
		List<FnBounty> bountyList = baseService.getList(queryItem, FnBounty.class);
		
		Integer count = 0;
		for (FnBounty bounty : bountyList) {
			//手动赠送红包处理(针对适合标类型)
			if(!StringUtils.isBlank(bounty.getLoanType()) && bounty.getLoanType().contains(String.valueOf(fnLoan.getCategoryId())) && amount.compareTo(bounty.getAmountMin()) >= 0){
				count++;
			}else if(StringUtils.isBlank(bounty.getLoanType()) && amount.compareTo(bounty.getAmountMin()) >= 0){
				count++;
			}else{
				continue;
			}
		}
		
		return count;
	}
	
	
	/**
	 * 投资提交
	 * @param login_token
	 * @param id
	 * @param amount
	 * @param paypassword
	 * @param password
	 * @param depositCertificate
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse invest(Integer login_token,Integer id,BigDecimal amount,String paypassword,String password,String depositCertificate, String redId) throws Exception {
		//验证
		MbMember user = this.getMbMember(Long.valueOf(login_token));
		if(user == null) return errorJsonResonse("尚未登录!");
		if(user.getIsRealname()==-1)return errorJsonResonse("尚未实名认证");
		if(user.getIsPhone()==-1)return errorJsonResonse("尚未进行手机认证");
		//资金账户
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		//accountItem.setFields("balance_amount");
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount fnAccount =  this.baseService.getOne(accountItem, FnAccount.class);
		//借款标
		QueryItem loanItem = new QueryItem("loan","loan");
		//loanItem.setFields("amount,credited_amount,member_id,id");
		loanItem.getWhere().add(Where.eq("id", id));
		FnLoan fnLoan =this.baseService.getOne(loanItem, FnLoan.class);
		//判断是否满标
		if(fnLoan.getProgress().compareTo(new BigDecimal(100)) == 0)return errorJsonResonse("该标已被投满");
		BigDecimal balanceAmount = fnAccount.getBalanceAmount();//可用余额
		BigDecimal waitAmount = fnLoan.getAmount().subtract(fnLoan.getCreditedAmount());//当前可投金额
		BigDecimal com = new BigDecimal(10);
		if(amount == null)return errorJsonResonse("投标金额不能为空!");
		if(amount.compareTo(com) < 0)return errorJsonResonse("投标金额不能小于10!");
		if(amount.remainder(com).compareTo(new BigDecimal(0)) != 0)return errorJsonResonse("投标金额不是10的倍数!");
		if(amount.compareTo(waitAmount)>0)return errorJsonResonse("投标金额不能大于可投金额!");
		if(balanceAmount.compareTo(com)<0 ||balanceAmount.compareTo(amount)<0)return errorJsonResonse("可用余额不足!");
		if(paypassword == null && "".equals(paypassword))return errorJsonResonse("支付密码不能为空!");
		if(fnLoan.getMemberId().equals(user.getId()))return errorJsonResonse("不能投自己的标");
		//借款标详情
		QueryItem loanInfoItem = new QueryItem(Module.LOAN,Function.LN_LOANINFO);
		loanInfoItem.setFields("id,password,additional_status,additional_amount_max,tender_amount_max");
		loanInfoItem.getWhere().add(Where.eq("loan_id", fnLoan.getId()));
		FnLoanInfo fnLoanInfo = this.baseService.getOne(loanInfoItem, FnLoanInfo.class);
		String loanPassword = null;
		Integer additionalStatus = null ;
		BigDecimal additionalAmountMax = null ;
		if(fnLoanInfo != null){
			BigDecimal tenderAmountMax = fnLoanInfo.getTenderAmountMax();
			if(tenderAmountMax.compareTo(BigDecimal.ZERO) > 0 && tenderAmountMax.compareTo(amount) < 0){
				return errorJsonResonse("投标金额不能超过最高投标金额");
			}
			loanPassword = fnLoanInfo.getPassword();
			additionalStatus = fnLoanInfo.getAdditionalStatus() ;
			additionalAmountMax = fnLoanInfo.getAdditionalAmountMax();
		}
		
		//判断是否是新手标
		if (null != additionalStatus && 1 == additionalStatus) {
			/*QueryItem tenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
			tenderItem.setFields("count(-1) row_count");
			tenderItem.getWhere().add(Where.eq("member_id", user.getId()));
			tenderItem.getWhere().add(Where.notEq("loan_id", fnLoan.getId()));
			tenderItem.getWhere().add(Where.notEq("status", -1));
			Map<String, Object> tenderMap = this.baseService.getOne(tenderItem, Map.class);
			if (null != tenderMap && Integer.valueOf(tenderMap.get("row_count").toString()) > 0) {
				return errorJsonResonse("您不是新手无法对新手标进行投标");
			}*/
			if (NumberUtils.greaterThanZero(additionalAmountMax)) {
				QueryItem sumTenderItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
				sumTenderItem.setFields("sum(amount) tender_sum_amount");
				sumTenderItem.getWhere().add(Where.eq("member_id", user.getId()));
				sumTenderItem.getWhere().add(Where.eq("loan_id", fnLoan.getId()));
				sumTenderItem.getWhere().add(Where.notEq("status", -1));
				Map<String, Object> sumMap = this.baseService.getOne(sumTenderItem);
				BigDecimal tenderSumAmount = amount;
				BigDecimal hasTenderAmount = BigDecimal.ZERO;
				if (sumMap != null && sumMap.get("tender_sum_amount") != null) {
					tenderSumAmount = tenderSumAmount.add((BigDecimal) sumMap.get("tender_sum_amount"));
					hasTenderAmount = (BigDecimal) sumMap.get("tender_sum_amount");
				}
				if (NumberUtils.greaterThan(tenderSumAmount, additionalAmountMax)) {
					BigDecimal amountMax = BigDecimal.ZERO;
					if(additionalAmountMax.compareTo(tenderSumAmount) < 0){
						amountMax = NumberUtils.sub(additionalAmountMax,hasTenderAmount);
					}
					//return createErrorJsonResonse("累计投标金额" + NumberUtils.format(tenderSumAmount) + "超过新手标投资上限" + NumberUtils.format(additionalAmountMax) + "");
					return errorJsonResonse("您最多还能投资"+NumberUtils.round(amountMax)+"元");
				}
			}
		}
		//判断投资密码
		if(loanPassword !=null && !"".equals(loanPassword)){
			if(password == null)return errorJsonResonse("投资密码不能为空");
			if(!password.equals(loanPassword))return errorJsonResonse("投资密码错误");
		}
		//判断支付密码
		if(!SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword)).equals(user.getPaypassword())){
			return errorJsonResonse("支付密码错误!");
		}
		//修改借款标,添加投资数据，冻结用户投资金额，添加资金记录
		financeService.invest(user,fnLoan,amount,IpUtil.ipStrToLong(GetUtils.getRemoteIp()),null,StringUtils.isNotBlank(depositCertificate)? Integer.valueOf(depositCertificate):null, redId);
		return successJsonResonse("投标成功!");
	}
	
	/**
	 *投资流转标
	 */
	public DyPhoneResponse investRoam(boolean isTrust, Long memberId,Integer id,Integer number,String paypassword,String password,String depositCertificate,String redId) throws Exception {
		//验证
		MbMember user = this.getMbMember(memberId);
		if(user.getIsRealname()==-1)return errorJsonResonse("尚未实名认证");
		if(user.getIsPhone()==-1)return errorJsonResonse("尚未进行手机认证");
		//资金账户
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount fnAccount = this.baseService.getOne(accountItem, FnAccount.class);
		//借款标
		QueryItem loanItem = new QueryItem( Module.LOAN, Function.LN_LOAN);
		//loanItem.setFields("amount,credited_amount,member_id,id");
		loanItem.getWhere().add(Where.eq("id", id));
		FnLoan fnLoan =  this.baseService.getOne(loanItem, FnLoan.class);
		//判断是否满标
		if(fnLoan.getProgress().compareTo(new BigDecimal(100)) == 0)return errorJsonResonse("该标已被投满");
		if(number == null)return errorJsonResonse("投标份数不能为空!");
		BigDecimal buyNum = new BigDecimal(number);
		//流转标信息
		QueryItem roamItem = new QueryItem(Module.LOAN, Function.LN_ROAM) ;
		roamItem.getWhere().add(Where.eq("loan_id", fnLoan.getId())) ;
		FnLoanRoam roam = this.baseService.getOne(roamItem, FnLoanRoam.class) ;
		
		BigDecimal balanceAmount = fnAccount.getBalanceAmount();//可用余额
		Integer waitNumber = roam.getPortionTotal() - roam.getPortionYes();//当前可投份数
		BigDecimal amount =  NumberUtils.mul(roam.getAmount(),buyNum)  ; //投标金额
		
		if(!NumberUtils.greaterThanZero(buyNum))return errorJsonResonse("投标份数不能小于1!");
		if(number.intValue() > waitNumber)return errorJsonResonse("投标份数不能大于可投份数!");
		if(balanceAmount.compareTo(amount)<0)return errorJsonResonse("可用余额不足!");
		if(paypassword == null && "".equals(paypassword))return errorJsonResonse("支付密码不能为空!");
		if(fnLoan.getMemberId().equals(user.getId()))return errorJsonResonse("不能投自己的标");
		//借款标详情
		QueryItem loanInfoItem = new QueryItem(Module.LOAN,Function.LN_LOANINFO);
		loanInfoItem.setFields("id,password,additional_status");
		loanInfoItem.getWhere().add(Where.eq("loan_id", fnLoan.getId()));
		FnLoanInfo fnLoanInfo = this.baseService.getOne(loanInfoItem,FnLoanInfo.class);
		String loanPassword = null;
		//Integer additionalStatus = null ;
		if(fnLoanInfo != null){
			loanPassword = fnLoanInfo.getPassword();
		//	additionalStatus = fnLoanInfo.getAdditionalStatus() ;
		}
		//判断是否是新手标
		/*if(null != additionalStatus && 1 == additionalStatus){
			QueryItem tenderItem = new QueryItem(Module.LOAN,Function.LN_TENDER);
			tenderItem.getWhere().add(Where.eq("member_id", user.getId()));
			tenderItem.getWhere().add(Where.notEq("status", -1));
			List<FnTender> tenders = this.baseService.getList(tenderItem, FnTender.class);
			if(null != tenders && 0 < tenders.size()){
				return errorJsonResonse("不是新手无法对新手标进行投标!");
			}
		}*/
		//判断投资密码
		if(loanPassword !=null && !"".equals(loanPassword)){
			if(password == null)return errorJsonResonse("投资密码不能为空");
			if(!password.equals(loanPassword))return errorJsonResonse("投资密码错误");
		}
		//判断支付密码
		if(!SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword)).equals(user.getPaypassword())){
			return errorJsonResonse("支付密码错误!");
		}
		//修改借款标,添加投资数据，冻结用户投资金额，添加资金记录
		TenderVo tenderVo = new TenderVo();
		tenderVo.setFnLoan(fnLoan);
		tenderVo.setMember(user);
		tenderVo.setIsTrust(isTrust);
		tenderVo.setIsAuto(false);
		tenderVo.setAmount(amount);
		tenderVo.setRedId(redId);
		financeService.investRoam(tenderVo);
		return successJsonResonse("投标成功!");
	}
	
	/**
	 * 获取协议
	 * @param loanId
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> getAgreeInfo(Long loanId) throws Exception{
		FnLoan loan = getLoan(loanId);
		QueryItem cateItem = new QueryItem();
		cateItem.getWhere().add(Where.eq("id", loan.getCategoryId()));
		cateItem.setFields("nid");
		FnLoanCategory loanCate = this.getOneByEntity(cateItem, Module.LOAN, Function.LN_CATEGORY,FnLoanCategory.class);
		//查询协议书
		QueryItem agreeItem = new QueryItem(Module.CONTENT, Function.CT_AGREEMENT);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("type", loanCate.getNid()));
		agreeItem.setWhere(whereList);
		CtAgreement agreement = this.baseService.getOne(agreeItem, CtAgreement.class);
		//借款信息
		Map<String,Object> loanMap = new HashMap<String,Object>();
		Long serialno = loan.getSerialno();
		String member_name = loan.getMemberName();
		String repay_type = getRepayStatus(loan.getRepayType());
		Long reverify_time = loan.getReverifyTime();
		String web_name = getSysConfig("site_name");
		String borrow_period_name = "";
		Long repay_last_time = null;
		Integer repayType = loan.getRepayType();
		if (repayType == 5) {
			borrow_period_name = loan.getPeriod()+"天";
			repay_last_time = DateUtil.addDay(loan.getReverifyTime(), loan.getPeriod());
		} else {
			borrow_period_name = loan.getPeriod()+"个月";
			repay_last_time = DateUtil.addMonth(loan.getReverifyTime(), loan.getPeriod());
		}
		BigDecimal apr = loan.getApr();
		BigDecimal credited_amount = new BigDecimal(0);
		BigDecimal repayAmount = new BigDecimal(0);
		//出借人列表
		List<FnTender> tenderList = getTenderList(loan.getId());
		for(FnTender tender:tenderList){
			credited_amount = credited_amount.add(tender.getAmount());
			repayAmount = repayAmount.add(tender.getRecoverAmount());
		}
		//债券转让明细
		QueryItem queryItem=new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		queryItem.setWhere(new Where("loan_id",loanId ));
		queryItem.setFields("member_name,buy_member_name,amount,success_time");
		List<Map> item=(List<Map>)new DataConvertUtil(this.baseService.getList(queryItem, Map.class)).setDate("success_time");
		//还款明细
		List<FnLoanRepayPeriod> repayList = getRepayList(loan.getId());
		loanMap.put("serialno", serialno);
		loanMap.put("member_name", member_name);
		loanMap.put("reverify_time", reverify_time);
		loanMap.put("web_name", web_name);
		loanMap.put("repay_type", repay_type);
		loanMap.put("borrow_period_name", borrow_period_name);
		loanMap.put("repay_last_time", repay_last_time);
		loanMap.put("apr", apr);
		loanMap.put("credited_amount", credited_amount);
		loanMap.put("repayAmount", repayAmount);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("agreement", agreement);
		map.put("loan_info", loanMap);
		map.put("tender", tenderList);
		map.put("repay", repayList);
		if(item!=null && item.size()>0){
			map.put("transfre",item);
		}		
		return map;
	}
	
	
	/**
	 * 根据loanId查询tenderList
	 * @throws Exception 
	 */
	private List<FnTender> getTenderList(Long loanId) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_TENDER);
		item.getWhere().add(Where.eq("loan_id", loanId));
		List<FnTender> tenderList = this.baseService.getList(item, FnTender.class);
		return tenderList;
	}
	/**
	 * 根据loanId获取loaninfo
	 * @throws Exception 
	 */
	private FnLoanInfo getLoanInfo(Long loanId) throws Exception{
		QueryItem liQueryItem = new QueryItem(Module.LOAN, Function.LN_LOANINFO);
		liQueryItem.setFields("id,award_status,vouch_company_info,additional_apr,additional_name,additional_amount_max");
		liQueryItem.setWhere(Where.eq("loan_id", loanId));
		FnLoanInfo loanInfo = this.baseService.getOne(liQueryItem,FnLoanInfo.class);
		return loanInfo;
	}
	
	/**
	 * 根据id获取loan
	 * @throws Exception
	 */
	private FnLoan getLoan(Long id) throws Exception{
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		loanItem.getWhere().add(Where.eq("id", id));
		FnLoan loan = this.baseService.getOne(loanItem, FnLoan.class);
		return loan;
	}
	/**
	 * 根据loanID获取借款明细
	 * @throws Exception 
	 */
	private List<FnLoanRepayPeriod> getRepayList(Long loanId) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_REPAYPERIOD);
		item.getWhere().add(Where.eq("loan_id", loanId));
		List<FnLoanRepayPeriod> repayList = this.baseService.getList(item, FnLoanRepayPeriod.class);
		return repayList;
	}
	/**
	 * 根据id获取还款方式
	 * @throws Exception
	 */
	private String getRepayStatus(Integer id) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		item.setFields("name");
		item.getWhere().add(Where.eq("id", id));
		FnLoanRepayType repayType = this.baseService.getOne(item, FnLoanRepayType.class);
		return repayType.getName();
	}
	
	/**
	 * 获取系统配置
	 * @throws Exception 
	 */
	private String getSysConfig(String type) throws Exception{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		item.getWhere().add(Where.eq("nid", type));
		SysSystemConfig config = this.baseService.getOne(item,SysSystemConfig.class);
		return config.getValue();
	} 
	
	/**
	 * 拼接网站访问地址
	 * @param url
	 * @return
	 */
	private String getWebDomain(HttpServletRequest request, String url) {
		String domain = request.getScheme() + "://" + request.getServerName();
		int port = request.getServerPort();
		if (port == 80) {
			domain = domain + request.getContextPath() + "/" + url;
		} else {
			domain = domain + ":" + request.getServerPort() + request.getContextPath() + "/" + url;
		}
		return domain;
	}
}
