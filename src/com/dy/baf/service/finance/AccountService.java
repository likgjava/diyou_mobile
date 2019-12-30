package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnAccountLog;
import com.dy.baf.entity.common.FnAccountRecharge;
import com.dy.baf.entity.common.FnAccountWithdraw;
import com.dy.baf.entity.common.FnTender;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;
/**
 * 
 * @Description: 用户资金
 * @author 波哥
 * @date 2015年9月14日 下午2:56:52 
 * @version V1.0
 */
@Service("mobileAccountService")
public class AccountService extends  MobileService {
	
	@Autowired
	private BaseService baseService;

	
	/**
	 * 用户资金详情
	 * @param login_token
	 * @return
	 */
	public DyPhoneResponse memberAccount(String memberId) throws Exception{
		
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("用户登录标识不能为空");
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		//资金账户
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		FnAccount fnAccount = (FnAccount) this.baseService.getOne(accountItem, FnAccount.class);
		//充值总额
		QueryItem rechargeItem = new QueryItem("finance", "recharge");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id", user.getId()));
		whereList.add(new Where("status", 1));
		rechargeItem.setWhere(whereList);
		List<FnAccountRecharge> rechargeList = (List<FnAccountRecharge>) this.baseService.getList(rechargeItem,
				FnAccountRecharge.class);
		BigDecimal amountTotal = new BigDecimal(0);
		for (FnAccountRecharge recharge : rechargeList) {
			amountTotal = amountTotal.add(recharge.getAmount());
		}
		//提现总额
		QueryItem cashItem = new QueryItem(Module.FINANCE,Function.FN_WITHDRAW);
		cashItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<FnAccountWithdraw> cashList = (List<FnAccountWithdraw>) this.baseService.getList(cashItem,
				FnAccountWithdraw.class);
		BigDecimal cashAccount = new BigDecimal(0);
		for (FnAccountWithdraw withdraw : cashList) {
			cashAccount = cashAccount.add(withdraw.getAmount());
		}
		Map account = this.centertenderdata(memberId) ;
		Map<String, Object> map = new HashMap<String, Object>();
		
		
		map.put("balance_amount", fnAccount.getBalanceAmount());
		map.put("expend_amount", fnAccount.getExpendAmount());
		map.put("freeze_amount", fnAccount.getFreezeAmount());
		map.put("income_amount", fnAccount.getIncomeAmount());
		map.put("recharge_total", amountTotal);
		map.put("total_amount", fnAccount.getTotalAmount());
		//未转让的已收利息
		QueryItem recoverItem = new QueryItem();
		recoverItem.getWhere().add(Where.eq("tender_member_id", user.getId()));
		recoverItem.getWhere().add(Where.eq("transfer_member_id", 0));
		recoverItem.setFields("sum(interest_yes) interest_yes");
		FnTenderRecover recover = this.getOneByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		
		//已转让的已收利息
		QueryItem reItem = new QueryItem();
		reItem.getWhere().add(Where.notEq("tender_member_id", user.getId()));
		reItem.getWhere().add(Where.eq("transfer_member_id", user.getId()));
		reItem.setFields("sum(interest_yes) interest_yes");
		FnTenderRecover tranRecover = this.getOneByEntity(reItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		BigDecimal tranInterestYes = BigDecimal.ZERO;
		if(null != tranRecover){
			tranInterestYes = tranRecover.getInterestYes();
		}
		
		
		//新手标利息
		BigDecimal newIncome = BigDecimal.ZERO;
		QueryItem newItem = new QueryItem();
		newItem.getWhere().add(Where.eq("fee_id", 322));
		newItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<FnAccountLog> accounts = this.getListByEntity(newItem, Module.FINANCE, Function.FN_ACCOUNTLOG, FnAccountLog.class);
		for(FnAccountLog fnAccountLog : accounts){
			newIncome = newIncome.add(NumberUtils.sub(fnAccountLog.getIncome(),fnAccountLog.getPreIncome()));
		}

		//投资表,查询奖励
		QueryItem tenderItem = new QueryItem();
		List<Where> where = new ArrayList<Where>();
		where.add(new Where("member_id",user.getId()));
		where.add(new Where("status",1));
		tenderItem.setWhere(where);
		tenderItem.setFields("sum(award_amount) award_amount");
		FnTender tender =this.getOneByEntity(tenderItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
		
		BigDecimal interestAward = (tender != null ? tender.getAwardAmount() : BigDecimal.ZERO);
		BigDecimal interestYesTotal = (recover != null ? recover.getInterestYes() : BigDecimal.ZERO);
		map.put("interest_yes_total", NumberUtils.add(newIncome, interestYesTotal, tranInterestYes)) ;
		map.put("award_total", interestAward) ;
		map.put("wait_repay_amount", fnAccount.getWaitRepayAmount());
		map.put("withdraw_total", cashAccount);
		map.put("role", user.getRole());
		map.putAll(account) ;
		
		return successJsonResonse(map);
	}
	
	
	/**
	 * 用户中心资金
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse toMemberCenter(String memberId) throws Exception{
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("用户登陆标识不能为空");
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		if(user == null){
			return errorJsonResonse("用户不存在");
		}
		//资金账户
		QueryItem accountItem = new QueryItem(Module.FINANCE,Function.FN_ACCOUNT);
		accountItem.getWhere().add(Where.eq("member_id", user.getId()));
		Map<String,Object> fnAccount = this.baseService.getOne(accountItem);
		
		Map tenderMap = this.centertenderdata(memberId);
		fnAccount.putAll(tenderMap);
		
		//用户信息
		QueryItem infoItem = new QueryItem();
		infoItem.getWhere().add(Where.eq("member_id", memberId));
		MbMemberInfo memberInfo = (MbMemberInfo) this.getOne("member", "memberinfo", infoItem, MbMemberInfo.class);
		
		//图片地址头部
		String imgPath = PropertiesUtil.getImageHost();
		fnAccount.put("imgPath", imgPath);
		fnAccount.put("avatarImage", imgPath+memberInfo.getAvatar());
		fnAccount.put("memberInfo", memberInfo);
		fnAccount.put("phone", user.getPhone());

		/**
		 * 站内信消息
		 */
	    QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_MESSAGE);
	    queryItem.setFields("count(1) msgnum");
	    List<Where> whereList = new ArrayList<Where>();
	    whereList.add(Where.eq("status", 1));
	    whereList.add(Where.eq("member_id", user.getId()));
	    queryItem.setWhere(whereList);
	    Map<String, Object> map = this.baseService.getOne(queryItem);
	    fnAccount.put("count", Integer.parseInt(map.get("msgnum").toString()));
		return successJsonResonse(fnAccount);
		
	}
	
