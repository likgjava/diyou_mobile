package com.dy.baf.service.loan;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanApply;
import com.dy.baf.entity.common.FnLoanCategory;
import com.dy.baf.entity.common.FnLoanRepay;
import com.dy.baf.entity.common.FnLoanRepayPeriod;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemAreas;
import com.dy.baf.entity.custom.LoanParam;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Option;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.DyHttpClient;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.OptionUtil;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.StringUtils;
import com.dy.core.utils.serializer.SerializerUtil;
import com.dy.httpinvoker.LoanService;
import com.google.gson.Gson;

/**
 * 我的借款
 * 
 * @Description: TODO
 * @author 波哥
 * @date 2015年9月23日 下午12:53:50
 * @version V1.0
 */
@Service("mobileLoanService")
public class LoanMobileService extends MobileService {

	@Autowired
	private BaseService baseService;
	@Autowired
	private LoanService loanService;
	@Autowired
	private OptionUtil optionUtil;

	/**
	 * 我的借款列表
	 * 
	 * @param memberId
	 * @param pageNumStr
	 * @param status
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getMyLoanList(String memberId, String pageNumStr, String status, String startTime,
			String endTime) throws Exception {

		MbMember member = this.getMbMember(Long.valueOf(memberId));
		int pageNum = 1;
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}

		String innerStatus = "";
		if ("1".equals(status)) {
			//1发标待审,-10 初审驳回,4满标待审,2初审通过自动投标中,3借款中,4满标待审, -7 锁定中
			innerStatus = "1,-10,4,2, 3,-7, 4";
		} else if ("2".equals(status)) {
			//6还款中
			innerStatus = "6";
		} else if ("3".equals(status)) {
			//-1初审失败,-2借款到期未满标,-3复审失败不合格标,-4用户撤标（流标）,-5过期后台撤标（流标）-6 未过期后台撤标（流标） -8（复审失败 后台处理中）-9 (后台撤标 后台处理中),7已还完 
			innerStatus = "-1,-2,-3,-4,-5,-6,-8,-9,7";
		}
		
		
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		queryItem.setFields(
				"id,ind,serialno,name,amount,credited_amount,status,status status_name,add_time,category_id,reverify_time,period,repay_type");
		queryItem.setWhere(Where.in("status", innerStatus));
		queryItem.setWhere(Where.eq("member_id", member.getId()));
		List<NameValue> andList = new ArrayList<NameValue>();
		if (StringUtils.isNotBlank(startTime)) {
			andList.add(new NameValue("add_time", DateUtil.convert(startTime), ">="));
		}
		if (StringUtils.isNotBlank(endTime)) {
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(DateUtil.dateParse(endTime));
			endCal.add(Calendar.DAY_OF_MONTH, 1);
			andList.add(new NameValue("add_time", DateUtil.convert(endCal.getTime()), "<"));
		}
		if (andList.size() > 0) {
			queryItem.setWhere(Where.setAndList(andList));
		}
		
		//流转标的跟普通标的借款中、还款中不一致
		//借款中包含流转标的流转中，在借款人眼中未凑满的流转标叫流转中
		//还款中包含流转标的回购中，在借款人眼中凑满的流转标叫回购中
		//在主查询中，借款中要排除掉回购中的借款标，还款中要排除掉流转中的流转标，只记录流转标中回购标
		String inLoanIdStr="";	
		if("1".equals(status)||"2".equals(status)){//流转中 | 回购中
			QueryItem queryRoamconn=new QueryItem();
			queryRoamconn.setFields("id,status,category_type,amount,credited_amount");
			queryRoamconn.setWhere(Where.eq("member_id", member.getId()));
			queryRoamconn.getWhere().add(new Where("status", "2,3",IN));
			queryRoamconn.getWhere().add(new Where("category_type", "3",EQ));
			List<FnLoan> queryLoanRoamList=this.getListByEntity(queryRoamconn, Module.LOAN, Function.LN_LOAN, FnLoan.class);

			//记录回购中的借款标的id
			for(int i=0;i<queryLoanRoamList.size();i++){
					if(queryLoanRoamList.get(i).getAmount().compareTo(queryLoanRoamList.get(i).getCreditedAmount())==0){//借款金额等于到账金额
						inLoanIdStr=inLoanIdStr+queryLoanRoamList.get(i).getId()+",";							
					}	
										
			}
			if(StringUtils.isNotBlank(inLoanIdStr)){
				inLoanIdStr=inLoanIdStr.length()>1?inLoanIdStr.substring(0,inLoanIdStr.length()-1):inLoanIdStr;
				List<Where> whereList = new ArrayList<Where>();
				List<NameValue> ands = new ArrayList<NameValue>();
				ands.add(new NameValue("status",innerStatus,IN,false));
				if("loan".equals(status)){//借款中排除掉流转标的回购
					ands.add(new NameValue("id",inLoanIdStr,NOT_IN,false));//false表示且
				}else if("repay".equals(status)){//还款中包含流转标的回购中
					ands.add(new NameValue("id",inLoanIdStr,IN,true));//true表示或
				}				
				whereList.add(new Where(ands));
				queryItem.getWhere().addAll(whereList);					
			}else{
				queryItem.getWhere().add(Where.in("status", innerStatus));
			}

		}else{
			queryItem.getWhere().add(Where.in("status", innerStatus));
			
		}
		
		
		queryItem.setOrders("add_time desc");
		queryItem.setPage(pageNum);
		queryItem.setLimit(6);
		Page page = this.baseService.getPage(queryItem, Map.class);
		List<Map<String, Object>> loanList = (List<Map<String, Object>>) page.getItems();
		
	
		
		//获取借款标状态集合
		Map<String,String> borrowStatusNameMap=new HashMap<String, String>();
		List<Option> borrowStatusNameList=optionUtil.getBorrowStatus();
		for(Option option:borrowStatusNameList){
			borrowStatusNameMap.put(String.valueOf(option.getValue()), option.getText());
		}
		for (Map<String, Object> map : loanList) {
			//翻译借款标状态		
			map.put("status_name", borrowStatusNameMap.get(String.valueOf(map.get("status_name"))));
			if("3".equals(String.valueOf(map.get("category_type")))){//流转标
				if("repay".equals(status)){
					map.put("status_name", "回购中");
				}else if("loan".equals(status)){
					map.put("status_name", "流转中");
				}
			}
			
			//下期还款时间
			if("2".equals(status) || "3".equals(status)){
				map.putAll(getNexRepayTime(map.get("id").toString()));
			}
			
			map.putAll(this.getLoanInfoByLoanId(map.get("id").toString()));
					
		}
		return successJsonResonse(new DataConvertUtil(page).setStatus("status_name", optionUtil.getBorrowStatus())
				.setDate("add_time", "yyyy-MM-dd").convert());
	}

	/**
	 * 借款标种类型列表
	 * 
	 * @return
	 */
	public DyPhoneResponse loanCategoryList() throws Exception {
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);
		queryItem.setFields(
				"id,name,description,amount_category_id,amount_category_id amount_category_name,loan_amount_min,loan_amount_max,apr_min,apr_max,period_min,period_max,period_days_min,period_days_max,repay_type");
		queryItem.getWhere().add(Where.eq("status", 1));
		queryItem.getWhere().add(Where.eq("admin_status", -1));
		queryItem.setOrders("id asc");
		Page pageResponse = this.baseService.getPage(queryItem, Map.class);
		return successJsonResonse(new DataConvertUtil(pageResponse)
				.setStatus("amount_category_name", optionUtil.getAmountCate()).convert());
	}

	/**
	 * 借款标种详情
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse loanCategoryInfo(String id) throws Exception {
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);
		queryItem.setFields(
				"id,name,description,amount_category_id,amount_category_id,loan_amount_min,loan_amount_max,apr_min,apr_max,period_min,period_max,period_days_min,period_days_max,repay_type");
		queryItem.getWhere().add(Where.eq("status", 1));
		queryItem.getWhere().add(Where.eq("admin_status", -1));
		queryItem.getWhere().add(Where.eq("id", id));
		queryItem.setOrders("id asc");
		Map map = this.baseService.getOne(queryItem, Map.class);

		// 还款方式
		QueryItem repayItem = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		repayItem.setWhere(Where.in("id", map.get("repay_type")));
		
		repayItem.setFields("id,name");
		List repayList = this.baseService.getList(repayItem, Map.class);
		map.put("repay_type_list", repayList);
		return successJsonResonse(map);
	}

	/**
	 * 借款申请第二步
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse loanSecond(String id) throws Exception {

		// 获取标种
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);
		queryItem.setFields(
				"id,name,cardinality,loan_amount_min,loan_amount_max,apr_min,apr_max," +
				"period_min,period_max,period_days_min,period_days_max,validate_min," +
				"validate_max,tender_amount_min,tender_amount_max,award_proportion_min," +
				"award_proportion_max,award_status,award_amount_min,award_amount_max,repay_type");
		queryItem.getWhere().add(Where.eq("id", id));
		Map<String, Object> loanCategory = this.baseService.getOne(queryItem);

		// 还款方式
		QueryItem repayItem = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		repayItem.setWhere(Where.in("id", loanCategory.get("repay_type")));
		repayItem.setFields("id,name,contents,status");
		List repayList = this.baseService.getList(repayItem, Map.class);
		loanCategory.put("repay_type_list", repayList);
		return successJsonResonse(loanCategory);
	}

	/**
	 * 借款申请第三步 获取担保公司
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse loanThird(String id) throws Exception {

		// 获取标种
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);
		queryItem.setFields("is_roam,id,name,is_vouch");
		queryItem.getWhere().add(Where.eq("id", id));
		Map<String, Object> loanCategory = this.baseService.getOne(queryItem);

		/**
		 * 判断该标种是否是担保标
		 */
		if ("1".equals(loanCategory.get("is_vouch").toString()) || "1".equals(loanCategory.get("is_roam").toString())) {
			QueryItem comItem = new QueryItem(Module.MEMBER, Function.MB_VOUCHCOMPANY);
			comItem.setFields("id,name");
			comItem.getWhere().add(Where.eq("status", 1));
			List<Map> companyList = this.baseService.getList(comItem, Map.class);
			loanCategory.put("vouch_company_list", companyList);
		}

		/**
		 * 借款用途
		 */
		QueryItem useTypeQueryItem = new QueryItem(Module.SYSTEM, Function.SYS_LINKAGE);
		useTypeQueryItem.setFields("id,name");
		useTypeQueryItem.getWhere().add(Where.eq("status", 1));
		useTypeQueryItem.getWhere().add(Where.eq("pid", 101));
		useTypeQueryItem.setOrders("sort_index");
		List useType = this.baseService.getList(useTypeQueryItem, Map.class);
		loanCategory.put("use", useType);
		return successJsonResonse(loanCategory);
	}

	/**
	 * 借款信息提交
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse loanSub(Map<String, String> paramsMap, Boolean isTrust, Long ip) throws Exception {

		String login_token = paramsMap.get("login_token");
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("登录标识不能为空");
		}

		MbMember member = this.getMbMember(Long.valueOf(login_token));
		String borrowAmtStr = paramsMap.get("amount");
		if (StringUtils.isBlank(borrowAmtStr)) {
			return errorJsonResonse("借款金额不能为空");
		}
		String borrowAprStr = paramsMap.get("apr");
		if (StringUtils.isBlank(borrowAprStr)) {
			return errorJsonResonse("年利率不能为空");
		}
		String borrowPeriodStr = paramsMap.get("period");
		if (StringUtils.isBlank(borrowPeriodStr)) {
			return errorJsonResonse("借款期限不能为空");
		}
		String borrowValidateStr = paramsMap.get("validate");
		if (StringUtils.isBlank(borrowValidateStr)) {
			return errorJsonResonse("筹标期限不能为空");
		}
		/*
		 * String tenderAmountMinStr = paramsMap.get("tender_amount_min"); if
		 * (StringUtils.isBlank(borrowValidateStr)) { return
		 * createErrorJsonResonse(this.getMessage("validate.null", new String[]
		 * { "最低投资金额" })); }
		 */

		QueryItem cateItem = new QueryItem();
		cateItem.getWhere().add(Where.eq("id", paramsMap.get("id")));
		cateItem.setFields("id,is_vouch,is_roam");
		FnLoanCategory loanCate = this.getOneByEntity(cateItem, Module.LOAN, Function.LN_CATEGORY,
				FnLoanCategory.class);
		String categoryType = "1";
		if (loanCate.getIsRoam() == 1) {
			categoryType = "3";
		} else if (loanCate.getIsVouch() == 1) {
			categoryType = "2";
		}
		// 获取图像地址
		String imgPath = PropertiesUtil.getImageHost();
		String attachment_ids = StringUtils.isNotBlank(paramsMap.get("attachment_ids"))
				? paramsMap.get("attachment_ids") : "";
		if ("2".equals(categoryType) && "".equals(attachment_ids)) {
			return errorJsonResonse("项目材料不能为空");
		}
		attachment_ids = attachment_ids.replace(imgPath, "");
		String[] images = attachment_ids.split(";");
		List<Map> attachmentList = new ArrayList<Map>();
		for (String str : images) {
			Map<String,Object> strMap = new HashMap<String, Object>();
			strMap.put("imgurl", str);
			strMap.put("minimg", str);
			attachmentList.add(strMap);
		}
		attachment_ids = new String(new SerializerUtil().serialize(attachmentList));

		String tenderAmountMaxStr = paramsMap.get("tender_amount_max");
		if (loanCate.getIsRoam() != 1) {
			if (StringUtils.isBlank(tenderAmountMaxStr)) {
				return errorJsonResonse("最高投标金额不能为空");
			}
		}

		LoanParam loanParam = new LoanParam();
		loanParam.setIsTrust(isTrust);
		loanParam.setMemberId(member.getId());
		loanParam.setMemberName(member.getName());
		loanParam.setLoanTitle(paramsMap.get("name"));
		loanParam.setCategoryId(loanCate.getId());
		loanParam.setCategoryType(categoryType);
		loanParam.setBorrowAmount(new BigDecimal(borrowAmtStr));
		loanParam.setBorrowApr(new BigDecimal(borrowAprStr));
		loanParam.setBorrowUse(Integer.valueOf(paramsMap.get("use")));
		loanParam.setBorrowValidate(Integer.valueOf(borrowValidateStr));
		loanParam.setBorrowPeriod(Integer.valueOf(borrowPeriodStr));
		loanParam.setBorrowPassword(paramsMap.get("password"));
		loanParam.setRepayType(Integer.valueOf(paramsMap.get("repay_type")));
		loanParam.setTenderAmountMin(new BigDecimal(10));
		loanParam
				.setTenderAmountMax(loanCate.getIsRoam() == 1 ? new BigDecimal(0) : new BigDecimal(tenderAmountMaxStr));// 最高投资
		loanParam.setAttachmentIds(attachment_ids);

		if (loanCate.getIsRoam() == 1) {// 流转标判断

			// 最小流转金额的判断
			String tender_roam_min = paramsMap.get("tender_roam_min");
			if (StringUtils.isBlank(tender_roam_min)) {
				return errorJsonResonse("最小流转金额不能为空!");
			}
			// 如果为流转标的时候//判断借款标类型，补充相应信息
			if (StringUtils.isBlank(tender_roam_min) || !tender_roam_min.matches("[0-9]+")) {
				return errorJsonResonse("最小流转单位必须是整数且不能为空!");
			}
			Double yuShu = Double.parseDouble(borrowAmtStr) % Double.parseDouble(tender_roam_min);
			if (yuShu != 0) {
				return errorJsonResonse("最小流转单位必须能够将借款金额整除!");
			}
			loanParam.setAmountMin(BigDecimal.valueOf(Double.parseDouble(tender_roam_min)));// 最小流转金额

			// 反担保方式 vouch_style
			String vouchStyle = paramsMap.get("vouch_style");
			if (StringUtils.isBlank(vouchStyle)) {
				return errorJsonResonse("反担保方式");
			}
			loanParam.setVouchStyle(vouchStyle);

			// 担保公司id vouch_company_id
			String vouchCompanyId = paramsMap.get("vouch_company_id");
			if (StringUtils.isBlank(vouchCompanyId)) {
				return errorJsonResonse("担保公司");
			}
			loanParam.setVouchCompanyId(Long.parseLong(vouchCompanyId));

			// 借款方资产情况 assets
			String assets = paramsMap.get("assets");
			if (StringUtils.isBlank(assets)) {
				return errorJsonResonse("借款方资产情况");
			}
			loanParam.setAssets(assets);

			// 借款方资金用途 assets_use
			String assetsUse = paramsMap.get("assets_use");
			if (StringUtils.isBlank(assetsUse)) {
				return errorJsonResonse("借款方资金用途");
			}
			loanParam.setAssetsUse(assetsUse);

			// 风险控制措施 risk
			String risk = paramsMap.get("risk");
			if (StringUtils.isBlank(risk)) {
				return errorJsonResonse("风险控制措施");
			}
			loanParam.setRisk(risk);

		} else if (loanCate.getIsVouch() == 1) {// 担保标
			loanParam.setVouchCompanyId(Long.valueOf(paramsMap.get("vouch_company_id")));
		}

		Integer awardStatus = Integer.valueOf(paramsMap.get("award_status"));
		if (awardStatus == 1) {
			loanParam.setAwardAmount(new BigDecimal(paramsMap.get("award_amount")));
		} else if (awardStatus == 2) {
			loanParam.setAwardScale(new BigDecimal(paramsMap.get("award_proportion")));
		}
		loanParam.setAwardStatus(awardStatus);
		loanParam.setBorrowContents(paramsMap.get("contents"));
		loanParam.setOptIp(ip);
		loanParam.setIsCompany(Integer.valueOf(paramsMap.get("is_company")));
		if (StringUtils.isNotBlank(paramsMap.get("certificate"))) {
			loanParam.setDepositCertificate(Integer.valueOf(paramsMap.get("certificate")));
			// //查询借款存证费用
			// BigDecimal certFee = getCertFee();
			// QueryItem item = new QueryItem();
			// item.getWhere().add(Where.eq("member_id", member.getId()));
			// FnAccount account = this.getOneByEntity(item, Module.FINANCE,
			// Function.FN_ACCOUNT, FnAccount.class);
			// if(account.getBalanceAmount().compareTo(certFee) < 0){
			// return createErrorJsonResonse("可用余额不足支付存证费用");
			// }
		} else {
			loanParam.setDepositCertificate(-1);
		}
		// if(loanCate.getIsVouch() == 1){
		// loanParam.setVouchCompanyId(Long.valueOf(paramsMap.get("vouch_company_id")));
		// }

		DyResponse dyResponse = loanService.applyLoan(loanParam);
		return this.pcTurnApp(dyResponse);
	}

	/**
	 * 还款类型列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getRepayTypeList() throws Exception {

		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		queryItem.setWhere(new Where("status", 1));
		queryItem.setWhere(Where.notIn("id", "5,7"));
		List<Map> list = this.baseService.getList(queryItem, Map.class);
		return successJsonResonse(list);
	}

	/**
	 * 我的借款详情（单个标）
	 * 
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse myLoanInfo(String id) throws Exception {

		Map<String, Object> responseMap = new HashMap<String, Object>();

		// 借款信息
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		queryItem.setFields(
				"id,category_type,serialno,name,repay_type,repay_type repay_type_name,period,apr,status,status status_name,verify_time start_date,overdue_time end_date");
		queryItem.setWhere(Where.eq("id", id));
		Map<String, Object> loan_info = this.baseService.getOne(queryItem, Map.class);

		Integer repayType = (Integer) loan_info.get("repay_type");
		Integer period = (Integer) loan_info.get("period");
		String periodName = period + "个月";
		if (repayType == 5) {
			periodName = period + "天";
		}
		loan_info.put("period_name", periodName);
		responseMap.put("loan_info",
				new DataConvertUtil(loan_info).setStatus("repay_type_name", optionUtil.getRepayType())
						.setStatus("status_name", optionUtil.getBorrowStatus()).setDate("start_date", "yyyy-MM-dd").setDate("end_date", "yyyy-MM-dd").convert());

		// 还款信息
		QueryItem rpQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAY);
		rpQueryItem.setFields("success_time,expire_time,amount_total,interest_total,principal_total,interest_yes,principal_yes,next_repay_time");
		rpQueryItem.setWhere(Where.eq("loan_id", id));
		Map<String, Object> repayMap = this.baseService.getOne(rpQueryItem, Map.class);
		if (repayMap == null) {
			repayMap = new HashMap<String, Object>();
			repayMap.put("award_total", 0);
			repayMap.put("award_yes", 0);
			repayMap.put("loan_id", id);
			repayMap.put("principal_total", 0);
			repayMap.put("interest_total", 0);
			repayMap.put("interest_yes", 0);
			repayMap.put("principal_yes", 0);
			repayMap.put("next_repay_time", 0);
			repayMap.put("next_repay_amount", 0);
		}else{
			QueryItem periodQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAYPERIOD);
			periodQueryItem.setWhere(new Where("loan_id", id));
			periodQueryItem.setWhere(new Where("status", -1));
			periodQueryItem.setOrders("period_no");
			periodQueryItem.setLimit(1);
			periodQueryItem.setFields("id,period_no,amount");
			List<FnLoanRepayPeriod>  periodList = this.baseService.getList(periodQueryItem, FnLoanRepayPeriod.class);
			if(periodList != null && periodList.size() > 0){
				repayMap.put("next_repay_amount", periodList.get(0).getAmount());
			}else{
				repayMap.put("next_repay_amount", 0);
			}
		}
		responseMap.put("repay_total", repayMap);

		// 还款期数信息
		QueryItem rppQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAYPERIOD);
		StringBuffer fields = new StringBuffer();
		fields.append("id,repay_id,period_no,period,amount,interest,principal,repay_time,repay_dateymd,status,");
		// fields.append("case when repay_time_yes is null then
		// datediff(now(),repay_dateymd) else ");
		// fields.append("round((cast(repay_time_yes as signed)-cast(repay_time
		// as signed))/(3600*24),0) end late_day,");
		fields.append("repay_time_yes,normal_fee,prepayment_fee,overdue_fee,overdue_interest,repay_type,");
		fields.append("case when status=1 then principal else 0 end principal_yes,");
		fields.append(
				"case when status=1 then (case when repay_type=3 then 0 else interest end) else 0 end interest_yes,");
		fields.append("case when status=1 then (principal+(case when repay_type=3 then 0 else interest end)");
		fields.append(" +(case when normal_fee is null then 0 else normal_fee end)");
		fields.append(" +(case when prepayment_fee is null then 0 else prepayment_fee end)");
		fields.append(" +(case when overdue_fee is null then 0 else overdue_fee end)");
		fields.append(
				" +(case when overdue_interest is null then 0 else overdue_interest end)) else 0 end amount_total");
		rppQueryItem.setFields(fields.toString());
		rppQueryItem.setWhere(Where.eq("loan_id", id));
		rppQueryItem.setOrders("period_no");
		Page repayPeriodPage = this.baseService.getPage(rppQueryItem, Map.class);

		Map<String,Object> repay_info = new HashMap<String, Object>();
		repay_info.put("epage", repayPeriodPage.getEpage());
		repay_info.put("page", repayPeriodPage.getPage());
		repay_info.put("total_items", repayPeriodPage.getTotal_items());
		repay_info.put("total_pages", repayPeriodPage.getTotal_pages());
		
		List<Option> optionList = optionUtil.getRepayPeriodType();
		int periodCount = 0;
		int delayPeriod = 0;
		List<Map> repayPeriodList =  repayPeriodPage.getItems();
		if(repayPeriodPage.getItems() != null && repayPeriodPage.getItems().size() > 0){
			
			
			for (Map map : repayPeriodList) {
				String recoverType = map.get("repay_type").toString();
				Long repayTimeYes = null;
				if ("1".equals(map.get("status").toString())) { // 已还款
					repayTimeYes = Long.valueOf(map.get("repay_time_yes").toString());
				} else {
					periodCount++;
				}
				int lateDay = RepayUtil.lateDays(Long.valueOf(map.get("repay_time").toString()), repayTimeYes);
				if (lateDay > 0) {
					map.put("late_day", lateDay);
					if (delayPeriod == 0 && !"1".equals(map.get("status").toString())) {
						delayPeriod = Integer.valueOf(map.get("period_no").toString());
					}
				}else{
					map.put("late_day",0);
				}
				map.put("repay_time",DateUtil.dateFormat(map.get("repay_time")));
				for (Option option : optionList) {
					if (recoverType.equals(option.getValue().toString())) {
						map.put("repay_type", option.getText());
						break;
					}
				}
			}
		}
		repay_info.put("items", repayPeriodList);
		repay_info.put("period_wait", periodCount);
		responseMap.put("repay_info", repay_info);

		responseMap.put("period_no", delayPeriod);
		responseMap.put("roam_repay_status", 1);
		/**
		 * 是否显示借款协议
		 */
		String status =  loan_info.get("status").toString();
		if("3".equals(status) || "1".equals(status) || "-2".equals(status) || "-1".equals(status) ||"-6".equals(status) ||"-7".equals(status) ||"-3".equals(status) ||"-4".equals(status) ||"-5".equals(status) ||"-6".equals(status)){
			responseMap.put("procotol", "-1");
		}else{
			responseMap.put("procotol", "1");
		}
		
		if("3".equals(status)){
			responseMap.put("repay", "1");
		}else{
			responseMap.put("repay", "-1");
		}
		return successJsonResonse(responseMap);
	}

	public DyPhoneResponse loanInfo(String id) throws Exception {

		//借款信息
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		queryItem.setWhere(Where.eq("id", id));
		Map<String, Object> loanMap = this.baseService.getOne(queryItem, Map.class);
		
		QueryItem liQueryItem = new QueryItem(Module.LOAN, Function.LN_LOANINFO);
		liQueryItem.setWhere(Where.eq("loan_id", id));
		Map<String, Object> loanInfoMap = this.baseService.getOne(liQueryItem,Map.class);
		loanInfoMap.putAll(loanMap);
		
		

		
		loanMap.putAll(loanInfoMap);
		
		//还款方式
		QueryItem repayQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAYTYPE);
		repayQueryItem.setFields("id,name,contents,remark");
		repayQueryItem.setWhere(Where.eq("id", loanMap.get("repay_type")));
		Map<String, Object> repayTypeMap = this.baseService.getOne(repayQueryItem, Map.class);
		//借款人详情
		QueryItem miQueryItem = new QueryItem(Module.MEMBER, Function.MB_MEMBERINFO);
		miQueryItem.setWhere(Where.eq("member_id",loanMap.get("member_id")));
		Map<String,Object> memberInfoMap = this.baseService.getOne(miQueryItem, Map.class);
		
		
		
		
		//认证信息
		MbMember loanUser = this.getMbMember(Long.valueOf(loanMap.get("member_id").toString()));
		Map<String,Object> member_approve = new HashMap<String,Object>();
		member_approve.put("is_email", loanUser.getIsEmail());
		member_approve.put("is_phone", loanUser.getIsPhone());
		member_approve.put("is_realname", loanUser.getIsRealname());
		member_approve.put("name", loanUser.getName());
		member_approve.put("member_name", loanUser.getName());
		
		Map<String,Object> member_loan_info = new HashMap<String,Object>();
		member_loan_info.put("loan_count", getLoanCount(loanMap.get("member_id").toString(),"all"));//发布借款笔数
		member_loan_info.put("loan_success_count", getLoanCount(loanMap.get("member_id").toString(),"success"));//成功借款笔数
		member_loan_info.put("repay_success_count", getRepayCount(loanMap.get("member_id").toString(),"repayed"));//还清笔数
		member_loan_info.put("late_repay", getRepayCount( loanMap.get("member_id").toString(),"over"));//逾期笔数
		member_loan_info.put("late_repay_max", getRepayCount(loanMap.get("member_id").toString(),"deOver"));//严重逾期笔数
		FnLoanRepay repay = getRepayList( loanMap.get("member_id").toString());
		Map<String,Object> map = getData(loanMap.get("member_id").toString());
		BigDecimal late_amount = BigDecimal.ZERO;
		if (repay != null) {
			late_amount = NumberUtils.add(repay.getOverdueFee(), repay.getOverdueInterest());
		}
		member_loan_info.put("loan_success_amount", map.get("loan_success_amount"));//总借入
		member_loan_info.put("wait_repay_total",map.get("wait_repay_total"));//待还
		member_loan_info.put("late_amount", late_amount);//逾期金额
		
		
		Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put("loan_info", loanMap);
		valueMap.put("member_info", memberInfoMap);
		valueMap.put("repay_type", repayTypeMap);
		valueMap.put("member_approve", member_approve);
		valueMap.put("member_loan_info", member_loan_info);
		
		return successJsonResonse(valueMap);
	}
	
	/**
	 * 借款撤销
	 * 
	 * @return
	 */
	public DyPhoneResponse loanCancel(Long id) throws Exception {
		return this.pcTurnApp(this.loanService.userCancelLoan(id));
	}

	/**
	 * 图片上传
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse upload(HttpServletRequest request) throws Exception {
		Gson gson = new Gson();
		Map<String, File> fileMap = new HashMap<String, File>();
		// CommonsMultipartFile multiRequest = (CommonsMultipartFile) request;
		CommonsMultipartFile file = (CommonsMultipartFile) ((MultipartRequest) request).getFile("upload_file");
		if (file == null)
			return errorJsonResonse("上传图片不能为空");
		if (file != null) {
			DiskFileItem fileItem = (DiskFileItem) file.getFileItem();
			fileMap.put(file.getOriginalFilename(), fileItem.getStoreLocation());
		}
		// 上传到图片服务器
		String path = "";
		if (fileMap.size() > 0) {
			DyResponse response = DyHttpClient.doImageUpload("member", "memberInfo", fileMap);
			if (response == null || response.getStatus() != DyResponse.OK)
				return errorJsonResonse("上传出错");

			List<Map<String, Object>> fileList = (List<Map<String, Object>>) response.getData();
			for (Map<String, Object> map : fileList) {
				if (file != null && file.getOriginalFilename().equals(map.get("name").toString())) {
					path = map.get("id").toString();
				}
			}
		}
		// 图片地址头部
		String imgPath = PropertiesUtil.getImageHost();
		String url = "";
		if (!"".equals(path))
			url = imgPath + "/" + path;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> newMap = new HashMap<String, Object>();
		newMap.put("file_url", path);
		return successJsonResonse(newMap);
	}
	
	/**
	 * 获取下一期还款时间、借款利息
	 * @param id
	 * @return
	 * @throws DyServiceException 
	 */
	public Map<String,Object> getNexRepayTime(String id) throws DyServiceException{
		QueryItem queryItem = new QueryItem(Module.LOAN, Function.LN_REPAY);
		queryItem.setWhere(new Where("loan_id", id));
		queryItem.setFields("id,next_repay_time,interest_yes");
		FnLoanRepay repay = this.baseService.getOne(queryItem, FnLoanRepay.class);
		Map<String,Object> responeMap = new HashMap<String, Object>();
		if(repay != null && repay.getId() != null){
			
			responeMap.put("next_repay_time", DateUtil.dateFormat(repay.getNextRepayTime()));
			responeMap.put("interest_yes", repay.getInterestYes());
			
		}else{
			responeMap.put("next_repay_time", "");
			responeMap.put("interest_yes", 0);
		}
		return responeMap;
	}
	
	/**
	 * 借款协议
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse loanProtocolData()throws Exception{
		return successJsonResonse("dfd");
	}
	
	
	
	/**
	 * 借款标笔数
	 * @throws Exception 
	 */
	private Integer getLoanCount(String memberId,String type) throws Exception{
		QueryItem loanItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id",memberId));
		if("all".equals(type)){//发布借款条数
			
		}else if("success".equals(type)){//成功借款条数
			whereList.add(new Where("status",6,">="));
		}
		loanItem.setWhere(whereList);
		List<FnLoan> loanList = this.getListByEntity(loanItem,Module.LOAN,Function.LN_LOAN,FnLoan.class);
		Integer count = loanList.size();
		return count==null?0:count;
	}
	/**
	 * 还款笔数
	 * @throws Exception 
	 */
	private Integer getRepayCount(String memberId,String type) throws Exception{
		QueryItem recoverItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("loan_member_id",memberId));
		if("repayed".equals(type)){//还清期数
			whereList.add(new Where("status",1));
		}else if("over".equals(type)){//逾期期数
			whereList.add(new Where("recover_type",2));
		}else if("deOver".equals(type)){//严重逾期期数
			whereList.add(new Where("overdue_days",30,">"));
		}
		recoverItem.setWhere(whereList);
		List<FnLoanRepay> repayList = this.getListByEntity(recoverItem,Module.LOAN,Function.LN_RECOVER,FnLoanRepay.class);
		Integer count = repayList.size();
		return count==null?0:count;
	}
	/**
	 * 借款标还款表
	 * @throws Exception 
	 */
	private FnLoanRepay getRepayList(String memberId) throws Exception{
		QueryItem repayItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id",memberId));
		repayItem.setWhere(whereList);
		repayItem.setFields("sum(amount_total) amount_total,sum(amount_yes) amount_yes,sum(overdue_fee) overdue_fee,sum(overdue_interest) overdue_interest");
		FnLoanRepay repay = this.getOneByEntity(repayItem, Module.LOAN,Function.LN_REPAY, FnLoanRepay.class);
		return repay;
	}
	
	
	
	private Map<String,Object> getData(String memberId) throws Exception{
		StringBuffer fields = new StringBuffer();
		fields.append("loan_member_id,");
		fields.append("sum(amount) principal_total_all,"); //借款总额
		fields.append("sum(case when recover_status!=1 then recover_principal else 0 end) nr_recover_principal,"); //未回款应回款总本金
		fields.append("sum(case when recover_status!=1 then recover_interest else 0 end) nr_recover_interest,"); //未回款应回款总利息
		fields.append("sum(case when recover_status!=1 then recover_principal_yes else 0 end) nr_recover_principal_yes,");//未回款已收本金
		fields.append("sum(case when recover_status!=1 then recover_interest_yes else 0 end) nr_interest_yes_total,");//未回款已收利息
		fields.append("sum(recover_interest_yes) interest_yes_total,");//已收利息
		fields.append("sum(award_amount) award_amount_yes");//已收奖金
		QueryItem queryItem = new QueryItem("loan", "tender");
		queryItem.setFields(fields.toString());
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("loan_member_id", memberId));
		queryItem.setWhere(whereList);
		//queryItem.getWhere().add(Where.eq("loan_member_id", member.getId()));
		queryItem.setGroup("loan_member_id");
		Map<String, Object> valueMap = (Map<String, Object>) this.baseService.getOne(queryItem);
		BigDecimal principalWaitTotal = BigDecimal.ZERO;
		BigDecimal interestWaitTotal = BigDecimal.ZERO;
		BigDecimal principalTotalAll = BigDecimal.ZERO;
		if (valueMap != null) {
			BigDecimal recoverPrincipal = valueMap.get("nr_recover_principal") != null ? (BigDecimal) valueMap.get("nr_recover_principal") : BigDecimal.ZERO;
			BigDecimal recoverInterest = valueMap.get("nr_recover_interest") != null ? (BigDecimal) valueMap.get("nr_recover_interest") : BigDecimal.ZERO;
			BigDecimal recoverPrincipalYes = valueMap.get("nr_recover_principal_yes") != null ? (BigDecimal) valueMap.get("nr_recover_principal_yes") : BigDecimal.ZERO;
			BigDecimal recoverInterestYes = valueMap.get("nr_interest_yes_total") != null ? (BigDecimal) valueMap.get("nr_interest_yes_total") : BigDecimal.ZERO;
			principalWaitTotal = recoverPrincipal.subtract(recoverPrincipalYes);//待还本金
			interestWaitTotal = recoverInterest.subtract(recoverInterestYes);//代还利息
			principalTotalAll = (BigDecimal)valueMap.get("principal_total_all");
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("wait_repay_total", principalWaitTotal.add(interestWaitTotal));
		map.put("loan_success_amount", principalTotalAll);
		return map;
	}
	
	/**
	 * 获取借款标详情
	 * @param id
	 * @return
	 * @throws DyServiceException
	 */
	private Map getLoanInfoByLoanId(String id) throws DyServiceException{
		QueryItem loanInfoItem = new QueryItem(Module.LOAN,Function.LN_LOANINFO);
		loanInfoItem.setWhere(new Where("loan_id", id));
		loanInfoItem.setFields("amount_category_id");
		Map map = this.baseService.getOne(loanInfoItem, Map.class);
		return map;
	}

	/**
	 * 在线申请提交
	 * @param paramsMap
	 * @param istrust
	 * @param ipStrToLong
	 * @return
	 */
	public DyPhoneResponse loanApplySub(Map<String, String> paramsMap,String sessionCode,Long ipStrToLong) {
		String login_token = paramsMap.get("login_token");
		MbMember member = null;
		try {
			boolean isLogin = false;
			if (!StringUtils.isBlank(login_token)) {
				member = this.getMbMember(Long.valueOf(login_token));
				isLogin = true;
				if (member.getIsRealname() != 1 || member.getIsPhone() != 1 || member.getIsEmail() != 1 || member.getPaypassword() == null) {
					return errorJsonResonse("请先进行认证!");
				}
			}
			FnLoanApply loanApply = new FnLoanApply();
			if (!isLogin) {
				String member_name = paramsMap.get("member_name");
				if (StringUtils.isBlank(member_name)) {
					return errorJsonResonse("姓名不能为空");
				}
				String phone = paramsMap.get("phone");
				if (StringUtils.isBlank(phone)) {
					return errorJsonResonse("手机号码不能为空");
				}
				String phoneCode = paramsMap.get("phoneCode");
				if (StringUtils.isBlank(phoneCode)) {
					return errorJsonResonse("手机验证码不能为空");
				}
				String email = paramsMap.get("email");
				if (StringUtils.isBlank(email)) {
					return errorJsonResonse("电子邮箱不能为空");
				}
				if (!phoneCode.equals(sessionCode)) {
					return errorJsonResonse("手机验证码错误");
				}
				loanApply.setMemberName(member_name);
				loanApply.setPhone(Long.valueOf(phone));
				loanApply.setEmail(email);
			} else {
				loanApply.setMemberId(member.getId());
				loanApply.setMemberName(member.getName());
				loanApply.setPhone(member.getPhone());
				loanApply.setEmail(member.getEmail());
			}
	
			String id = paramsMap.get("id");
			String name = paramsMap.get("name");
			if (StringUtils.isBlank(name)) {
				return errorJsonResonse("项目名称不能为空");
			}
			String province = paramsMap.get("province");
			String city = paramsMap.get("city");
			String amount = paramsMap.get("amount");
			if (StringUtils.isBlank(amount)) {
				return errorJsonResonse("借款金额不能为空");
			}
			String period = paramsMap.get("period");
			if (StringUtils.isBlank(period)) {
				return errorJsonResonse("借款期限不能为空");
			}
			String apr = paramsMap.get("apr");
			if (StringUtils.isBlank(apr)) {
				return errorJsonResonse("年化回报率不能为空");
			}
			String contents = paramsMap.get("contents");
			if (StringUtils.isBlank(contents)) {
				return errorJsonResonse("详细描述不能为空");
			}
		
			//地区
			QueryItem item = new QueryItem();
			item.getWhere().add(Where.in("id", province + "," + city));
			List<SysSystemAreas> areaList = this.getListByEntity(item, Module.SYSTEM, Function.SYS_AREAS, SysSystemAreas.class);
			String provinceName = "";
			String cityName = "";
			for (SysSystemAreas area : areaList) {
				if (province.equals(area.getId().toString())) {
					provinceName = area.getName();
				} else if (city.equals(area.getId().toString())) {
					cityName = area.getName();
				}
			}

			loanApply.setName(name);
			loanApply.setProvince(Integer.valueOf(province));
			loanApply.setCity(Integer.valueOf(city));
			loanApply.setAmount(new BigDecimal(amount));
			loanApply.setAreas(provinceName + cityName);
			loanApply.setApr(new BigDecimal(apr));
			loanApply.setPeriod(Integer.valueOf(period));
			loanApply.setContents(contents);
			loanApply.setStatus(2);
			if (StringUtils.isNotBlank(id)) {
				loanApply.setId(Long.valueOf(id));
				this.baseService.updateById(Module.LOAN, Function.LN_APPLY, loanApply);
			} else {
				loanApply.setAddTime(DateUtil.getCurrentTime());
				loanApply.setAddIp(ipStrToLong);
				this.baseService.insert(Module.LOAN, Function.LN_APPLY, "id", loanApply);
			}
			return this.successJsonResonse("申请成功");
		} catch (Exception e) {
			return this.errorJsonResonse("申请失败");
		}
	}
	
	
}
