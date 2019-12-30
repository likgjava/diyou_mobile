package com.dy.baf.service.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanCategory;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Option;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.OptionUtil;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.StringUtils;

@Service("myLoanDataService")
public class MyLoanDataService extends MobileService {
	@Autowired
	private BaseService baseService;
	@Autowired
	private OptionUtil optionUtil;

	/**
	 * 获取我的借款信息
	 * @param status
	 * @param member
	 * @param pageNumStr
	 * @param startTime
	 * @param endTime
	 * @param object 
	 * @param isTrust
	 * @return
	 * @throws Exception
	 */
	public Page myLoanDataList(String status, MbMember member,String pageNumStr,String startTime, String endTime,String epage, boolean isTrust) throws Exception {
		int pageNum = 1;
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}

		String innerStatus = "";
				
		if (StringUtils.isBlank(status) || "first".equals(status)) {
			innerStatus = "1,-10"; //1发标待审,-10 初审驳回
		} else if ("loan".equals(status)) {
			innerStatus = "2, 3,-7,-2, 4";// 2初审通过自动投标中,3借款中,4满标待审, -7 锁定中,-2借款到期未满标（过期）
		} else if ("repay".equals(status)) {
			innerStatus = "6";//6复审通过
		} else if ("repay_yes".equals(status)) {
			innerStatus = "7";//7已还完
		} else if ("over".equals(status)) {
			//-1初审失败,-3复审失败不合格标,-4用户撤标（流标）,-5过期后台撤标（流标）-6 未过期后台撤标（流标） -8（复审失败 后台处理中）-9 (后台撤标 后台处理中) 
			innerStatus = "-1,-3,-4,-5,-6,-8,-9";
		}