	/**
	 * 获取总累计收益，总代收本金，总代收利息
	 * @throws Exception 
	 */
	public Map centertenderdata(String memberId) throws Exception {
		
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		BigDecimal interest_award = BigDecimal.ZERO;//总累计收益
		BigDecimal principal_wait_total = BigDecimal.ZERO;//总代收本金
		BigDecimal interest_wait_total = BigDecimal.ZERO;//总代收利息
		//投资回款表
		QueryItem recoverItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("tender_member_id",user.getId()));
		whereList.add(new Where("transfer_member_id",0));
		recoverItem.setWhere(whereList);
		recoverItem.setFields("sum(interest_yes) interest_yes,sum(principal) principal,sum(principal_yes) principal_yes,sum(interest) interest,sum(interest_yes) interest_yes");
		FnTenderRecover recover = this.getOneByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		
		QueryItem reItem = new QueryItem();
		List<Where> rewhere = new ArrayList<Where>();
		rewhere.add(new Where("tender_member_id",user.getId()));
		rewhere.add(new Where("transfer_member_id",0));
		rewhere.add(new Where("status",-1));
		reItem.setWhere(rewhere);
		reItem.setFields("sum(principal) principal,sum(principal_yes) principal_yes,sum(interest) interest,sum(interest_yes) interest_yes");
		FnTenderRecover re = this.getOneByEntity(reItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		if(recover != null){
			interest_award = recover.getInterestYes();
			if(re != null){
				principal_wait_total = NumberUtils.sub(re.getPrincipal(), re.getPrincipalYes());
				interest_wait_total = NumberUtils.sub(re.getInterest(), re.getInterestYes());
			}
		}
		//债权回款表
		QueryItem wardItem = new QueryItem();
		List<Where> wardwhere = new ArrayList<Where>();
		wardwhere.add(new Where("transfer_member_id",user.getId()));
		wardItem.setWhere(wardwhere);
		wardItem.setFields("sum(interest_yes) interest_yes");
		FnTenderRecover wardRecover = this.getOneByEntity(wardItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
				
		QueryItem transferItem = new QueryItem();
		List<Where> tranwhere = new ArrayList<Where>();
		tranwhere.add(new Where("transfer_member_id",user.getId()));
		tranwhere.add(new Where("status",-1));
		transferItem.setWhere(tranwhere);
		transferItem.setFields("sum(principal) principal,sum(principal_yes) principal_yes,sum(interest) interest,sum(interest_yes) interest_yes");
		FnTenderRecover transferRecover = this.getOneByEntity(transferItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		if(wardRecover != null){
			interest_award = interest_award.add(wardRecover.getInterestYes());
		}
		if(transferRecover != null){
			principal_wait_total = principal_wait_total.add(NumberUtils.sub(transferRecover.getPrincipal(), transferRecover.getPrincipalYes()));
			interest_wait_total = interest_wait_total.add(NumberUtils.sub(transferRecover.getInterest(), transferRecover.getInterestYes()));
		}
		//投资表,查询奖励
		QueryItem tenderItem = new QueryItem();
		List<Where> where = new ArrayList<Where>();
		//where.add(new Where("transfer_status",1,"!="));
		where.add(new Where("member_id",user.getId()));
		where.add(new Where("status",1));
		tenderItem.setWhere(where);
		tenderItem.setFields("sum(award_amount) award_amount");
		FnTender tender =this.getOneByEntity(tenderItem, Module.LOAN, Function.LN_TENDER, FnTender.class);
		if(tender != null){
			interest_award = interest_award.add(tender.getAwardAmount());
		}
		
		//新手标利息
		BigDecimal newIncome = BigDecimal.ZERO;
		QueryItem newItem = new QueryItem();
		newItem.getWhere().add(Where.eq("fee_id", 322));
		newItem.getWhere().add(Where.eq("member_id", user.getId()));
		List<FnAccountLog> accounts = this.getListByEntity(newItem, Module.FINANCE, Function.FN_ACCOUNTLOG, FnAccountLog.class);
		for(FnAccountLog fnAccountLog : accounts){
			newIncome = newIncome.add(NumberUtils.sub(fnAccountLog.getIncome(),fnAccountLog.getPreIncome()));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("interest_wait_total", interest_wait_total);
		map.put("principal_wait_total", principal_wait_total);
		map.put("interest_award", NumberUtils.add(interest_award, newIncome));
		return map;
	}

}
