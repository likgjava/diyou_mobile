package com.dy.baf.service.spread;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbSpreadLog;
import com.dy.baf.entity.common.MbSpreadSettleLog;
import com.dy.baf.entity.common.MbSpreadSta;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.JsonUtils;
import com.dy.core.utils.SecurityUtil;
import com.dy.httpinvoker.MemberService;

/**
 * 我的推广
 * 
 * @author Administrator
 *
 */
@Service("mobileSpreadService")
public class SpreadService extends MobileService {

	@Autowired
	private BaseService baseService;
	@Autowired
	private MemberService memberService;

	/**
	 * 我的推广记录
	 * 
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse mySpreadAll(String memberId, String page, String epage) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		// 查询推广人关系表
		QueryItem relationItem = new QueryItem(Module.MEMBER, Function.MB_SPREADRELATION);
		relationItem.setPage(page == null ? 1 : Integer.valueOf(page));
		relationItem.setLimit(epage == null ? 1 : Integer.valueOf(epage));
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", user.getId()));
		relationItem.setWhere(whereList);
		Page pageObj = this.baseService.getPage(relationItem, Map.class);
		List<Map<String, Object>> relationList = pageObj.getItems();
		for (Map<String, Object> rela : relationList) {
			rela.put("tender_success_amount", getTotalAmount(rela.get("spreaded_member_id").toString(), "tender"));
			rela.put("repay_amount_yes", getTotalAmount(rela.get("spreaded_member_id").toString(), "loan"));
			rela.put("member_name", rela.get("spreaded_member_name"));
		}
		// 查询邀请人数，提成总额，投资提成，借款还款提成
		Map<String, Object> totalMap = getCountInfo(user);
		// 查询结算限制，当前结算总额，结算中金额，邀请链接
		Map<String, Object> settleMap = getSpreadAccount(user);
		String userName = user.getName();

		Map<String, Object> resonseMap = JsonUtils.json2Object(pageObj, Map.class);
		String siteUrl = getSysConfig("site_mobile") + "/wap/common/member/reg?invite="
				+ SecurityUtil.encode(userName.getBytes("UTF-8"));
		resonseMap.put("share_url", siteUrl);
		resonseMap.put("share_title", "");
		resonseMap.put("share_content", "");
		resonseMap.putAll(settleMap);
		resonseMap.putAll(totalMap);
		return successJsonResonse(resonseMap);
	}

	/**
	 * 邀请人数，提成总额，投资提成，借款还款提成
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCountInfo(MbMember user) throws Exception {
		Integer person_count = 0;
		BigDecimal income = BigDecimal.ZERO;
		BigDecimal repay_income = BigDecimal.ZERO;
		BigDecimal tender_income = BigDecimal.ZERO;
		QueryItem staItem = new QueryItem(Module.MEMBER, Function.MB_SPREADSTATISTICS);
		staItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbSpreadSta sta = this.baseService.getOne(staItem, MbSpreadSta.class);
		if (sta != null) {
			person_count = sta.getPersonCount();
			income = sta.getTenderIncome().add(sta.getRepayIncome());
			repay_income = sta.getRepayIncome();
			tender_income = sta.getTenderIncome();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("person_count", person_count);
		map.put("income", income);
		map.put("repay_income", repay_income);
		map.put("tender_income", tender_income);
		return map;
	}

	/**
	 * 查询结算限制，当前结算总额，结算中金额，邀请链接
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSpreadAccount(MbMember user) throws Exception {
		BigDecimal unAaccount = BigDecimal.ZERO;
		BigDecimal accounted = BigDecimal.ZERO;
		BigDecimal accounting = BigDecimal.ZERO;
		QueryItem logItem = new QueryItem();
		logItem.setFields("award,status");
		logItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<MbSpreadLog> logList = this.getListByEntity(logItem, Module.MEMBER, Function.MB_SPREADLOG, MbSpreadLog.class);
		for(MbSpreadLog log : logList){
			if(log.getStatus() == -1){//未结算
				unAaccount = unAaccount.add(log.getAward());
			}else if(log.getStatus() == 2){//结算中
				accounting = accounting.add(log.getAward());
			}else if(log.getStatus() == 1){//已结算
				accounted = accounted.add(log.getAward());
			}
		}
		
		BigDecimal failAccounting = BigDecimal.ZERO;
		QueryItem settleItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", user.getId()));
		settleItem.setWhere(whereList);
		List<MbSpreadSettleLog> settleList = this.getListByEntity(settleItem, Module.MEMBER, Function.MB_SPREADSETTLE, MbSpreadSettleLog.class);
		for(MbSpreadSettleLog settleLog : settleList){
			if(2L == settleLog.getVerifyStatus()){
				failAccounting = failAccounting.add(settleLog.getMoney());
			}
		}
		
		String limit = getSysConfig("spread_account");
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> totalMap = new HashMap<String,Object>();
		totalMap.put("unAaccount", unAaccount);
		totalMap.put("accounted", accounted);
		totalMap.put("accounting", accounting.subtract(failAccounting));
		map.put("total", totalMap);
		map.put("limit", limit);
		return map;
	}

	/**
	 * 结算记录
	 */
	public DyPhoneResponse getSettleLog(String memberId, String page, String epage) throws Exception {
		MbMember user = getMbMember(Long.valueOf(memberId));
		// 查询结算记录
		QueryItem settleItem = new QueryItem(Module.MEMBER, Function.MB_SPREADSETTLE);
		settleItem.setPage(page == null ? 1 : Integer.valueOf(page));
		settleItem.setLimit(epage == null ? 10 : Integer.valueOf(epage));
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", user.getId()));
		settleItem.setWhere(whereList);
		Page pageObj = this.baseService.getPage(settleItem, Map.class);
		List<Map<String, Object>> list = pageObj.getItems();
		for (Map<String, Object> map : list) {
			map.put("add_time", DateUtil.dateFormat(map.get("add_time")));
			String value = "";
			Integer status = Integer.valueOf(map.get("verify_status").toString());
		
			if (status == 2) {
				value = "审核失败";
			}else{
				if(new BigDecimal(map.get("not_pay").toString()).compareTo(BigDecimal.ZERO) == 0){
					value = "已结清";
				}else{
					value = "未结清";
				}
			}
			map.put("status", value);
		}
		return successJsonResonse(pageObj);
	}