		QueryItem queryItem = new QueryItem();
		queryItem.setFields(
				"id,ind,serialno,name,category_type,(case when status >=4 then credited_amount else amount end) amount," +
				"credited_amount,status,(case when overdue_time < " + DateUtil.getCurrentTime() + " and category_type != 3 then (case when status != 3 then status else -2 end) else status end) status_name," +
				"add_time,category_id,verify_time,reverify_time,period,repay_type");
		//queryItem.setWhere(Where.in("status", innerStatus));
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
		if("loan".equals(status)||"repay".equals(status)){//流转中 | 回购中
			QueryItem queryRoamconn=new QueryItem();
			queryRoamconn.setFields("id,status,category_type,amount,credited_amount");
			queryRoamconn.setWhere(Where.eq("member_id", member.getId()));
			queryRoamconn.getWhere().add(new Where("status", "2,3",IN));
			queryRoamconn.getWhere().add(new Where("category_type", "3",EQ));
			List<FnLoan> queryLoanRoamList=this.getListByEntity(queryRoamconn, Module.LOAN, Function.LN_LOAN, FnLoan.class);

			//记录回购中的借款标的id
			for(int i=0;i<queryLoanRoamList.size();i++){
					if(isTrust && "repay".equals(status)) {
						if(queryLoanRoamList.get(i).getCreditedAmount().compareTo(BigDecimal.ZERO) > 0)
							inLoanIdStr=inLoanIdStr+queryLoanRoamList.get(i).getId()+",";
//						} else if(queryLoanRoamList.get(i).getAmount().compareTo(queryLoanRoamList.get(i).getCreditedAmount())==0){//借款金额等于到账金额
//							inLoanIdStr=inLoanIdStr+queryLoanRoamList.get(i).getId()+",";							
//						}	
					} else if(queryLoanRoamList.get(i).getCreditedAmount().compareTo(BigDecimal.ZERO) > 0){//到账金额大于0
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
//						whereList.add(new Where("credited_amount",0,">"));
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
					
		
		//分页参数
		queryItem.setOrders("add_time desc");
		queryItem.setPage(pageNum);
		if(epage==null){
			epage="6";
		}
		queryItem.setLimit(Integer.parseInt(epage));
		Page page = this.getPageByMap(queryItem, Module.LOAN, Function.LN_LOAN);
		List<Map<String, Object>> loanList = (List<Map<String, Object>>) page.getItems();
		//移到点击查看的时候再生成url
//			AccessToken accessToken=null;
//			//是否开启存证,0关闭，1开启
		String isCert = getIsCert();
//			if("1".equals(isCert)){
//				accessToken=CunnarUtils.getAccessToken(member.getId().toString());
//			}
		//获取借款标状态集合
		Map<String,String> borrowStatusNameMap=new HashMap<String, String>();
		List<Option> borrowStatusNameList=optionUtil.getBorrowStatus();
		for(Option option:borrowStatusNameList){
			borrowStatusNameMap.put(String.valueOf(option.getValue()), option.getText());
		}
		for (Map<String, Object> map : loanList) {
//				if(map.get("certificate_file_id")!=null && "1".equals(isCert)){
//					String certificateUrl=CunnarUtils.getUrl(accessToken,map.get("certificate_file_id").toString());
//					String certificateReceiptUrl=CunnarUtils.getReceiptUrl(accessToken, map.get("certificate_file_id").toString());
//					map.put("certificate_url", certificateUrl);
//					map.put("certificate_receipt_url", certificateReceiptUrl);
//				}
			//翻译借款标状态		
			map.put("status_name", borrowStatusNameMap.get(String.valueOf(map.get("status_name"))));
			if(!"-7".equals(map.get("status").toString()) && "3".equals(String.valueOf(map.get("category_type")))){//流转标
				if("repay".equals(status)){
					map.put("status_name", "回购中");
				}else if("loan".equals(status)){
					map.put("status_name", "流转中");
				}
			}
			
			if("6".equals(map.get("status").toString())){
				map.put("status_name", "还款中");
			}
					
		}
		
		if (("repay".equals(status) || "repay_yes".equals(status))) {
			if (page.getItems() != null && page.getItems().size() > 0) {
				List<Map<String, Object>> valueList = (List<Map<String, Object>>) page.getItems();
				String loanIds = "";
				Long expireTime = 0L;
				Long reverifyTime = 0L;
				for (Map<String, Object> map : valueList) {
					if (loanIds == "") {
						loanIds += map.get("id");
					} else {
						loanIds += "," + map.get("id");
					}
					
					Integer repayType = Integer.valueOf(map.get("repay_type").toString());
					Integer period = Integer.valueOf(map.get("period").toString());
					//复审时间与过期时间(流转标的有效时间是初审时间计算)
					reverifyTime = "3".equals(map.get("category_type"))?Long.valueOf(map.get("verify_time").toString()):Long.valueOf(map.get("reverify_time").toString());
					map.put("reverify_time", reverifyTime);
					if (5 == repayType) {
						expireTime = DateUtil.addDay(reverifyTime, period);
					} else {
						expireTime = DateUtil.addMonth(reverifyTime, period);
					}
					map.put("expire_time", expireTime);
				}
				
				QueryItem tdQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAY);
				tdQueryItem.setFields("id,loan_id,amount_total,amount_yes");
				tdQueryItem.setWhere(Where.in("loan_id", loanIds));
				List<Map<String, Object>> tndList = (List<Map<String, Object>>) baseService.getList(tdQueryItem);
				for (Map<String, Object> tndMap : tndList) {
					String loanId = tndMap.get("loan_id").toString();
					BigDecimal amountTotal = (BigDecimal) tndMap.get("amount_total");
					BigDecimal amount_yes = (BigDecimal) tndMap.get("amount_yes");
					for (Map<String, Object> map : valueList) {
						if (loanId.equals(map.get("id").toString())) {
							map.put("amount_total", amountTotal);
							map.put("amount_yes", amount_yes);
							break;
						}
					}
				}
			}
		}
		page.setParams(isCert);
		return page;
 }
	
	/**
	 * 获取当笔借款信息
	 * @param id
	 * @param sessionUser
	 * @param loanMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getrepayINfo(Long id, MbMember sessionUser,Map<String, Object> loanMap ) throws Exception {
		
		Integer repayType = (Integer) loanMap.get("repay_type");
		Integer period = (Integer) loanMap.get("period");
		String periodName = period + "个月";
		if (repayType == 5) {
			periodName = period + "天";
		}
		loanMap.put("period_name", periodName);
		//判断该标是否支持提前还款
		String operateAuth = getOperateAuthByCateId(Long.valueOf(loanMap.get("category_id").toString()));
		String isAdvanceRepay = "1";//可以提前还款
		if(operateAuth != null && !operateAuth.contains("3")){
			isAdvanceRepay = "0";
		}
		loanMap.put("isAdvanceRepay", isAdvanceRepay);
		//还款信息
		QueryItem rpQueryItem = new QueryItem();
		rpQueryItem.setFields("success_time,expire_time,amount_total,interest_total,principal_total");
		rpQueryItem.setWhere(Where.eq("loan_id", id));
		Map<String, Object> repayMap = getOne( Module.LOAN, Function.LN_REPAY, rpQueryItem);

		//还款期数信息
		QueryItem rppQueryItem = new QueryItem();
		StringBuffer fields = new StringBuffer();
		fields.append("id,repay_id,period_no,period,amount,interest,principal,repay_time,repay_dateymd,status,");
		//fields.append("case when repay_time_yes is null then datediff(now(),repay_dateymd) else ");
		//fields.append("round((cast(repay_time_yes as signed)-cast(repay_time as signed))/(3600*24),0) end late_day,");
		fields.append("repay_time_yes,normal_fee,prepayment_fee,overdue_fee,overdue_interest,repay_type,");
		fields.append("case when status=1 then principal else 0 end principal_yes,");
		fields.append("case when status=1 then (case when repay_type=3 then 0 else interest end) else 0 end interest_yes,");
		fields.append("case when status=1 then (principal+(case when repay_type=3 then 0 else interest end)");
		fields.append(" +(case when normal_fee is null then 0 else normal_fee end)");
		fields.append(" +(case when prepayment_fee is null then 0 else prepayment_fee end)");
		fields.append(" +(case when overdue_fee is null then 0 else overdue_fee end)");
		fields.append(" +(case when overdue_interest is null then 0 else overdue_interest end)) else 0 end amount_total");
		rppQueryItem.setFields(fields.toString());
		rppQueryItem.setWhere(Where.eq("loan_id", id));
		rppQueryItem.setOrders("period_no");
		List<Map> repayPeriodList = getListByEntity(rppQueryItem, Module.LOAN, Function.LN_REPAYPERIOD, Map.class);

		List<Option> optionList = optionUtil.getRepayPeriodType();
		int periodCount = 0;
		int delayPeriod = 0;
		for (Map map : repayPeriodList) {
			String recoverType = map.get("repay_type").toString();
			Long repayTimeYes = null;
			if ("1".equals(map.get("status").toString())) { //已还款
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
			}
			for (Option option : optionList) {
				if (recoverType.equals(option.getValue().toString())) {
					map.put("repay_type", option.getText());
					break;
				}
			}
		}

		Map<String, Object> valueMap = new HashMap<String, Object>();
		loanMap.putAll(repayMap);
		loanMap.put("period_count", periodCount);
		loanMap.put("delay_period", delayPeriod);
		loanMap.put("loan_id", id);
		
		valueMap.put("info",new DataConvertUtil(loanMap).setStatus("repay_type_name", optionUtil.getRepayType()).convert());
		valueMap.put("repay_list", repayPeriodList);
		return valueMap;
	}
	
	
	/**
	 * 判断是否开启存证
	 * @throws Exception 
	 */
	public String getIsCert() throws Exception{
		QueryItem item = new QueryItem();
		item.getWhere().add(Where.eq("nid", "cert_isopen"));
		SysSystemConfig config = this.getOneByEntity(item, Module.SYSTEM, Function.SYS_CONFIG, SysSystemConfig.class);
		String value = "1";//开启
		if(config.getStatus() != 1 || "2".equals(config.getValue())){
			value = "2";
		}
		if(config.getStatus() == 1 && "1".equals(config.getValue())){
			value = "1";
		}
		return value;
	}
	

	/**
	 * 根据标种id获取可操作权限
	 * @throws Exception 
	 */
	public String getOperateAuthByCateId(Long cateId) throws Exception{
		QueryItem cateItem = new QueryItem();
		cateItem.getWhere().add(Where.eq("id", cateId));
		cateItem.setFields("operate_auth");
		FnLoanCategory cate = this.getOneByEntity(cateItem, Module.LOAN, Function.LN_CATEGORY, FnLoanCategory.class);
		return cate == null ? "" : cate.getOperateAuth();
	}
}


