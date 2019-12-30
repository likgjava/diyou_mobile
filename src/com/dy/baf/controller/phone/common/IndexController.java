package com.dy.baf.controller.phone.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnBounty;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberReviews;
import com.dy.baf.entity.common.MbMemberReviewsResult;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.common.IndexService;
import com.dy.baf.service.loan.LoanTenderService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Option;
import com.dy.core.entity.Page;
import com.dy.core.utils.NumberUtils;


/**
 * 类名: IndexController<BR>
 * 描述: 首页数据获取
 * 作者: Administrator<BR>
 * 版本信息:<BR>
 * 日期: 2017-7-19 下午8:05:51<BR>
 * 版本: V1.0<BR>
 * modify 2017-7-19 下午8:05:51<BR>
 * copyright 帝友科技.
 */
@Controller(value="appIndexController")
public class IndexController extends AppBaseController {
	
	@Autowired
	private IndexService indexService;
	@Autowired
	private LoanTenderService loanTenderService;	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping("/index/index")
	public DyPhoneResponse index(String xmdy,String diyou){
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap==null?null:paramsMap.get("login_token");
			MbMember member = null;
			if(login_token!=null && !login_token.isEmpty()){
				member = this.getMember(Long.valueOf(login_token));
			}
			
			Map<String,Object> dataMap = new HashMap<String, Object>();
			
			/**
			 * 广告图片
			 */
			List<Map>  bannerList =  this.indexService.phonebanner();
			dataMap.put("banner", bannerList);
			
			/**
			 * 首页新手标
			 */
			List<Map> newHandList = this.indexService.newHandLoan();
			//dataMap.put("newHandLoan", newHandList);
			
			/**
			 * 首页借款标
			 */
			Page page = this.loanTenderService.getTenderNoNewHandList();	//不含新手标
			List<Map> listMap = page.getItems();
			if(listMap !=null && listMap.size() > 0){
				List loanList =(List) dataConvert(listMap,"repay_type_name:getRepayType,category_name:getBorrowType,status_name:getBorrowStatus");
				if(loanList!=null){
					newHandList.add((Map)loanList.get(0));
					newHandList.add(loanList.size()>1?(Map)loanList.get(1):null);
				}
				dataMap.put("loanList", newHandList);
			}
			
			
			
			/**
			 * 收益风险偏好等级
			 */
			Map resultMap = this.getResultData(member);
			dataMap.put("resultMap", resultMap);
			
			/**
			 * 标种借款比例(status大于3)
			 */
			Map loanSum = this.getLoanSum();
			dataMap.put("loanSum", loanSum);
			
			/**
			 * 数据统计
			 */
			List<Map<String,Object>> staticTotal = this.getStaticTotal();
			dataMap.put("staticTotal", staticTotal);
			
				
			/**
			 * 服务电话和时间
			 */			
			QueryItem queryItem = new QueryItem("system", "config");
			queryItem.setFields("id, nid, name, value, status");
			queryItem.getWhere().add(Where.in("nid", "service_tel,service_hours,site_license_number,site_copyright"));
			queryItem.setFields("id, nid, name, value");
			List<SysSystemConfig> configList = (List<SysSystemConfig>) this.getList(queryItem, SysSystemConfig.class);
			if(configList != null && configList.size() > 0){
				for (SysSystemConfig config : configList) {
					if ("service_tel".equals(config.getNid())) {
						dataMap.put("service_tel", config.getValue());
					}
					if ("service_hours".equals(config.getNid())) {
						dataMap.put("service_hours", config.getValue());
					}
					if ("site_license_number".equals(config.getNid())) {
						dataMap.put("site_license_number", config.getValue());
					}
					if ("site_copyright".equals(config.getNid())) {
						dataMap.put("site_copyright", config.getValue());
					}
					
				}
			}
			
			return successJsonResonse(dataMap);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
	/**
	 * 数据统计
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/loan/loantotal")
	public List<Map<String,Object>> getStaticTotal() throws Exception {
		Map<String,Object>resultMap=new HashMap<String, Object>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		//投资收益:投资者实际收到的利息和奖励；等于投资利息+投资奖励（含奖励标、新手专享和红包奖励）
		//赚取收益1.总已实还利息，总提前还款罚息，总逾期罚金（罚息）
		BigDecimal tenderAward =BigDecimal.ZERO;
		QueryItem recoverItem = new QueryItem();
		recoverItem.getWhere().add(Where.eq("status", 1));
		recoverItem.setFields("sum(interest_yes) interest_yes ,sum(advance_interest) advance_interest, sum(late_interest) late_interest");
		FnTenderRecover recover = this.getOneByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		tenderAward = recover==null?tenderAward:NumberUtils.add(tenderAward,recover.getInterestYes(),recover.getAdvanceInterest(),recover.getLateInterest()) ;
		
		//赚取收益 2.新手专享奖励的利息
		QueryItem rewardsitem = new QueryItem();
		rewardsitem.getWhere().add(Where.eq("status", 1));
		rewardsitem.setFields("sum(interest_amount) interest_amount");
		Map<String,Object> rewardMap = this.getOneByMap(rewardsitem, Module.LOAN, Function.LN_NEWREWARDS); 
		tenderAward = tenderAward.add((rewardMap == null ? BigDecimal.ZERO : (BigDecimal)rewardMap.get("interest_amount")));
		
		//赚取收益3.投资奖励金额
		QueryItem tenderItem = new QueryItem();
		tenderItem.setFields("sum(award_amount) sum_award");
		tenderItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> awardMap = this.getOneByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
		BigDecimal awardAmountTotal = new BigDecimal(0);
		if (awardMap != null && awardMap.get("sum_award") != null) {
			awardAmountTotal = new BigDecimal(awardMap.get("sum_award").toString());
		}
		tenderAward = tenderAward.add(awardAmountTotal);
		
		Map<String,Object> map = new HashMap<String, Object>();		
		//投资总额 12亿1062万2189元、 注册用户 25万5253 个、赚取收益 4920万6872元
		//每天虚拟新增：投资总额 332万8526元、 注册用户 681个、赚取收益 13万5896元
//		Date nowDate = DateUtil.getCurrentDate();
//		Date startDate =  DateUtil.dateParse("2017-08-03");	//上线日期
//		int addTenderTotal = 1210622189;	//添加虚拟投资总额
//		BigDecimal addTenderAward = BigDecimal.valueOf(49206872);	//添加虚拟赚取收益
//		int addWebRegNum = 255253;	//添加虚拟注册人数
//		if(nowDate.after(startDate)){
//			int days = DateUtil.daysBetween(startDate, nowDate);
//			addTenderTotal = addTenderTotal + days * 3328526; 
//			addTenderAward = addTenderAward.add(BigDecimal.valueOf(days * 135896)); 
//			addWebRegNum = addWebRegNum+ days * 681; 
//		}		
		//赚取收益 4.红包奖励金额（用户赏金金额）
		QueryItem bountyItem = new QueryItem();
		bountyItem.getWhere().add(Where.eq("status", 2));
		bountyItem.setFields("sum(amount) amount");
		FnBounty bounty = this.getOneByEntity(bountyItem, Module.FINANCE, Function.FN_BOUNTY, FnBounty.class);
		tenderAward = tenderAward.add(bounty == null ? BigDecimal.ZERO : bounty.getAmount());
		
		resultMap.put("tenderAward", tenderAward);
		map = new HashMap<String, Object>();
		map.put("name", "赚取收益(元)");
		map.put("value", NumberUtils.format(tenderAward));
		list.add(map);
		
		//投资总额
		map = new HashMap<String, Object>();
		map.put("name", "投资总额(元)");		
		tenderItem = new QueryItem();
		tenderItem.setFields("sum(amount) amountTotal");
		tenderItem.getWhere().add(Where.eq("status", 1));
		Map tenderMap = this.getOneByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
		if (tenderMap != null && tenderMap.get("amountTotal") != null) {
			Double amountTotal = Double.valueOf(tenderMap.get("amountTotal").toString());
			map.put("value", NumberUtils.format(amountTotal));
		}else{
			map.put("value", "0");
		}
		list.add(map);

		//注册用户
		int webRegNum = 0;
		resultMap.put("web_reg_num", 0);
		QueryItem mbNumItem = new QueryItem();
		mbNumItem.setFields("count(id) totalTender");//count(DISTINCT member_id) totalTender
		mbNumItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> totalTenderMap = this.getOneByMap(mbNumItem, Module.MEMBER, Function.MB_MEMBER);
		if(!CollectionUtils.isEmpty(totalTenderMap)){
			webRegNum = Integer.parseInt(totalTenderMap.get("totalTender").toString());
			resultMap.put("web_reg_num", webRegNum);
		}
		map = new HashMap<String, Object>();
		map.put("name", "注册用户(个)");
		map.put("value", webRegNum);
		list.add(map);
		
		//安全释放金
		map = new HashMap<String, Object>();
		map.put("name", "风险缓释金(元)");
		map.put("value", "0万");
		list.add(map);
		return list;
	}
	
	/**
	 * 方法: getLoanSum
	 * 描述: 标种借款比例(status大于3)
	 * @return
	 * @throws Exception DyResponse
	 */
	@ResponseBody
	@RequestMapping(value="/loan/getLoanSum")
	public Map<String,Object> getLoanSum() throws Exception{
		//借款总额(状态大于3)
		Map<String,Object> resultMap = new HashMap<String, Object>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List nameList=new ArrayList();
		QueryItem queryItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.ge("status", 3));
		queryItem.setFields("sum(amount) amount");
		queryItem.setWhere(whereList);
		Map<String,Object> amountCount = this.getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);
		Double amountDou = Double.valueOf(amountCount.get("amount").toString());
		BigDecimal amountSum = BigDecimal.valueOf(amountDou);	
		List<Option> loanCategoryList = optionUtil.getLoanCategory();
		if(loanCategoryList!=null){
			for (int i = 0; i < loanCategoryList.size(); i++) {
				Option option = loanCategoryList.get(i);
				Map<String,Object> map = new HashMap<String, Object>();
				QueryItem loanQueryItem = new QueryItem();
				List<Where> loanWhereList = new ArrayList<Where>();
				whereList.add(Where.eq("category_id", option.getValue()));
				whereList.add(Where.ge("status", 3));
				queryItem.setFields("sum(amount) amount");
				queryItem.setWhere(whereList);
				Map<String,Object> categoryAmountCount = this.getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);
				nameList.add(option.getText());
				map.put("name", option.getText());
				map.put("value", categoryAmountCount==null?0:categoryAmountCount.get("amount"));
				//map.put(option.getText(), categoryAmountCount==null?0:categoryAmountCount.get("amount"));
				list.add(map);
			}
			
		}
		 NumberFormat format = NumberFormat.getInstance();
		 format.setMinimumFractionDigits(0);
		resultMap.put("list", list);
		resultMap.put("nameList",nameList);
		resultMap.put("amountSum",amountSum);
		resultMap.put("amountSumStr",format.format(amountSum));
		return resultMap;
	}
	
	/**
	 * 个人收益风险偏好等级
	 * @param page
	 * @return
	 * @throws Exception
	 * 
	 */
	public Map getResultData(MbMember member) throws Exception{
		Map<String,Object>resultMap=new HashMap<String,Object>();
		//MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		//user = getMember(user.getId());
		if(member == null){
			resultMap.put("name", "平衡型");
			resultMap.put("scale", "50");
			resultMap.put("earnings", "10");
			return resultMap;
		}
		
		//没测评过
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", member.getId()));
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(null == reviews || reviews.size() == 0){
			resultMap.put("name", "平衡型");
			resultMap.put("scale", "50");
			resultMap.put("earnings", "10");
			return resultMap;
		}
		
		//查询评估结果
		QueryItem queryResult = new QueryItem();
    	queryResult.setFields("id,name");
    	queryResult.setWhere(Where.eq("id", reviews.get(reviews.size()-1).getLevel()));
    	MbMemberReviewsResult result = this.getOneByEntity(queryResult, Module.MEMBER, Function.MB_REVIEWSRESULT, MbMemberReviewsResult.class);
		if("保守型".equals(result.getName())){
			resultMap.put("name", "保守型");
			resultMap.put("scale", "0");
			resultMap.put("earnings", "5");
		 }
		if("稳健型".equals(result.getName())){
			resultMap.put("name", "稳健型");
			resultMap.put("scale", "25");
			resultMap.put("earnings", "7");
		 }
		if("平衡型".equals(result.getName())){
			resultMap.put("name", "平衡型");
			resultMap.put("scale", "50");
			resultMap.put("earnings", "9");
		 }
		if("进取型".equals(result.getName())){
			resultMap.put("name", "进取型");
			resultMap.put("scale", "60");
			resultMap.put("earnings", "11");
		 }
		if("激进型".equals(result.getName())){
			resultMap.put("name", "激进型");
			resultMap.put("scale", "100");
			resultMap.put("earnings", "15");
		 }
		return resultMap;
	}
	
}
