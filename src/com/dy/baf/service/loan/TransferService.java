package com.dy.baf.service.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanRepay;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.FnTenderTransfer;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.entity.custom.Tender;
import com.dy.baf.entity.custom.TenderCondition;
import com.dy.baf.service.MobileService;
import com.dy.baf.service.system.VipService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.controller.BaseController;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.FinanceService;

/**
 * 
 * 
 * @Description: 用户中心:我的债权转让列表/购买记录
 * @author 波哥
 * @date 2015年9月12日 下午3:32:09
 * @version V1.0
 */
@Service("mobileTransferService")
public class TransferService extends MobileService {
	@Autowired
	private BaseService baseService;
	@Autowired
	private FinanceService financeService;
	@Autowired
	private VipService vipService;

	
	/**
	 * 转让记录/购买记录
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public DyPhoneResponse getTransferList(String memberId,String statusNid,String page,String epage) throws Exception{
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		if("transfer".equals(statusNid)){//转让列表
			List<FnTender> canTenderList = (List<FnTender>) getCanTransfer(user,true,null);//可以转让
			List<FnTenderTransfer> inTransferList = (List<FnTenderTransfer>) getInTransfer(user,true,null);//转让中
			List<FnTenderTransfer> successTransferList = (List<FnTenderTransfer>) getSuccessTransfer(user,true,null);//转让成功
			List<FnTenderTransfer> cancelTransferList = (List<FnTenderTransfer>) getCancelTransfer(user,true,null);//已撤销
			String success_date;//转让时间
			String expire_date;//到期时间
			Long loan_id;//借款标id
			String loan_name;//借款标名称
			Integer recover_count;//总期数
			BigDecimal apr;//利率
			Integer wait_recover_count;//待还期数
			BigDecimal wait_recover_principal = BigDecimal.ZERO;//待收本金
			BigDecimal wait_recover_interest = BigDecimal.ZERO;//待收利息
			BigDecimal transfer_amount;//转让价格
			Integer transfer_status;//转让状态
			String status_name;//状态
			Long id;//转让id
			String changeStatus;
			List<Map<String,Object>> transferTenderList = new ArrayList<Map<String,Object>>();
			for(FnTender tender:canTenderList){//可以转让
				if(tender.getTransferStatus() != -3){
					wait_recover_principal = BigDecimal.ZERO;
					wait_recover_interest = BigDecimal.ZERO;
					Map<String,Object> map = new HashMap<String,Object>();
					expire_date = DateUtil.dateFormatSlash(tender.getExpireTime());
					loan_id = tender.getLoanId();
					loan_name = tender.getLoanName();
					recover_count = tender.getRecoverCount();
					FnLoan loan = getLoan(loan_id);
					apr = loan.getApr();
					Integer recoverCountYes = tender.getRecoverCountYes() == null ?0:tender.getRecoverCountYes();
					wait_recover_count = tender.getRecoverCount()-recoverCountYes;
					FnTenderRecover recover = getRecoverMoney(tender.getId());
					if(recover != null){
						wait_recover_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
						wait_recover_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
					}
					transfer_status = tender.getTransferStatus();
					status_name = "可以转让";
					id = tender.getId();
					changeStatus = "0";
					map.put("wait_period", wait_recover_count);//待还期数
					map.put("period", tender.getRecoverCount());//总期数
					map.put("transfer_money", wait_recover_principal.add(wait_recover_interest));//待收本息
					
					map.put("expire_date", expire_date);
					map.put("loan_id", loan_id);
					map.put("loan_name", loan_name);
					map.put("recover_count", recover_count);
					map.put("apr", apr);
					map.put("wait_recover_count", wait_recover_count);
					map.put("wait_recover_principal", wait_recover_principal);
					map.put("wait_recover_interest", wait_recover_interest);
					map.put("transfer_status", transfer_status);
					map.put("transfer_status_name", status_name);
					map.put("id", id);
					map.put("changeStatus", changeStatus);
					transferTenderList.add(map);
				}
			}
			for(FnTenderTransfer tenderTransfer:inTransferList){//转让中
				wait_recover_principal = BigDecimal.ZERO;
				wait_recover_interest = BigDecimal.ZERO;
				Map<String,Object> map = new HashMap<String,Object>();
				FnTender tender = getTender(tenderTransfer.getTenderId());
				expire_date = DateUtil.dateFormatSlash(tender.getExpireTime());
				loan_id = tenderTransfer.getLoanId();
				loan_name = tenderTransfer.getLoanName();
				recover_count = tenderTransfer.getTotalPeriod();
				apr = tenderTransfer.getApr();
				Integer recoverCountYes = tender.getRecoverCountYes() == null ?0:tender.getRecoverCountYes();
				wait_recover_count = recover_count - recoverCountYes;
				FnTenderRecover recover = getRecoverMoney(tenderTransfer.getTenderId());
				if(recover != null){
					wait_recover_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
					wait_recover_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
				}
				transfer_status = tenderTransfer.getStatus();
				transfer_amount = tenderTransfer.getAmount();
				status_name = "转让中";
				id = tenderTransfer.getTenderId();
				changeStatus = "1";
				map.put("wait_period", wait_recover_count);//待还期数
				map.put("period", tender.getRecoverCount());//总期数
				map.put("transfer_money", wait_recover_principal.add(wait_recover_interest));//待收本息
				
				map.put("expire_date", expire_date);
				map.put("loan_id", loan_id);
				map.put("loan_name", loan_name);
				map.put("recover_count", recover_count);
				map.put("apr", apr);
				map.put("wait_recover_count", wait_recover_count);
				map.put("wait_recover_principal", wait_recover_principal);
				map.put("wait_recover_interest", wait_recover_interest);
				map.put("transfer_status", transfer_status);
				map.put("transfer_status_name", status_name);
				map.put("id", id);
				map.put("changeStatus", changeStatus);
				map.put("transfer_amount", transfer_amount);
				transferTenderList.add(map);
			}
			for(FnTenderTransfer tenderTransfer:successTransferList){//转让成功
				wait_recover_principal = BigDecimal.ZERO;
				wait_recover_interest = BigDecimal.ZERO;
				Map<String,Object> map = new HashMap<String,Object>();
				success_date = DateUtil.dateFormatSlash(tenderTransfer.getSuccessTime());
				FnTender tender = getTender(tenderTransfer.getTenderId());
				expire_date = DateUtil.dateFormatSlash(tender.getExpireTime());
				loan_id = tenderTransfer.getLoanId();
				loan_name = tenderTransfer.getLoanName();
				recover_count = tenderTransfer.getTotalPeriod();
				apr = tenderTransfer.getApr();
				Integer recoverCountYes = tenderTransfer.getPeriod() == null ?0:tenderTransfer.getPeriod();
				wait_recover_count = recover_count - recoverCountYes + 1;
				
			/*	FnTenderRecover recover = getRecoverMoney(tenderTransfer.getTenderId());
				if(recover != null){
					wait_recover_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
					wait_recover_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
				}*/
				
				FnTenderTransfer transfer = getTransfer(tender.getId());
				if(transfer != null){
					wait_recover_principal = transfer.getWaitPrincipal();
					wait_recover_interest = transfer.getWaitInterest();
				}
				
				transfer_status = tenderTransfer.getStatus();
				transfer_amount = tenderTransfer.getAmount();
				status_name = "转让成功";
				id = tenderTransfer.getTenderId();
				changeStatus = "2";
				
				map.put("wait_period", wait_recover_count);//待还期数
				map.put("period", tender.getRecoverCount());//总期数
				map.put("transfer_money", wait_recover_principal.add(wait_recover_interest));//待收本息
				
