package com.dy.baf.controller.wap.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.FnBounty;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberReviews;
import com.dy.baf.entity.common.MbMemberReviewsResult;
import com.dy.baf.service.common.CommonService;
import com.dy.baf.service.common.IndexService;
import com.dy.baf.service.loan.FnTenderService;
import com.dy.baf.service.loan.LoanTenderService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Option;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.SecurityUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 公共信息
 * @author 波哥
 * @date 2015年9月9日 下午6:52:44 
 * @version V1.0
 */
@Controller(value="wapCommonController")
public class CommonController extends WapBaseController {
	@Autowired
	private CommonService commonService;

	@Autowired
	private IndexService indexService;
	@Autowired
	private LoanTenderService loanTenderService;	
	@Autowired
	private FnTenderService fnTenderService;
	
	/**
	 * 省份城市信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/getProvinceCity")
	public DyPhoneResponse getProvinceCity() {
		try {
			return successJsonResonse(commonService.getProvinceCity());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 省份信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/public/getProvince")
	public DyPhoneResponse getProvince() {
		try {
			return successJsonResonse(commonService.getProvince());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 城市信息
	 * 
	 * @param pid
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/public/getCity")
	public DyPhoneResponse getCity() {
		try {
			String pid = this.getRequest().getParameter("pid") ;
			if ( StringUtils.isBlank(pid)) {
				return errorJsonResonse("请传入省份ID");
			}
			return successJsonResonse(commonService.getCity(pid));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 推广注册
	 * @param model
	 * @param invite
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/common/member/reg")
	public ModelAndView regiserIndex(Model model, String invite) throws Exception {
		if (this.getSessionAttribute(Constant.SESSION_USER) != null) {
			return new ModelAndView("redirect:/wap/member/index");
		}
		this.setSessionAtrribute("invite", new String(SecurityUtil.decode(invite),"UTF-8"));
		return new ModelAndView("redirect:/wap/system/reglogin");
	}
	
	/**
	 * 地区信息
	 * @param cid
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/public/getArea")
	public DyPhoneResponse getArea() {
		try {
			String cid = this.getRequest().getParameter("cid");
			if ( StringUtils.isBlank(cid)) {
				return errorJsonResonse("请传入市ID");
			}
			return successJsonResonse(commonService.getArea(cid));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 利息计算器
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/calculator")
	public DyPhoneResponse calculator(HttpServletRequest request){
		try {
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("amount", request.getParameter("account"));
			paramsMap.put("apr", request.getParameter("lilv"));
			paramsMap.put("period", request.getParameter("period"));
			paramsMap.put("repay_type", request.getParameter("repay_type"));
			return this.commonService.calculator1(paramsMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 收益风险偏好等级
	 * @param page
	 * @return
	 * @throws Exception
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="/risk/levelData",  method=RequestMethod.POST)
	public DyResponse getResultData() throws Exception{
		Map<String,Object>resultMap=new HashMap<String,Object>();
		MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		//user = getMember(user.getId());
		if(user == null){
			resultMap.put("name", "平衡型");
			resultMap.put("scale", "50");
			resultMap.put("earnings", "10");
			return createSuccessJsonResonse(resultMap);
		}
		
		//没测评过
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(null == reviews || reviews.size() == 0){
			resultMap.put("name", "平衡型");
			resultMap.put("scale", "50");
			resultMap.put("earnings", "10");
			return createSuccessJsonResonse(resultMap);
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
		if("成长型".equals(result.getName())){
			resultMap.put("name", "成长型");
			resultMap.put("scale", "60");
			resultMap.put("earnings", "11");
		 }
		if("进取型".equals(result.getName())){
			resultMap.put("name", "成长型");
			resultMap.put("scale", "100");
			resultMap.put("earnings", "15");
		 }
		return this.createSuccessJsonResonse(resultMap);
	}
	/**
	 * 方法: getLoanSum
	 * 描述: 标种借款比例(status大于3)
	 * @return
	 * @throws Exception DyResponse
	 */
	@ResponseBody
	@RequestMapping(value="/loan/getLoanSum")
	public DyResponse getLoanSum() throws Exception{
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
				loanWhereList.add(Where.eq("category_id", option.getValue()));
				loanWhereList.add(Where.ge("status", 3));
				loanQueryItem.setFields("sum(amount) amount");
				loanQueryItem.setWhere(loanWhereList);
				Map<String,Object> categoryAmountCount = this.getOneByMap(loanQueryItem, Module.LOAN, Function.LN_LOAN);
				nameList.add(option.getText());
				map.put("name", option.getText());
				map.put("value", categoryAmountCount==null?0:categoryAmountCount.get("amount"));
				//map.put(option.getText(), categoryAmountCount==null?0:categoryAmountCount.get("amount"));
				list.add(map);
			}
			
		}
		resultMap.put("list", list);
		resultMap.put("nameList",nameList);
		NumberFormat format = NumberFormat.getInstance();
		 format.setMinimumFractionDigits(0);
		resultMap.put("amountSum",format.format(amountSum));
		return createSuccessJsonResonse(resultMap);
	}
	
	/**
	 * 首页资金统计颜色
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/loan/getLoanSumPer",method=RequestMethod.POST)
	public DyResponse tenderFinance() throws Exception{
		String []color={"#317ef3", "#83c4f3","#b2e0ef","#d6f0e0", "#f3d887", "#ff7f2e"} ;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<Option> loanCategoryList = optionUtil.getLoanCategory();	//(客户发标类型不会超过六个)
		if(loanCategoryList!=null){
			for (int i = 0; i < loanCategoryList.size(); i++) {
				Option option = loanCategoryList.get(i);
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("name", option.getText());
				map.put("value", color[i]);
				list.add(map);
			}			
		}
		return createSuccessJsonResonse(list);
	}
	
	/**
	 *首页进行评测按钮
	 * @param model
	 * @return
	 */
	@RequestMapping("/member/risk")
	public ModelAndView riskIndex(Model model) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			//判断用户是否已经登陆过
			if(this.getSessionAttribute(Constant.SESSION_USER) == null)  {
				model.addAttribute("type", "login");
				system.setContentPage("member/login.jsp");
			} else {
				//system.setContentPage("member/main.jsp");
				return new ModelAndView("redirect:/risk/answer/result");
			}
			view = this.initSystemPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 数据统计
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/loan/loantotal")
	public DyResponse getStaticTotal() throws Exception {
		Map<String,Object>resultMap=new HashMap<String, Object>();
		//投资收益:投资者实际收到的利息和奖励；等于投资利息+投资奖励（含奖励标、新手专享和红包奖励）
		//投资利息
		BigDecimal tenderAward =BigDecimal.ZERO;
		QueryItem recoverItem = new QueryItem();
		recoverItem.getWhere().add(Where.eq("status", 1));
		recoverItem.setFields("sum(interest_yes) interest_yes ,sum(advance_interest) advance_interest, sum(late_interest) late_interest");
		FnTenderRecover recover = this.getOneByEntity(recoverItem, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		tenderAward = recover==null?tenderAward:NumberUtils.add(tenderAward,recover.getInterestYes(),recover.getAdvanceInterest(),recover.getLateInterest()) ;
		
		//新手专享奖励
		QueryItem rewardsitem = new QueryItem();
		rewardsitem.getWhere().add(Where.eq("status", 1));
		rewardsitem.setFields("sum(interest_amount) interest_amount");
		Map<String,Object> rewardMap = this.getOneByMap(rewardsitem, Module.LOAN, Function.LN_NEWREWARDS); 
		tenderAward = tenderAward.add((rewardMap == null ? BigDecimal.ZERO : (BigDecimal)rewardMap.get("interest_amount")));
		
		//投资奖励
		QueryItem tenderItem = new QueryItem();
		tenderItem.setFields("sum(award_amount) sum_award");
		tenderItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> awardMap = this.getOneByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
		BigDecimal awardAmountTotal = new BigDecimal(0);
		if (awardMap != null && awardMap.get("sum_award") != null) {
			awardAmountTotal = new BigDecimal(awardMap.get("sum_award").toString());
		}
		tenderAward = tenderAward.add(awardAmountTotal);
		
		//红包奖励
		QueryItem bountyItem = new QueryItem();
		bountyItem.getWhere().add(Where.eq("status", 2));
		bountyItem.setFields("sum(amount) amount");
		FnBounty bounty = this.getOneByEntity(bountyItem, Module.FINANCE, Function.FN_BOUNTY, FnBounty.class);
		tenderAward = tenderAward.add(bounty == null ? BigDecimal.ZERO : bounty.getAmount());
		
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
		
		resultMap.put("tenderAward", tenderAward);

		//注册用户
		resultMap.put("web_reg_num", 0);		
		QueryItem mbNumItem = new QueryItem();
		mbNumItem.setFields("count(id) totalTender");//count(DISTINCT member_id) totalTender
		mbNumItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> totalTenderMap = this.getOneByMap(mbNumItem, Module.MEMBER, Function.MB_MEMBER);
		if(!CollectionUtils.isEmpty(totalTenderMap)){
			resultMap.put("web_reg_num", Integer.parseInt(totalTenderMap.get("totalTender").toString()));
		}
		
		//投资总额
		tenderItem = new QueryItem();
		tenderItem.setFields("sum(amount) amountTotal");
		tenderItem.getWhere().add(Where.eq("status", 1));
		Map<String, Object> tenderMap = this.getOneByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
		if (tenderMap != null && tenderMap.get("amountTotal") != null) {
			Double amountTotal = Double.valueOf(tenderMap.get("amountTotal").toString());
			resultMap.put("tenderTotal", amountTotal);
		}else{
			resultMap.put("tenderTotal",0);
		}
		
		//安全释放金
		resultMap.put("safeTotal","0万");
		return createSuccessJsonResonse(resultMap);
	}
	
	/**
	 * 方法: loanTopThree<BR>
	 * 描述: 首页借款标数据
	 * @return
	 * @throws Exception Map
	 */
	@ResponseBody
	@RequestMapping("/index/loanTopThree")
	public Map loanTopThree() throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
		Page page = this.loanTenderService.getTenderNoNewHandList();	//不含新手标
		List<Map> listMap = page.getItems();
		if(listMap != null && !listMap.isEmpty()){
			List loanList = (List) dataConvert(listMap,
					"repay_type_name:getRepayType,category_name:getBorrowType,status:getBorrowStatus");
			Map<String, String> loanMap = new HashMap<String, String>();
			Map<String, String> loanMapTwo = new HashMap<String, String>();;
			if(loanList!=null && loanList.size()>0){
				loanMap=(Map<String, String>) loanList.get(0);
				if(loanList.size()>1){
					loanMapTwo = loanList==null?null:loanList.size()>=2?(Map<String, String>) loanList.get(1):null;
				}
			}
			
			responseMap.put("loan_one", loanMap);
			responseMap.put("loan_two", loanMapTwo);
		}
		Map previewMap = null;
		//新手标
		List<Map> handLoanList = this.indexService.newHandLoan();
		if(handLoanList!=null && handLoanList.size()>0){
			 previewMap = this.indexService.newHandLoan().get(0);
		}
		
		responseMap.put("new_loan", dataConvert(previewMap, "repay_type_name:getRepayType"));
		return responseMap;
	}
	

	@ResponseBody
	@RequestMapping("/index/siteConfig")
	public Map siteConfig() throws Exception {
		QueryItem queryItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.in("nid", "site_license_number,site_copyright,service_tel,service_hours"));
		queryItem.setWhere(whereList);
		List<Map> configList = this.getListByMap(queryItem, Module.SYSTEM, Function.SYS_CONFIG);
		Map<String,Object> siteList = new HashMap<String, Object>();
		for (Map map : configList) {
			siteList.put(map.get("nid").toString(), map);
		}
		return siteList;
	}
	
}