	/**
	 * 立即结算
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse doAccount(String memberId, String ip) throws Exception {
		MbMember user = getMbMember(Long.valueOf(memberId));
		
		BigDecimal unAaccount = BigDecimal.ZERO;
		QueryItem logItem = new QueryItem(Module.MEMBER, Function.MB_SPREADLOG);
		logItem.setFields("award,status");
		logItem.getWhere().add(Where.eq("status", -1));
		logItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<MbSpreadLog> logList = this.baseService.getList(logItem, MbSpreadLog.class);
		for (MbSpreadLog log : logList) {
			if (log.getStatus() == -1) {// 未结算
				unAaccount = unAaccount.add(log.getAward());
			}
		}
		
		if(!(unAaccount.compareTo(BigDecimal.ZERO) == 1)){
			return errorJsonResonse("结算金额需大于0元");
		}
		memberService.settle(user, IpUtil.ipStrToLong(ip));
		
		return successJsonResonse("结算中");

	}

	/**
	 * 获取系统配置
	 * 
	 * @throws Exception
	 */
	private String getSysConfig(String type) throws Exception {
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		item.getWhere().add(Where.eq("nid", type));
		SysSystemConfig config = this.baseService.getOne(item, SysSystemConfig.class);
		return config.getValue();
	}

	/**
	 * 计算投资总额或还款总额
	 * 
	 * @throws Exception
	 */
	private BigDecimal getTotalAmount(String memberId, String type) throws Exception {
		QueryItem item = new QueryItem(Module.MEMBER, Function.MB_SPREADLOG);
		item.setFields(
				"sum(amount_principal) amount_principal,sum(amount_all) amount_all,sum(amount_interest) amount_interest");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("spreaded_member_id", memberId));
		whereList.add(Where.eq("spread_type", type));
		item.setWhere(whereList);
		MbSpreadLog log = this.baseService.getOne(item, MbSpreadLog.class);
		if (log == null)
			return BigDecimal.ZERO;
		if ("tender".equals(type)) {
			return log.getAmountPrincipal();
		} else {
			return log.getAmountAll();
		}
	}
	/**
	 * 我的推广
	 * @param memberId
	 * @return
	 * @throws DyServiceException 
	 * @throws NumberFormatException 
	 */
	public DyPhoneResponse mySpreadData(String memberId) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		Map<String,Object> resonseMap = new HashMap<String, Object>() ;
		// 查询邀请人数，提成总额，投资提成，借款还款提成
		Map<String, Object> totalMap = getCountInfo(user);
		// 查询结算限制，当前结算总额，结算中金额，邀请链接
		Map<String, Object> settleMap = getSpreadAccount(user);
		String userName = user.getName();

		String siteUrl = getSysConfig("site_mobile") + "/wap/common/member/reg?invite="
				+ SecurityUtil.encode(userName.getBytes("UTF-8"));
		resonseMap.put("share_url", siteUrl);
		resonseMap.put("countInfo", totalMap);
		resonseMap.putAll(settleMap);
		resonseMap.putAll(totalMap);
		return successJsonResonse(resonseMap);
	}
	/**
	 * 我的推广列表
	 */
	public DyPhoneResponse mySpread(String memberId, String page) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		// 查询推广人关系表
		QueryItem relationItem = new QueryItem(Module.MEMBER, Function.MB_SPREADRELATION);
		relationItem.setPage(page == null ? 1 : Integer.valueOf(page));
		relationItem.setLimit(10);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", user.getId()));
		relationItem.setWhere(whereList);
		Page pageObj = this.baseService.getPage(relationItem, Map.class);
		List<Map<String, Object>> relationList = pageObj.getItems();
		for (Map<String, Object> rela : relationList) {
			rela.put("tender_success_amount", getTotalAmount(rela.get("spreaded_member_id").toString(), "tender"));
			rela.put("repay_amount_yes", getTotalAmount(rela.get("spreaded_member_id").toString(), "loan"));
			rela.put("member_name", rela.get("spreaded_member_name"));
		}
		return successJsonResonse(pageObj);
	}
}