				map.put("success_date", success_date);
				map.put("expire_date", expire_date);
				map.put("loan_id", loan_id);
				map.put("loan_name", loan_name);
				map.put("recover_count", recover_count);
				map.put("apr", apr);
				map.put("wait_recover_count", wait_recover_count);
				if (wait_recover_count == 0) {
					map.put("wait_recover_principal", BigDecimal.ZERO);
					map.put("wait_recover_interest", BigDecimal.ZERO);
				} else {
					map.put("wait_recover_principal", wait_recover_principal.compareTo(BigDecimal.ZERO) > 0 ? wait_recover_principal : "-");
					map.put("wait_recover_interest", wait_recover_interest.compareTo(BigDecimal.ZERO) > 0 ? wait_recover_interest : "-");
				}
				map.put("transfer_status", transfer_status);
				map.put("transfer_status_name", status_name);
				map.put("id", id);
				map.put("changeStatus", changeStatus);
				map.put("transfer_amount", transfer_amount);
				transferTenderList.add(map);
			}
			for(FnTenderTransfer tenderTransfer:cancelTransferList){//已撤销
				wait_recover_principal = BigDecimal.ZERO;
				wait_recover_interest = BigDecimal.ZERO;
				Map<String,Object> map = new HashMap<String,Object>();
			//	success_date = DateUtil.dateFormatSlash(tenderTransfer.getAddTime());
				FnTender tender = getTender(tenderTransfer.getTenderId());
				expire_date = DateUtil.dateFormatSlash(tender.getExpireTime());
				loan_id = tenderTransfer.getLoanId();
				loan_name = tenderTransfer.getLoanName();
				recover_count = tenderTransfer.getTotalPeriod();
				apr = tenderTransfer.getApr();
				Integer recoverCountYes = tender.getRecoverCountYes() == null ?0:tender.getRecoverCountYes();
				wait_recover_count = recover_count - recoverCountYes;
				FnTenderRecover recover = getRecoverMoney(tenderTransfer.getTenderId());
				if(recover != null){
					wait_recover_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
					wait_recover_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
				}
				transfer_status = tenderTransfer.getStatus();
				transfer_amount = tenderTransfer.getAmount();
				status_name = "已撤销";
				id = tenderTransfer.getTenderId();
				changeStatus = "-1";
				map.put("wait_period", wait_recover_count);//待还期数
				map.put("period", tender.getRecoverCount());//总期数
				map.put("transfer_money", wait_recover_principal.add(wait_recover_interest));//待收本息
				
			//	map.put("success_date", success_date);
				map.put("expire_date", expire_date);
				map.put("loan_id", loan_id);
				map.put("loan_name", loan_name);
				map.put("recover_count", recover_count);
				map.put("apr", apr);
				map.put("wait_recover_count", wait_recover_count);
				map.put("wait_recover_principal", wait_recover_principal);
				map.put("wait_recover_interest", wait_recover_interest);
				map.put("transfer_status", transfer_status);
				map.put("transfer_status_name", status_name);
				map.put("id", id);
				map.put("changeStatus", changeStatus);
				map.put("transfer_amount", transfer_amount);
				transferTenderList.add(map);
			}
			Page pageObj = new Page();
			pageObj.setEpage(10);
			pageObj.setPage(page==null?1:Integer.valueOf(page));
			pageObj.setTotal_items(transferTenderList.size());
			if(transferTenderList != null){
				if(transferTenderList.size() / pageObj.getEpage() < 1){
					pageObj.setItems(transferTenderList);
				}else{
					List<Map<String,Object>> newTransferList = new ArrayList<Map<String,Object>>();
					for(int i = (pageObj.getPage()-1)*pageObj.getEpage(); i < pageObj.getPage()*pageObj.getEpage(); i++){
							if(i < transferTenderList.size()){
								newTransferList.add(transferTenderList.get(i));
							}
					}
					pageObj.setItems(newTransferList);
				}
			}
			return successJsonResonse(pageObj);
		}else if("buy".equals(statusNid)){//购买记录
			Page pageObj = getAllBuyTransfer(user,true,page == null ? null : Integer.valueOf(page));
			return successJsonResonse(pageObj);
		}
		return null;
	}
	/**
	 * 全部购买记录
	 */
	@SuppressWarnings("unchecked")
	public Page getAllBuyTransfer(MbMember user,boolean isAll,Integer page) throws Exception{
		List<FnTenderTransfer> inRecoverList = (List<FnTenderTransfer>) getInRecover(user,isAll,null);//回收中
//		Page inPage = (Page) getInRecover(user,isAll,page);
//		Page hasPage  = (Page) getHasRecover(user,isAll,page);
//		List<FnTenderTransfer> inRecoverList = inPage.getItems();
//		List<FnTenderTransfer> hasRecoverList = hasPage.getItems();
		List<FnTenderTransfer> hasRecoverList = (List<FnTenderTransfer>) getHasRecover(user,isAll,null);//回收完
		Long loan_id;//借款标id
		String loan_name;//借款标名称
		Integer total_period;//期限
		BigDecimal apr;//利率
		Integer wait_recover_count;//待还期数
		Integer buy_period;//已购期数
		BigDecimal wait_principal = BigDecimal.ZERO;//待收本金
		BigDecimal wait_interest = BigDecimal.ZERO;//待收利息
		String status_name;//状态
		Long id;
		List<Map<String,Object>> transferTenderList = new ArrayList<Map<String,Object>>();
		for(FnTenderTransfer transfer:inRecoverList){
			wait_principal = BigDecimal.ZERO;
			wait_interest = BigDecimal.ZERO;
			Map<String,Object> map = new HashMap<String,Object>();
			total_period = transfer.getTotalPeriod();
			map.put("transfer_total", null);//债权成功转让金额
			map.put("transfer_interest _total", null);//债权成功转出盈亏
			map.put("transfer_buy_total", transfer.getAmount());//债权成功购入金额
			map.put("transfer_buy_interest_total", NumberUtils.sub(transfer.getAmount(), transfer.getAmountMoney()));//债权成功购入盈亏
			map.put("transfer_amount", transfer.getAmount());//转让价格
			FnTender tender = getTender(transfer.getTenderId());
			Integer recoverCountYes = tender.getRecoverCountYes() == null ?0:tender.getRecoverCountYes();
			wait_recover_count = total_period - recoverCountYes;
			buy_period = transfer.getTotalPeriod()-transfer.getPeriod()+1;
			FnTenderRecover recover = getRecoverMoney(transfer.getTenderId());
			if(recover != null){
				wait_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
				wait_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
			}
			status_name = "回收中";
			map.put("wait_period", wait_recover_count);//待还期数
			map.put("period", total_period);//总期数
			map.put("transfer_status", transfer.getStatus());//债权状态
			map.put("buy_repay", transfer.getStatus()) ;
			map.put("buy_repay_status", status_name);
			map.put("transfer_status_name", status_name);//状态名
			map.put("repay_status", transfer.getRepayStatus());
			map.put("transfer_money", transfer.getAmountMoney());//债权价值
			map.put("id", tender.getId());//投资id
			map.put("transfer_id", transfer.getId());//债权转让id
			map.put("wait_principal", wait_principal);
			map.put("wait_interest", wait_interest);
			map.put("wait_principal_interest", wait_principal.add(wait_interest));
			transferTenderList.add(map);
	}
		for(FnTenderTransfer transfer:hasRecoverList){//回收完
			wait_principal = BigDecimal.ZERO;
			wait_interest = BigDecimal.ZERO;
			Map<String,Object> map = new HashMap<String,Object>();
			
			total_period = transfer.getTotalPeriod();
			map.put("transfer_total", null);//债权成功转让金额
			map.put("transfer_interest _total", null);//债权成功转出盈亏
			map.put("transfer_buy_total", transfer.getAmount());//债权成功购入金额
			map.put("transfer_buy_interest_total", NumberUtils.sub(transfer.getAmount(), transfer.getAmountMoney()));//债权成功购入盈亏
			map.put("transfer_amount", transfer.getAmount());//转让价格
			FnTender tender = getTender(transfer.getTenderId());
			Integer recoverCountYes = tender.getRecoverCountYes() == null ?0:tender.getRecoverCountYes();
			wait_recover_count = total_period - recoverCountYes;
			buy_period = transfer.getTotalPeriod()-transfer.getPeriod()+1;
			FnTenderRecover recover = getRecoverMoney(transfer.getTenderId());
			if(recover != null){
				wait_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
				wait_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
			}
			status_name = "回收完";
			if("prepayment".equals(tender.getRecoverType())){
				status_name = "提前还款";
			}
			map.put("wait_period", wait_recover_count);//待还期数
			map.put("period", total_period);//总期数
			map.put("transfer_status", transfer.getStatus());//债权状态
			map.put("buy_repay", transfer.getStatus()) ;
			map.put("buy_repay_status", status_name);
			map.put("transfer_status_name", status_name);//状态名
			map.put("repay_status", transfer.getRepayStatus());
			map.put("transfer_money", transfer.getAmountMoney());//债权价值
			map.put("id", tender.getId());//投资id
			map.put("transfer_id", transfer.getId());//债权转让id
			map.put("wait_principal", wait_principal);
			map.put("wait_interest", wait_interest);
			map.put("wait_principal_interest", wait_principal.add(wait_interest));
			transferTenderList.add(map);
		}
		Page pageObj = new Page();
		pageObj.setEpage(10);
		pageObj.setPage(page==null?1:page);
		pageObj.setTotal_items(transferTenderList.size());
//		pageObj.setItems(transferTenderList);
		if(transferTenderList.size() > 0 && transferTenderList != null){
			if(pageObj.getTotal_pages() < pageObj.getPage()){
				return null;
			}
			if(transferTenderList.size() / pageObj.getEpage() < 1){
				pageObj.setItems(transferTenderList);
			}else{
				List<Map<String,Object>> newTransferList = new ArrayList<Map<String,Object>>();
				for(int i = (pageObj.getPage()-1)*pageObj.getEpage(); i < pageObj.getPage()*pageObj.getEpage(); i++){
						if(i < transferTenderList.size()){
							newTransferList.add(transferTenderList.get(i));
						}
				}
				pageObj.setItems(newTransferList);
			}
		}
		return pageObj;
	}
	/**
	 * 债权转让明细
	 * @throws Exception 
	 */
	public DyPhoneResponse transferInfo(String memberId,String tenderId) throws Exception{
		FnTender tender = this.getTender(Long.valueOf(tenderId));
		FnTenderTransfer transfer = this.getTransfer(tender.getId());
//		FnLoan loan = this.getLoan(tender.getLoanId());
		Integer recoverCountWait = tender.getRecoverCount() - tender.getRecoverCountYes() ;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("loan_name", tender.getLoanName());//转让标标题
		/**
		 * 计算待还期数（转让期数）
		 * 固定为转让时的待还期数
		 */
		int period = 0;
		if(transfer == null){
			period = tender.getRecoverCount() - tender.getRecoverCountYes();
		}else{
			if(transfer.getStatus() == -1 || transfer.getStatus() == 1){
				period = tender.getRecoverCount() - tender.getRecoverCountYes();
			}else{
				period = transfer.getTotalPeriod() - transfer.getPeriod() +1;
			}
		}
		map.put("period", period);//转让期数
		map.put("total_period", tender.getRecoverCount());//总期数
		map.put("amount", transfer == null ? null : transfer.getAmount());//转让价格
		/**
		 * 状态为转让成功的获取转让时间，其他状态获取还款期限
		 */
		
		if(transfer != null && transfer.getStatus() == 2){
			map.put("recover_time", DateUtil.dateTimeFormat(transfer.getSuccessTime()));
		}else{
			QueryItem repayItem = new QueryItem(Module.LOAN,Function.LN_REPAY);
			repayItem.setWhere(Where.eq("loan_id", tender.getLoanId()));
			repayItem.setFields("id,next_repay_time");
			FnLoanRepay loanRepay = this.baseService.getOne(repayItem, FnLoanRepay.class); 
			map.put("recover_time", DateUtil.dateTimeFormat(loanRepay.getNextRepayTime()));
		}
		
		//债权转让系数最小和最大值,手续费系数
		String transferMin = getTransferCoefficient("min");
		String transferMax = getTransferCoefficient("max");
		map.put("transfer_coefficient_min", transferMin);//转让系数最小值
		map.put("transfer_coefficient_max", transferMax);//转让系数最大值
		map.put("transfer_id", transfer == null ? null : transfer.getId());//撤销用的transferId
		map.put("tender_id", tender.getId());//转让时用的id
		map.put("coefficient", transfer == null ? null : transfer.getCoefficient());//转让系数
		map.put("transfer_status", transfer == null ? -1 : transfer.getStatus()) ;
//		BigDecimal income = investInterest(transfer.getAmount(),loan.getPeriod(),loan.getApr(),loan.getRepayType(),null);
//		map.put("income", income);//预期收益
//		map.put("amount_money", transfer == null ? this.getAmoutMoney(tender) : transfer.getAmountMoney());//债权价值
		map.put("amount_money", transfer == null ? this.getAmoutMoney(tender) : transfer.getAmountMoney());//债权价值
		if(transfer != null && transfer.getRepayStatus() == -1 && transfer.getStatus() == -1){
			map.put("amount_money", this.getAmoutMoney(tender));//债权价值
			map.put("period", recoverCountWait);//转让期数
			map.put("amount", null) ;
		}
		QueryItem sysItem = new QueryItem("system", "config");
		sysItem.setFields("value");
		sysItem.getWhere().add(Where.eq("id", 64));//转让费用系数
		SysSystemConfig config = this.baseService.getOne(sysItem,  SysSystemConfig.class);
		//map.put("transfer_fee",config.getValue());
		
		//modify by panxh at 2016年9月30日 for 区分普通会员、VIP转让费用系数
		String[] value = config.getValue().split(",");
		String memberVipLevel = vipService.getMemberVipLevel(Long.valueOf(memberId));
		if(StringUtils.isNotBlank(memberVipLevel)) {
			BigDecimal vipFee = new BigDecimal(value.length == 2 ? (value[1] == null ? value[0] : value[1]): value[0]);
			BigDecimal vipFeeScale = vipService.getVipCategory(memberVipLevel).getFeeScale();
			vipFeeScale = vipFeeScale == null ? BigDecimal.ONE : vipFeeScale.divide(new BigDecimal(100));
			map.put("transfer_fee", NumberUtils.round(NumberUtils.mul(vipFee, vipFeeScale)).toString());
		} else {
			map.put("transfer_fee", value[0]);
		}
		//end modify by panxh at 2016年9月30日 for 区分普通会员、VIP转让费用系数
		
		//撤销的次数
		if(transfer != null){
 			map.put("cancel_count", null == transfer.getCancelCount()? 0 : transfer.getCancelCount());
		} else {
			map.put("cancel_count", 0);
		}
		
		return successJsonResonse(map);
	}
	
	/**
	 * 债权购买明细
	 * @throws Exception 
	 */
	public Map<String,Object> myTransferBuyInfo(String memberId,String transferId,String page,String epage) throws Exception{
		FnTenderTransfer transfer = this.getTransferById(Long.valueOf(transferId));
		FnLoan loan = this.getLoan(transfer.getLoanId());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("loan_name", transfer.getLoanName());//转让标标题
		//map.put("period", transfer == null ? null : transfer.getTotalPeriod() - transfer.getPeriod());//转让期数
		int period = 0;
		List<FnTenderRecover> recoverList = getTenderRecover(transfer.getTenderId());
		if(recoverList!=null && !recoverList.isEmpty()){
			for (FnTenderRecover fnTenderRecover : recoverList) {
				if(fnTenderRecover.getTransferMemberId().equals(Long.valueOf(memberId))){
					if(fnTenderRecover.getStatus() == -1){
						period++;
					}
				}
			}
		}
		map.put("period", period);//转让期数
		map.put("total_period", transfer.getTotalPeriod());//总期数
		map.put("amount_money", transfer == null ? null : transfer.getAmountMoney());//债权价值
		map.put("amount", transfer == null ? null : transfer.getAmount().toString());//转让价格
		map.put("repay_status", transfer.getRepayStatus());
		map.put("transfer_id", transfer.getId());
		/*BigDecimal income = investInterest(transfer.getAmountMoney(),transfer.getTotalPeriod() - transfer.getPeriod() + 1,loan.getApr(),loan.getRepayType(),null);
		*/
/*		map.put("income", NumberUtils.sub(NumberUtils.add(transfer.getWaitInterest(),transfer.getWaitPrincipal()),transfer.getAmount()));//预期收益（购买债权时候的本息-转让价格）
*/		BigDecimal totalPriInt = getWaitPriInt(transfer.getPeriod(),transfer.getLoanId(),transfer.getTenderId());
        transfer.setAmountMoney(totalPriInt);
		map.put("income",transfer.getAmountMoney().subtract(transfer.getAmount()));
		//还款详情
		QueryItem recoverItem = new QueryItem(Module.LOAN,Function.LN_RECOVER);
		recoverItem.getWhere().add(Where.eq("tender_id", transfer.getTenderId()));
		recoverItem.getWhere().add(new Where ("period_no", transfer.getPeriod(),">="));
		recoverItem.setPage(page == null ? 1 : Integer.valueOf(page));
		recoverItem.setLimit(epage == null ? 15 : Integer.valueOf(epage));
		Page pageObj = this.baseService.getPage(recoverItem, Map.class);
		
		//add by panxh at 2016年10月18日  for 修改到期时间
		QueryItem queryExpireTim = new QueryItem();
		queryExpireTim.setFields("max(recover_time) recover_time");
		queryExpireTim.getWhere().add(Where.eq("tender_id", transfer.getTenderId()));
		Map<String, Object> expireMap = this.getOne(Module.LOAN,Function.LN_RECOVER, queryExpireTim);
		map.put("expire_time", expireMap.get("recover_time"));
		//end by panxh at 2016年10月18日   for 修改到期时间
		
		if(pageObj!=null && pageObj.getItems() != null & !pageObj.getItems().isEmpty()){
			List<Map> recovers = (List<Map>)pageObj.getItems();
			//map.put("expire_time", recovers.get(recovers.size()-1).get("recover_time"));
			for (Map map2 : recovers) {
				map2.put("recover_time", DateUtil.dateFormat(Long.valueOf(map2.get("recover_time").toString())));
				if("1".equals(map2.get("status").toString())){
					
					if("1".equals(map2.get("prepayment_status").toString())){
						map2.put("status_name", "提前还款");
					}else{
						map2.put("status_name", "已回款");
					}
				}else{
					map2.put("status_name", "未回款");
				}
			}
		}
		map.put("recover", pageObj);
		return map;
	}
	
	/**
	 * 通过tenderId查询我的投资回款表
	 * @param tenderId
	 * @return
	 * @throws Exception
	 */
	private List<FnTenderRecover> getTenderRecover(Long tenderId) throws Exception {
		QueryItem recoverItem = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		recoverItem.getWhere().add(Where.eq("tender_id", tenderId));
		return this.baseService.getList(recoverItem, FnTenderRecover.class);
	}
	
	/**
	 * 债权转让提交
	 * @throws Exception 
	 */
	public DyPhoneResponse transferSub(String memberId,String tenderId,String coefficient,String paypassword) throws Exception{
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		BigDecimal transferMin = new BigDecimal(getTransferCoefficient("min"));
		BigDecimal transferMax = new BigDecimal(getTransferCoefficient("max"));
		//投资		
		FnTender tender = getTender(Long.valueOf(tenderId));
		//by lhh 新手标无法进行债权转让
		FnLoan loan = getLoan(tender.getLoanId()) ;
		if(null != loan.getAdditionalStatus() && 1 == loan.getAdditionalStatus()){
			return errorJsonResonse("新手标无法进行债权转让");
		}
		
		BigDecimal coefficientBig = new BigDecimal(coefficient);
		if(coefficientBig.compareTo(transferMin) == -1 || coefficientBig.compareTo(transferMax) == 1){
			return errorJsonResonse("转让系数不正确");
		}
		//撤销三次以上无法转让
		FnTenderTransfer transfer = getTransfer(tender.getId());
		if(transfer!=null && transfer.getCancelCount() !=null && transfer.getCancelCount()>3){
			return errorJsonResonse("该债权无法被转让");
		}
		//判断该债权是否可以转让
		if(tender.getSuccessTime()==null||tender.getRecoverStatus() ==1)return errorJsonResonse("该债权无法被转让");
		if(tender.getTransferStatus() == 1 || tender.getTransferStatus() == -2)return errorJsonResonse("该债权已被转让或正在转让中");
		if(tender.getRecoverType()!=null && tender.getRepayType() == 5)return errorJsonResonse("该债权无法被转让");
		//判断该债权最近一笔待收是否逾期 
		if(tender.getNextRecoverTime() < DateUtil.getCurrentTime()) return errorJsonResonse("该债权无法被转让");
		//该债权的持有天数是否达到系统设置可转让天数(fn_tender表success_time字段+系统设定持有时间与当前时间做比较)
		String value = getTransferDay();//系统设定持有时间
		if(value !=null && !"".equals(value)){
			if(DateUtil.daysBetween(DateUtil.dateParse(tender.getSuccessTime()),new Date()) < Integer.valueOf(value)){
				return errorJsonResonse("该债权无法被转让");
			}
		}
		//判断支付密码
		if(!BaseController.isTrust){
			if(!SecurityUtil.md5(SecurityUtil.sha1(user.getPwdAttach()+paypassword)).equals(user.getPaypassword())){
				return errorJsonResonse("支付密码错误!");
			}
		}
		//债权转让金额
		BigDecimal amountMoney = getAmoutMoney(tender);
		//操作转让记录
		financeService.transfer(user.getId(),tender.getId(),loan.getId(),coefficientBig.intValue(),amountMoney,getTransferCoefficient("fee"));
		return successJsonResonse("转让成功");
	}
	
	/**
	 * 撤销提交
	 * @throws Exception 
	 */
	public DyPhoneResponse cancelSubmit(String memberId,String transferId) throws Exception{
		//获取债权
		FnTenderTransfer transfer = getTransferById(Long.valueOf(transferId));
		if(transfer.getStatus()!=1)return errorJsonResonse("该债权无法撤销!");
		if(transfer.getCancelCount()!=null && transfer.getCancelCount() >=3)return errorJsonResonse("该债权无法撤销!");
		//获取投资
		FnTender tender = getTender(transfer.getTenderId());
		tender.setTransferStatus(-3);
		this.baseService.updateById(Module.LOAN, Function.LN_TENDER, tender);
		transfer.setStatus(-1);
		transfer.setCancelTime(DateUtil.getCurrentTime());
		transfer.setCancelCount(transfer.getCancelCount() == null ? 1 : transfer.getCancelCount() + 1);
		this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
		Map<String,Object> cancelCountMap = new HashMap<String, Object>();
		cancelCountMap.put("cancel_count", transfer.getCancelCount());
		return successJsonResonse(cancelCountMap);
	}
	
	/**
	 * 可以转让获取数据
	 * @throws Exception 
	 */
	public Object getCanTransfer(MbMember user,boolean isAll,Integer page) throws Exception{
		//系统设定持有时间
		String value = getTransferDay();
		QueryItem canItem = new QueryItem(Module.LOAN, Function.LN_TENDER);
		List<Where> canWhere = new ArrayList<Where>();
		canWhere.add(new Where("member_id",user.getId()));
		canWhere.add(new Where("recover_status",-1));
//						canWhere.add(Where.notIn("recover_type", "1,2,3")) ;
		List<NameValue> ands = new ArrayList<NameValue>();
		ands.add(new NameValue("transfer_status",-1,"=",true));
		ands.add(new NameValue("transfer_status",-3,"=",true));
		canWhere.add(Where.expression("(recover_count_yes < recover_count)", false));
		canWhere.add(new Where(ands));
		//所有新手标不出现在债权转让列表 Start by lhh //所有流转标不出现在债权转让列表中
		QueryItem loanQuery = new QueryItem() ;
		List<Where> whereList = new ArrayList<Where>();
		List<NameValue> orList = new ArrayList<NameValue>();
		orList.add(new NameValue("additional_status", 1, EQ,true));
		orList.add(new NameValue("category_type", 3, EQ, true));
		orList.add(new NameValue("loan_repay_status", 3, EQ, true));
		whereList.add(Where.setAndList(orList));	
		loanQuery.setWhere(whereList);
		
		List<FnLoan> loans = this.getListByEntity(loanQuery, Module.LOAN, Function.LN_LOAN, FnLoan.class) ;
		String loanIds = "" ;
		if(null != loans){	
			for (FnLoan fnLoan : loans) {
				loanIds += ( fnLoan.getId() + "," ) ;
			}
			int length = loanIds.length() ;
			if(length > 0){
				loanIds=loanIds.substring(0, length - 1) ;
				canWhere.add(Where.notIn("loan_id", loanIds)) ;
			}
		}
		
		//End
//						canWhere.add(new Where("transfer_status",-1));
		canWhere.add(new Where("repay_type",5,"!="));
		canWhere.add(new Where("next_recover_time",DateUtil.getCurrentTime(),">="));
		canWhere.add(new Where("cancel_count",3,"<"));
		//canWhere.add(new Where("recover_type",2,"!="));
		if(value != null && !"".equals(value)){
			canWhere.add(new Where("success_time",DateUtil.addDay(DateUtil.getCurrentTime(), -Integer.valueOf(value)),"<="));
		}
		canWhere.add(new Where("status",1));
		canItem.setWhere(canWhere);
		if(!isAll){
			canItem.setPage(page == null ? 1:page);
			canItem.setLimit(10);
			Page pageObj = this.baseService.getPage(canItem, FnTender.class);
			//判断最近一笔待还是否逾期
			List<FnTender> tenderList = pageObj.getItems();
			 for (Iterator<FnTender> iter = tenderList.iterator(); iter.hasNext();) {  
				 FnTender tender = iter.next();
				 FnTenderRecover recover = getRecover(tender.getId(), tender.getRecoverCountYes()+1);
				 if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
		            	iter.remove();
				 }
				}
			return pageObj;
		}else{
			List<FnTender> canTenderList = this.getListByEntity(canItem, "loan", "tender", FnTender.class);
			//判断最近一笔待还是否逾期
			 for (Iterator<FnTender> iter = canTenderList.iterator(); iter.hasNext();) {  
				 FnTender tender = iter.next();
				 FnTenderRecover recover = getRecover(tender.getId(), tender.getRecoverCountYes()+1);
				 if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
		            	iter.remove();
				 }
				 
				 /**
				  * 网站垫付未还款过滤
				  */
				 if (tender.getRecoverCountYes() > 0 &&  tender.getRepayType() != null && tender.getRepayType() == 4) {
					QueryItem preQueryItem = new QueryItem(Module.LOAN, Function.LN_REPAYPERIOD);
					preQueryItem.setFields("status");
					preQueryItem.setWhere(Where.eq("loan_id", tender.getLoanId()));
					preQueryItem.setWhere(Where.eq("period_no", tender.getRecoverCountYes()));
					preQueryItem.setWhere(Where.eq("status", -1));
					preQueryItem.setFields("id");
					
					List<Map<String, Object>> preRepayList = (List<Map<String, Object>>)this.baseService.getList(preQueryItem);
					if (preRepayList != null && preRepayList.size() > 0) {
						iter.remove();
					}
				}
			}
			return canTenderList;
		}
	}
	
	/**
	 * 正在转让获取数据
	 */
	public Object getInTransfer(MbMember user,boolean isAll,Integer page) throws Exception{
		QueryItem inItem = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		List<Where> inWhere = new ArrayList<Where>();
		inWhere.add(new Where("member_id",user.getId()));
		inWhere.add(new Where("status",1));
		inItem.setWhere(inWhere);
		if(!isAll){
			inItem.setPage(page == null ? 1:page);
			inItem.setLimit(10);
			Page pageObj = this.baseService.getPage(inItem, FnTenderTransfer.class);
			List<FnTenderTransfer> transferList = pageObj.getItems();
			for (Iterator<FnTenderTransfer> iter = transferList.iterator(); iter.hasNext();) {  
	            FnTenderTransfer transfer = iter.next();
	            FnTender tender = getTender(transfer.getTenderId());
	            FnTenderRecover recover = getRecover(transfer.getTenderId(), tender.getRecoverCountYes() + 1);
	            //判断该标是否逾期未还
				if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
	            	tender.setTransferStatus(-3);
					this.baseService.updateById(Module.LOAN, Function.LN_TENDER, tender);
					transfer.setStatus(-1);
					transfer.setCancelTime(DateUtil.getCurrentTime());
					transfer.setCancelRemark("该标逾期债权撤销");
					this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
	            	iter.remove();
				}
			}
			return pageObj;
		}else{
			List<FnTenderTransfer> inTenderList = this.baseService.getList(inItem, FnTenderTransfer.class);
			for (Iterator<FnTenderTransfer> iter = inTenderList.iterator(); iter.hasNext();) {  
	            FnTenderTransfer transfer = iter.next();
	            FnTender tender = getTender(transfer.getTenderId());
	            FnTenderRecover recover = getRecover(transfer.getTenderId(), tender.getRecoverCountYes() + 1);
	            //判断该标是否逾期未还
				if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
	            	tender.setTransferStatus(-3);
					this.baseService.updateById(Module.LOAN, Function.LN_TENDER, tender);
					transfer.setStatus(-1);
					transfer.setCancelTime(DateUtil.getCurrentTime());
					transfer.setCancelRemark("该标逾期债权撤销");
					this.baseService.updateById(Module.LOAN, Function.LN_TRANSFER, transfer);
	            	iter.remove();
				}
			}
			return inTenderList;
		}
	}
	
	/**
	 * 回收中获取数据
	 */
	public Object getInRecover(MbMember user,boolean isAll,Integer page) throws Exception{
		QueryItem inRecoverItem = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		List<Where> inRecoverWhere = new ArrayList<Where>();
		inRecoverWhere.add(new Where("buy_member_id",user.getId()));
		inRecoverWhere.add(new Where("repay_status",-1));
		inRecoverWhere.add(new Where("status",2));
		inRecoverItem.setWhere(inRecoverWhere);
		if(!isAll){
			inRecoverItem.setPage(page == null ? 1:page);
			inRecoverItem.setLimit(10);
			Page pageObj = this.baseService.getPage(inRecoverItem, FnTenderTransfer.class);
			return pageObj;
		}else{
			List<FnTenderTransfer> inRecoverList = this.baseService.getList(inRecoverItem, FnTenderTransfer.class);
			return inRecoverList;
		}
	}
	
	/**
	 * 回收完获取数据
	 */
	public Object getHasRecover(MbMember user,boolean isAll,Integer page) throws Exception{
		QueryItem hasRecoverItem = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		List<Where> hasRecoverWhere = new ArrayList<Where>();
		hasRecoverWhere.add(new Where("buy_member_id",user.getId()));
		hasRecoverWhere.add(new Where("repay_status",1));
		hasRecoverWhere.add(new Where("status",2));
		hasRecoverItem.setWhere(hasRecoverWhere);
		if(!isAll){
			hasRecoverItem.setPage(page == null ? 1:page);
			hasRecoverItem.setLimit(10);
			Page pageObj = this.baseService.getPage(hasRecoverItem, FnTenderTransfer.class);
			return pageObj;
		}else{
			List<FnTenderTransfer> hasRecoverList = this.baseService.getList(hasRecoverItem, FnTenderTransfer.class);
			return hasRecoverList;
		}
	}
	
	/**
	 * 转让成功获取数据
	 */
	public Object getSuccessTransfer(MbMember user,boolean isAll,Integer page) throws Exception{
		QueryItem transItem = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		List<Where> transWhere = new ArrayList<Where>();
		transWhere.add(new Where("member_id",user.getId()));
		transWhere.add(new Where("status",2));
		transItem.setWhere(transWhere);
		if(!isAll){
			transItem.setPage(page == null ? 1:page);
			transItem.setLimit(10);
			Page pageObj = this.baseService.getPage(transItem, FnTenderTransfer.class);
			return pageObj;
		}else{
			List<FnTenderTransfer> transferList = this.baseService.getList(transItem, FnTenderTransfer.class);
			return transferList;
		}
	}
	
	/**
	 * 根据id获取借款标
	 * @throws Exception 
	 * 
	 */
	private FnLoan getLoan(Long loanId) throws Exception{
		QueryItem loanItem  = new QueryItem(Module.LOAN,Function.LN_LOAN);
		loanItem.getWhere().add(Where.eq("id", loanId));
		FnLoan loan = this.baseService.getOne(loanItem,FnLoan.class);
		return loan;
	}
	
	/**
	 * 根据id获取投标
	 * @throws Exception 
	 * 
	 */
	private FnTender getTender(Long tenderId) throws Exception{
		QueryItem tenderItem  = new QueryItem(Module.LOAN,Function.LN_TENDER);
		tenderItem.getWhere().add(Where.eq("id", tenderId));
		FnTender tender = this.baseService.getOne(tenderItem, FnTender.class);
		return tender;
	}
	
	/**
	 * 根据tender_id获取债权
	 * @throws Exception 
	 * 
	 */
	private FnTenderTransfer getTransfer(Long tenderId) throws Exception{
		QueryItem transferItem  = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		transferItem.getWhere().add(Where.eq("tender_id", tenderId));
		FnTenderTransfer transfer = this.baseService.getOne(transferItem, FnTenderTransfer.class);
		return transfer;
	}
	
	/**
	 * 根据id获取债权
	 * @throws Exception 
	 * 
	 */
	private FnTenderTransfer getTransferById(Long id) throws Exception{
		QueryItem transferItem  = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		transferItem.getWhere().add(Where.eq("id", id));
		FnTenderTransfer transfer = this.baseService.getOne(transferItem, FnTenderTransfer.class);
		return transfer;
	}
	
	
	/**
	 * 获取债权转让系数最大最小值
	 * 最小值：131
	 * 最大值：132
	 * @throws Exception 
	 */
	private String getTransferCoefficient(String type) throws Exception{
		QueryItem sysItem = new QueryItem(Module.SYSTEM,Function.SYS_CONFIG);
		sysItem.setFields("value");
		if("min".equals(type)){
			sysItem.getWhere().add(Where.eq("id", 131));//最小值
		}else if("max".equals(type)){
			sysItem.getWhere().add(Where.eq("id", 132));//最大值
		}else if("fee".equals(type)){
			sysItem.getWhere().add(Where.eq("id", 64));//转让费用系数
		}
		SysSystemConfig config = this.baseService.getOne(sysItem, SysSystemConfig.class);
		return config.getValue();
	}
	
	
	/**
	 * 债权系统持有天数
	 * @throws Exception 
	 */
	private String getTransferDay() throws Exception{
		QueryItem sysItem = new QueryItem(Module.SYSTEM,Function.SYS_CONFIG);
		sysItem.setFields("value");
		sysItem.getWhere().add(Where.eq("id", 122));
		SysSystemConfig config = this.baseService.getOne(sysItem, SysSystemConfig.class);
		return config.getValue();
	}
	
	/**
	 * 根据当前期数和tenderid获取还款表
	 * @throws Exception 
	 */
	private FnTenderRecover getRecover(Long tenderId,int periodNo) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("period_no", periodNo));
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover;
	}
	
	/**
	 * 根据tenderid和当前期数获取之后已还款的还款表利息总和
	 */
	private BigDecimal getTotalInterest(Long tenderId,int periodNo) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("period_no", periodNo,">"));
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("status", 1));
		item.setFields("sum(interest_yes) interest_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item,FnTenderRecover.class);
		return recover == null?null:recover.getInterestYes();
	}
	
	
	/**
	 * 查询用户的债权转让列表
	 * @param user
	 * @return
	 * @throws DyServiceException 
	 */
	private List<FnTenderTransfer> getTransferList(MbMember user) throws DyServiceException {
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		item.getWhere().add(Where.eq("member_id", user.getId()));
		List<FnTenderTransfer> transferList = this.baseService.getList(item, FnTenderTransfer.class);
		return transferList;
	}
	
	/**
	 * 查询用户的债权购买列表
	 * @throws DyServiceException 
	 */
	private Page getBuyTransferList(MbMember user,String page,String epage) throws DyServiceException{
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		item.getWhere().add(Where.eq("buy_member_id", user.getId()));
		item.setPage(page == null ? 1 : Integer.valueOf(page));
		item.setLimit(epage == null ? 10 : Integer.valueOf(epage));
		Page pageObj  = this.baseService.getPage(item, FnTenderTransfer.class);
		return pageObj;
	}
	
	/**
	 * 计算预期收益
	 */
	public BigDecimal investInterest(BigDecimal amount,Integer period,BigDecimal apr,Integer repayType,Double awardScale) {
		BigDecimal interestTotal = BigDecimal.ZERO;//预期收益
		BigDecimal interest = BigDecimal.ZERO;//利息
		BigDecimal awardAmount = BigDecimal.ZERO;//奖励
		if (awardScale != null) {
			awardScale = awardScale / 100;
		} else {
			awardScale = 0D;
		}
		if (amount != null) {
			TenderCondition repayCondtion = new TenderCondition();
			repayCondtion.setAmount(amount);
			repayCondtion.setApr(apr);
			repayCondtion.setCurrentTime(DateUtil.getCurrentTime());
			repayCondtion.setPeriod(period);
			repayCondtion.setRepayType(repayType);
			Tender tender = RepayUtil.getRepayInfo(repayCondtion);
			if (tender != null) {
				interest = tender.getInterestAll();
			}
			awardAmount = NumberUtils.mul(amount, new BigDecimal(awardScale));
			interestTotal = interest.add(awardAmount);

		}
		return interestTotal;
	}
	
	/**
	 * 根据tenderid查询最后还款时间
	 * @throws DyServiceException 
	 */
	private String getRecoverTime(Long tenderId,Integer period) throws DyServiceException{
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_RECOVER);
		item.getWhere().add(Where.eq("tender_id", tenderId));
		item.setOrders("recover_time desc") ;
		List<FnTenderRecover> recoverList = this.baseService.getList(item, FnTenderRecover.class);
		String recoverTime = null;
		if(recoverList != null && recoverList.size() > 0){
			return recoverList.get(0).getRecoverTime().toString() ;
//			recoverTime = DateUtil.dateFormat(recoverList.get(0).getRecoverTime());
		}
		return recoverTime;
	}
	/**
	 * 债权转让盈亏
	 * @param memberId
	 * @throws Exception 
	 */
	public DyPhoneResponse myTransfer(Long memberId) throws Exception {
		Map<String,Object> transferContMap = new HashMap<String,Object>();
		MbMember user = this.getMbMember(memberId);
		//转让的债权
		List<FnTenderTransfer> transferList = (List<FnTenderTransfer>) getSuccessTransfer(user,true,null);
		
		BigDecimal totalAmount = new BigDecimal(0);//转让的总金额
		BigDecimal totalPriInt = new BigDecimal(0);//待收总本息
		for(FnTenderTransfer transfer:transferList){
			totalAmount = totalAmount.add(transfer.getAmount());
			totalPriInt = totalPriInt.add(getWaitPriInt(transfer.getPeriod(),transfer.getLoanId(),transfer.getTenderId()));
		}
				
		BigDecimal profit = totalAmount.subtract(totalPriInt);//债权转出盈亏
		/*transInfoMap.put("total_amount", totalAmount);
		transInfoMap.put("profit", profit);*/
		//购买的债权
		Map<String,Object> buyInfoMap = new HashMap<String,Object>();
		List<FnTenderTransfer> buyTransferList = getBuySuccessTransfer(user.getId());
		BigDecimal buyTotalAmount = new BigDecimal(0);//购买的总金额
		BigDecimal buyTotalPriInt = new BigDecimal(0);//待收总本息
		for(FnTenderTransfer transfer:buyTransferList){
			buyTotalAmount = buyTotalAmount.add(transfer.getAmount());
			buyTotalPriInt = buyTotalPriInt.add(getWaitPriInt(transfer.getPeriod(),transfer.getLoanId(),transfer.getTenderId()));
		}
		BigDecimal buyProfit = buyTotalPriInt.subtract(buyTotalAmount);//债权转入盈亏
		/*buyInfoMap.put("total_amount", buyTotalAmount) ;
		buyInfoMap.put("profit", buyProfit) ;
		
		transferContMap.put("transferInfo", transInfoMap);
		transferContMap.put("buyInfo", buyInfoMap);*/
		
		
		transferContMap.put("transfer_total", totalAmount);
		transferContMap.put("transfer_interest_total", profit);
		transferContMap.put("transfer_buy_total", buyTotalAmount);
		transferContMap.put("transfer_buy_interest_total", buyProfit);
		
		return successJsonResonse(transferContMap) ;
	}
	/**
	 * 转让成功获取数据
	 */
	public List<FnTenderTransfer> getSuccessTransfer(Long memberId) throws Exception{
		QueryItem transItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		List<Where> transWhere = new ArrayList<Where>();
		transWhere.add(new Where("member_id",memberId));
		transWhere.add(new Where("status",2));
		transItem.setWhere(transWhere);
		List<FnTenderTransfer> transferList =  this.baseService.getList(transItem, FnTenderTransfer.class);
		return transferList;
	}
	/**
	 * 购买成功
	 */
	public List<FnTenderTransfer> getBuySuccessTransfer(Long memberId) throws Exception{
		QueryItem buyItem = new QueryItem(Module.LOAN, Function.LN_TRANSFER);
		List<Where> buyWhere = new ArrayList<Where>();
		buyWhere.add(new Where("buy_member_id",memberId));
		buyWhere.add(new Where("status","2"));
		buyItem.setWhere(buyWhere);
		List<FnTenderTransfer> transferList = this.baseService.getList(buyItem, FnTenderTransfer.class);
		return transferList;
	}
	
	private FnTenderRecover getRecoverMoney(Long tenderId) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN, Function.LN_RECOVER);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("status", -1));
		item.setFields("sum(amount) amount,sum(amount_yes) amount_yes,sum(interest) interest,sum(interest_yes) interest_yes,sum(principal) principal ,sum(principal_yes) principal_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.baseService.getOne(item, FnTenderRecover.class);
		return recover;
	} 
	
	private BigDecimal getAmoutMoney(FnTender tender) throws Exception{
		FnLoan loan = getLoan(tender.getLoanId());
		//计算剩余本金
		BigDecimal waitPrincipal = NumberUtils.sub(tender.getRecoverPrincipal(), tender.getRecoverPrincipalYes());
		//获取复审时间
		Long successTime = tender.getSuccessTime();
		Date successDate = DateUtil.dateParse(successTime);
		Calendar successCal = Calendar.getInstance();
		successCal.setTime(successDate);
		int successYear = successCal.get(Calendar.YEAR);// 得到年
		int successMonth = successCal.get(Calendar.MONTH) + 1;// 得到月
		//获取当前时间
		Date currentDate = new Date();
		Calendar date = Calendar.getInstance();
		int currentYear = date.get(Calendar.YEAR);// 得到年
		int currentMonth = date.get(Calendar.MONTH) + 1;// 得到月
		//判断复审时间是否是当日
		String currentMonthFirst = null;
		String currentMonthLast = null;
	/*	if(successYear == currentYear && successMonth == currentMonth){
			//获取当月时间
			currentMonthFirst = currentYear+ "-" + (successMonth+1) + "-01 00:00:00";
			//获取当月最后一天
			Date temp = DateUtil.addDay(DateUtil.addMonth(DateUtil.dateParse(currentMonthFirst), 1), -1);
			currentMonthLast = DateUtil.dateFormat(temp);
			currentMonthLast = currentMonthLast + " 23:59:59";
		}*/
		//获取本期利息
		QueryItem recoverItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tender.getId()));
		if (tender.getRepayType() != 3) {
			if (successYear == currentYear && successMonth == currentMonth) {
				// 获取当月时间
				currentMonthFirst = currentYear + "-" + (successMonth + 1) + "-01 00:00:00";
			} else
				// 获取当月时间
				currentMonthFirst = currentYear + "-" + currentMonth + "-01 00:00:00";
			// 获取当月最后一天
			Date temp = DateUtil.addDay(DateUtil.addMonth(DateUtil.dateParse(currentMonthFirst), 1), -1);
			currentMonthLast = DateUtil.dateFormat(temp);
			currentMonthLast = currentMonthLast + " 23:59:59";

			addAndWhereCondition(whereList, "recover_time", DateUtil.dateParse(currentMonthFirst), DateUtil.dateParse(currentMonthLast));
		}
		recoverItem.setWhere(whereList);
		recoverItem.setOrders("period_no asc");
		List<FnTenderRecover> recoverList = this.getListByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		
		FnTenderRecover currentRecover = recoverList.size() == 0 ? null : recoverList.get(0);
		
		
		//(以防通过工具修改应还款时间)
		if(currentRecover == null){
			QueryItem reItem = new QueryItem();
			reItem.getWhere().add(Where.eq("tender_id", tender.getId()));
			reItem.setOrders("period_no asc");
			List<FnTenderRecover> reList = this.getListByEntity(reItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
			currentRecover = reList.size() == 0 ? null : reList.get(0);
		}
		
		if(DateUtil.getCurrentTime()>currentRecover.getRecoverTime()&&DateUtil.getCurrentTime()>currentRecover.getRecoverYesTime()){
			
			QueryItem reItem = new QueryItem();
			reItem.getWhere().add(Where.eq("tender_id", tender.getId()));
			reItem.getWhere().add(Where.eq("period_no", currentRecover.getPeriodNo()+1));
			List<FnTenderRecover> reList = this.getListByEntity(reItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
			currentRecover = reList.size() == 0 ? null : reList.get(0);
			
		}
		
		
		//获取上一期时间
		QueryItem lastRecoverItem = new QueryItem();
		lastRecoverItem.getWhere().add(Where.eq("tender_id", tender.getId()));
		lastRecoverItem.getWhere().add(Where.eq("period_no", currentRecover == null ? 1 : currentRecover.getPeriodNo() - 1));
		lastRecoverItem.setFields("recover_time");
		FnTenderRecover lastRecover = this.getOneByEntity(lastRecoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		Long lastTime = lastRecover == null ? successTime : lastRecover.getRecoverTime();
		//获取第一期时间
		if(currentRecover.getPeriodNo() == 1){
			lastTime = successTime;
		}
		if(tender.getRepayType() == 6 && currentRecover.getPeriodNo() == 2){
			lastTime = successTime;
		}
		//转让时，当期未还
		//剩余未还本金 + 当期利息*min{成交日期 - 上期还款对应的应还款日期,30} / 30
		BigDecimal amount = BigDecimal.ZERO;
		if(currentRecover.getStatus() == -1){
			int days = DateUtil.daysBetween(DateUtil.dateParse(lastTime), currentDate);
			if(days > 30)days = 30;
			if(days < 0){
				days = Math.abs(days);
				if(days > 30)days = 30;
			}
			amount = NumberUtils.round(waitPrincipal.add(currentRecover.getInterest().multiply(NumberUtils.div(new BigDecimal(days), new BigDecimal(30)))));
			//到期还本还息
			//剩余未还本金 + 当期利息 /(借款期限 *30)*持有天数
			if(tender.getRepayType() == 3 && days > 0){
				days = DateUtil.daysBetween(DateUtil.dateParse(lastTime), currentDate);
				BigDecimal attr  = new BigDecimal(loan.getPeriod() * 30);
//				amount = NumberUtils.round(waitPrincipal.add(NumberUtils.div(currentRecover.getInterest(), attr)));
				amount = NumberUtils.round(waitPrincipal.add(NumberUtils.mul(NumberUtils.div(currentRecover.getInterest(), attr),new BigDecimal(days))));
            }
            return amount;
		}
		//获取当前已还利息和次数
		QueryItem hasRepayItem = new QueryItem();
		hasRepayItem.getWhere().add(Where.eq("tender_id", tender.getId()));
		hasRepayItem.getWhere().add(Where.eq("status", 1));
//		if (DateUtil.getCurrentTime() < currentRecover.getRecoverTime() && DateUtil.getCurrentTime() > currentRecover.getRecoverYesTime()) {
			hasRepayItem.getWhere().add(Where.gt("recover_time", DateUtil.getCurrentTime()));
			hasRepayItem.getWhere().add(Where.lt("recover_yes_time", DateUtil.getCurrentTime()));
	//	}
		hasRepayItem.setFields("sum(interest) interest,count(id) id");
		FnTenderRecover hasRecover = this.getOneByEntity(hasRepayItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		//转让时，当期已还
        //待收本金 - 当期利息*min{成交日期所在期的应还日期 - 成交日期,30} / 30
		int days = DateUtil.daysBetween(currentDate, DateUtil.dateParse(currentRecover.getRecoverTime()))+1;
		if(days > 30)days = 30;
		if(days < 0){
			days = Math.abs(days);
			if(days > 30)days = 30;
		}
        if(currentRecover.getStatus() == 1 && hasRecover.getId() == 1){
            return waitPrincipal.subtract(currentRecover.getInterest().multiply(NumberUtils.div(new BigDecimal(days), new BigDecimal(30))));
        }else{
            //转让时，当期及之后的N期已还，但还未还清
            //待收本金 - 当期利息*min{成交日期所在期的应还日期 - 成交日期,30} / 30 - 之后N期利息总和
            return NumberUtils.sub(waitPrincipal.subtract(currentRecover.getInterest().multiply(NumberUtils.div(new BigDecimal(days), new BigDecimal(30)))),(NumberUtils.sub(hasRecover.getInterest(),currentRecover.getInterest())));
        }
	}
	/**
	 * 已撤销
	 */
	public Object getCancelTransfer(MbMember user,boolean isAll,Integer page) throws Exception{
		QueryItem cancelItem = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
		List<Where> cancelWhere = new ArrayList<Where>();
		cancelWhere.add(new Where("member_id",user.getId()));
		cancelWhere.add(new Where("status",-1));
		cancelWhere.add(new Where("repay_status",-1));	
		cancelWhere.add(Where.expression("(cancel_count is null or cancel_count < 3)", false));	
		cancelItem.setWhere(cancelWhere);
		if(!isAll){
			cancelItem.setPage(page == null ? 1:page);
			cancelItem.setLimit(10);
			Page pageObj = this.baseService.getPage(cancelItem, FnTenderTransfer.class);
			List<FnTenderTransfer> transferList = pageObj.getItems();
	        for (Iterator<FnTenderTransfer> iter = transferList.iterator(); iter.hasNext();) {  
	            FnTenderTransfer transfer = iter.next();
	            FnTender tender = getTender(transfer.getTenderId());
	            FnTenderRecover recover = getRecover(transfer.getTenderId(), tender.getRecoverCountYes() + 1);
	            //判断该标是否逾期未还
				if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
	            	iter.remove();
				}
			}
			return pageObj;
		}else{
		   List<FnTenderTransfer> cancelTransferList = this.baseService.getList(cancelItem, FnTenderTransfer.class);
		  for (Iterator<FnTenderTransfer> iter = cancelTransferList.iterator(); iter.hasNext();) {  
	            FnTenderTransfer transfer = iter.next();
	            FnTender tender = getTender(transfer.getTenderId());
	            FnTenderRecover recover = getRecover(transfer.getTenderId(), tender.getRecoverCountYes() + 1);
	            //判断该标是否逾期未还
				if(recover != null && RepayUtil.lateDays(recover.getRecoverTime(), null) > 0 ){
	            	iter.remove();
				}
			}
			return cancelTransferList;
		}
	}
	/**
	 * 计算购买时的总待收本息
	 * @throws Exception 
	 */
	private BigDecimal getWaitPriInt(Integer period,Long loanId,Long tenderId) throws Exception{
		QueryItem  item = new QueryItem();
		item.getWhere().add(Where.eq("loan_id", loanId));
		item.getWhere().add(Where.eq("tender_id", tenderId));
		item.getWhere().add(new Where("period_no",period,">="));
		item.setFields("sum(principal + interest) amount");
		FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover == null ? BigDecimal.ZERO : recover.getAmount();
	}
}
