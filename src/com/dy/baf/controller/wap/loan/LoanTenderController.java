package com.dy.baf.controller.wap.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.FnAccount;
import com.dy.baf.entity.common.FnLoan;
import com.dy.baf.entity.common.FnLoanAmountCategory;
import com.dy.baf.entity.common.FnLoanCategory;
import com.dy.baf.entity.common.FnLoanInfo;
import com.dy.baf.entity.common.FnLoanRepay;
import com.dy.baf.entity.common.FnLoanRepayPeriod;
import com.dy.baf.entity.common.FnTenderRecover;
import com.dy.baf.entity.common.MbGuaranteeCompany;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbRatingCompany;
import com.dy.baf.entity.common.SysSystemAreas;
import com.dy.baf.entity.common.SysSystemLinkage;
import com.dy.baf.entity.custom.Tender;
import com.dy.baf.entity.custom.TenderCondition;
import com.dy.baf.entity.custom.TenderDetail;
import com.dy.baf.service.content.ContentService;
import com.dy.baf.service.loan.LoanMobileService;
import com.dy.baf.service.loan.LoanTenderService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;
import com.dy.core.utils.serializer.SerializerUtil;

/**
 * 
 * 
 * @Description: 我要投资
 * @author 波哥
 * @date 2015年9月8日 下午6:06:25 
 * @version V1.0
 */
@Controller(value="wapLoanTenderController")
public class LoanTenderController extends WapBaseController {
	
	
	@Autowired
	private LoanTenderService loanTenderService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private LoanMobileService loanService;
	
	/**
	 * 投资列表页面
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loantender")
	public ModelAndView loantender(HttpServletRequest request) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("loan/public/loan.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我要投资列表 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({"rawtypes" })
	@ResponseBody
	@RequestMapping("loan/loantenderdata")
	public Page getTenderIndexList(Integer page,String loan_type,String amount_search,String apr_search,String period_search,String order) throws Exception {
		try {
			
			Page objectPage = this.loanTenderService.getTenderIndexList(page, loan_type, amount_search, apr_search, period_search, order,null);
			
			Page loanPage =(Page) dataConvert(objectPage,"repay_type_name:getRepayType,category_name:getBorrowType");

			return loanPage;
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	
	/**
	 * 获取借款标详情地址页（这里返回wap地址页面）
	 * @return
	 */
	@RequestMapping(value="/loan/loaninfoview", method=RequestMethod.GET)
	public ModelAndView loanInfoView() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("loan/loan/loan_content.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * app专用
	 * 获取借款标详情地址页（这里返回wap地址页面）
	 * @param type
	 * @return
	 */
	@RequestMapping(value="/loan/appinfoview", method=RequestMethod.GET)
	public ModelAndView appLoanInfoView() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("loan/loan/appLoanContent.jsp");
			view = this.initIndexPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	/**
	 * 借款详细信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/loan/loanInfo", method = RequestMethod.POST)
	public Map getLoanInfo(Long id) {
		try {
			//增加浏览次数
			QueryItem loanItem = new QueryItem();
			loanItem.setWhere(Where.eq("loan_id", id));
			FnLoanInfo loanInfo = this.getOneByEntity(loanItem, Module.LOAN, Function.LN_LOANINFO,FnLoanInfo.class);
			loanInfo.setHits(loanInfo.getHits() == null ? 0 : loanInfo.getHits()+1);
			this.updateById(Module.LOAN, Function.LN_LOANINFO, loanInfo);
			//借款信息
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id,member_id,serialno,status,status status_name,member_name,name,category_type,category_id,repay_type,period,apr,amount,credited_amount,progress,overdue_time,vouch_company_id,certificate_file_id,tender_count,apr");
			queryItem.setWhere(Where.eq("id", id));
			Map<String, Object> loanMap = this.getOneByMap(queryItem, Module.LOAN, Function.LN_LOAN);
			loanMap.put("wait_amount", ((BigDecimal) loanMap.get("amount")).subtract((BigDecimal) loanMap.get("credited_amount")));
			Long repayType = Long.valueOf(loanMap.get("repay_type").toString());
			if (repayType == 5) {
				loanMap.put("period_name", loanMap.get("period") + "天");
			} else {
				loanMap.put("period_name", loanMap.get("period") + "个月");
			}
			if("2".equals(loanMap.get("status").toString()) || "3".equals(loanMap.get("status").toString())){
				if(Long.valueOf(loanMap.get("overdue_time").toString()) < DateUtil.getCurrentTime() && !"3".equals(loanMap.get("category_type").toString())){
					loanMap.put("status_name", -2);
					loanMap.put("status", -2);
				}
			}
			
			//根据标种获取协议书
			Integer category_id = MapUtils.getInteger(loanMap, "category_id", -1) ;
			QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);// 借款标类型
			loanCategoryItem.getWhere().add(Where.eq("id", category_id));
			FnLoanCategory cate = this.getOneByEntity(loanCategoryItem, Module.LOAN, Function.LN_CATEGORY, FnLoanCategory.class);
			String agreementNid = null ;
			String agreementTitle = null ;
			String category_name = null;
			if ("2".equals(loanMap.get("category_type").toString())) {
				agreementTitle = "担保标协议";
				agreementNid = "vouch";
				category_name = "担保标";
			} else if ("3".equals(loanMap.get("category_type").toString())) {
				agreementTitle = "流转标协议";
				agreementNid = "roam";
				category_name = "流转标";
			} else {
				if ("5".equals(loanMap.get("repay_type").toString())) {// 天标
					agreementTitle = "天标协议";
					agreementNid = "day";
					category_name = "天标";
				} else {
					if (cate!=null&&cate.getAmountCategoryId() != null && cate.getAmountCategoryId() != -1) {
						QueryItem amountTypeItem = new QueryItem();
						amountTypeItem.getWhere().add(Where.eq("id", cate.getAmountCategoryId()));
						FnLoanAmountCategory amountCategory = this.getOneByEntity(amountTypeItem, Module.FINANCE, Function.FN_LOANAMOUNTTYPE, FnLoanAmountCategory.class);
						if ("credit".equals(amountCategory.getRemark())) {
							agreementTitle = "信用标协议";
							agreementNid = "credit";
							category_name = "信用标";
						}
						if ("vouch".equals(amountCategory.getRemark())) {
							agreementTitle = "担保标协议";
							agreementNid = "vouch";
							category_name = "担保标";
						}
						if ("pawn".equals(amountCategory.getRemark())) {
							agreementTitle = "抵押标协议";
							agreementNid = "pawn";
							category_name = "抵押标";
						}
					} else {
						switch (category_id) {
						case 1:
							// 信用标
							agreementTitle = "信用标协议";
							agreementNid = "credit";
							category_name = "信用标";
							break;
						case 2:
							break;
						case 3:
							// 天标
							agreementTitle = "天标协议";
							agreementNid = "day";
							category_name = "天标";
							break;
						case 4:
							// 担保标
							agreementTitle = "担保标协议";
							agreementNid = "vouch";
							category_name = "担保标";
							break;
						case 11:
							// 抵押标
							agreementTitle = "抵押标协议";
							agreementNid = "pawn";
							category_name = "抵押标";
							break;
						case 9:
							// 流转标
							agreementTitle = "流转标协议";
							agreementNid = "roam";
							category_name = "流转标";
							break;
						}
					}
				}
			}
			loanMap.put("agreementId", cate.getAgreementId()) ;
			loanMap.put("agreementNid", agreementNid) ;
			loanMap.put("agreementTitle", agreementTitle) ;
			loanMap.put("category_name", category_name) ;
			
			/*QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);// 借款标类型
			loanCategoryItem.getWhere().add(Where.eq("id", category_id));
			FnLoanCategory category = this.getOneByEntity(loanCategoryItem, Module.LOAN, Function.LN_CATEGORY, FnLoanCategory.class);
			
			QueryItem ctAgreement = new QueryItem(Module.CONTENT,Function.CT_AGREEMENT);
			ctAgreement.getWhere().add(Where.eq("id", category.getAgreementId()));
			CtAgreement agreement = this.getOneByEntity(ctAgreement, Module.CONTENT, Function.CT_AGREEMENT, CtAgreement.class);
			
			loanMap.put("agreementId", category.getAgreementId()) ;
			loanMap.put("agreementTitle", null==agreement?"":agreement.getTitle()) ;
			loanMap.put("category_name", category.getName()) ;*/
			
			if("3".equals(loanMap.get("category_type").toString())) loanMap.put("category_name", "流转标") ;
				
			BigDecimal totalAmount = new BigDecimal(loanMap.get("amount").toString());
			BigDecimal creditedAmount = new BigDecimal(loanMap.get("credited_amount").toString());
			loanMap.put("left_amount", totalAmount.subtract(creditedAmount));
			
			loanMap.put("overdue_time", DateUtil.dateTimeFormat(loanMap.get("overdue_time")));
			QueryItem liQueryItem = new QueryItem();
			liQueryItem.setFields("use,password,validate,tender_amount_min,tender_amount_max,award_status,award_amount,award_proportion,is_auto,auto_scale,hits,contents,is_company,additional_status,additional_name,additional_apr,additional_amount_max");
			liQueryItem.setWhere(Where.eq("loan_id", id));
			Map<String, Object> loanInfoMap = this.getOneByMap(liQueryItem, Module.LOAN, Function.LN_LOANINFO);
			
			if(loanInfoMap.get("contents") != null){
				loanInfoMap.put("contents", loanInfoMap.get("contents").toString().replaceAll("& lt;", "<").replaceAll("& gt;", ">"));
			}
			//翻译借款标状态
			loanMap = (Map<String, Object>) this.dataConvert(loanMap, "status_name:getBorrowStatus");
			if("复审通过".equals(loanMap.get("status_name").toString()))loanMap.put("status_name", "还款中");
			if("3".equals(String.valueOf(loanMap.get("category_type")))){
				String status=String.valueOf(loanMap.get("status"));
				BigDecimal amount=new BigDecimal(String.valueOf(loanMap.get("amount")));
				
				if("2".equals(status)||"3".equals(status)){		
					if(creditedAmount.compareTo(amount)==0){
						loanMap.put("status_name", "回购中");
					}else{
						loanMap.put("status_name", "流转中");
					}
				}else if("7".equals(status)){
					loanMap.put("status_name", "回购完");
				}
			}
			if("借款到期未满标".equals(loanMap.get("status_name").toString())){
				loanMap.put("status_name","已过期");
			}

			
			loanMap.putAll(loanInfoMap);
			loanMap.put("password_status", loanInfoMap.get("password")==null?false:true);
			
			//还款方式
			QueryItem repayQueryItem = new QueryItem();
			repayQueryItem.setFields("id,name,contents,remark");
			repayQueryItem.setWhere(Where.eq("id", repayType));
			Map<String, Object> repayTypeMap = this.getOneByMap(repayQueryItem, Module.LOAN, Function.LN_REPAYTYPE);
			
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			Map<String, Object> memberMap = new HashMap<String, Object>();
			Map<String, Object> memberInfoMap = new HashMap<String, Object>();
			memberMap.put("is_login", -1);
			memberMap.put("self_loan", -1);
			if(member !=null){
				QueryItem mbQueryItem = new QueryItem();
				mbQueryItem.setWhere(Where.eq("id", member.getId()));
				memberMap = this.getOneByMap(mbQueryItem, Module.MEMBER, Function.MB_MEMBER);
				memberMap.put("is_login", 1);
				if (member.getId().equals(Long.valueOf(loanMap.get("member_id").toString()))) {
					memberMap.put("self_loan", 1);
				}else{
					memberMap.put("self_loan", -1);
				}
				memberMap.put("can_tender_new", 1);
				if (1 == (Integer) loanMap.get("additional_status")) {
					QueryItem tenderItem = new QueryItem();
					tenderItem.setFields("count(-1) row_count");
					tenderItem.getWhere().add(Where.eq("member_id", member.getId()));
					tenderItem.getWhere().add(Where.notEq("loan_id", id));
					tenderItem.getWhere().add(Where.eq("status", 1));
					Map<String, Object> tenderMap = this.getOneByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
					if (null != tenderMap && Integer.valueOf(tenderMap.get("row_count").toString()) > 0) {
						memberMap.put("can_tender_new", -1);
					}
				}
				//资金账户
				QueryItem accountItem = new QueryItem("finance","account");
				accountItem.getWhere().add(Where.eq("member_id", member.getId()));
				FnAccount fnAccount = (FnAccount) this.getOne(accountItem, FnAccount.class);
				memberMap.put("balance_amount", fnAccount.getBalanceAmount());
			}

			//借款人详情
			QueryItem miQueryItem = new QueryItem();
			miQueryItem.setWhere(Where.eq("member_id",loanMap.get("member_id")));
			memberInfoMap = this.getOneByMap(miQueryItem, Module.MEMBER, Function.MB_MEMBERINFO);
			memberInfoMap.put("birthday", memberInfoMap.get("birthday")==null?"未填":DateUtil.dateFormat((Date) memberInfoMap.get("birthday")));
			memberInfoMap.put("graduated", memberInfoMap.get("graduated")==null?"未填":memberInfoMap.get("graduated"));
			memberInfoMap.put("monthly_income_name", getLinkage(String.valueOf(memberInfoMap.get("monthly_income"))));
			String marry_name = "未填";
			//婚姻状况
			if(memberInfoMap.get("marry_status") !=null){
				marry_name = getConfig(memberInfoMap.get("marry_status").toString());
			}
			if(memberInfoMap.get("avatar") != null){
				memberInfoMap.put("avatar",PropertiesUtil.getImageHost() + memberInfoMap.get("avatar"));
			}else{
				memberInfoMap.put("avatar",null);
			}
			String areas = "";
			if(memberInfoMap.get("hometown_province") != null){
				areas = areas + getAreas(Long.valueOf(memberInfoMap.get("hometown_province").toString()));
			}
			if(memberInfoMap.get("hometown_city") != null){
				areas = areas + getAreas(Long.valueOf(memberInfoMap.get("hometown_city").toString()));
			}
			if(memberInfoMap.get("hometown_area") != null){
				areas = areas + getAreas(Long.valueOf(memberInfoMap.get("hometown_area").toString()));
			}
			
			memberInfoMap.put("areas", areas);
			memberInfoMap.put("marry_name", marry_name);
			memberInfoMap.put("edu_name", getLinkage(String.valueOf(memberInfoMap.get("educational_background"))));
			memberInfoMap.put("industry_name", getLinkage(String.valueOf(memberInfoMap.get("company_industry"))));
			memberInfoMap.put("company_scale_name", getLinkage(String.valueOf(memberInfoMap.get("company_scale"))));
			memberInfoMap.put("company_office_name", getLinkage(String.valueOf(memberInfoMap.get("company_office"))));
			String imgPath = PropertiesUtil.getImageHost();
			memberInfoMap.put("imgPath", imgPath);
			//认证信息
			MbMember loanUser = this.getMember(Long.valueOf(loanMap.get("member_id").toString()));
			Map<String,Object> member_approve = new HashMap<String,Object>();
			member_approve.put("is_email", loanUser.getIsEmail()==1?"yes":"no");
			member_approve.put("is_phone", loanUser.getIsPhone()==1?"yes":"no");
			member_approve.put("is_realname", loanUser.getIsRealname()==1?"yes":"no");
			member_approve.put("username", loanUser.getName());
			
			Map<String,Object> member_loan_info = new HashMap<String,Object>();
			member_loan_info.put("loan_count", getLoanCount((String) loanMap.get("member_id"),"all"));//发布借款笔数
			member_loan_info.put("loan_success_count", getLoanCount((String) loanMap.get("member_id"),"success"));//成功借款笔数
			member_loan_info.put("repay_success_count", getRepayCount((String) loanMap.get("member_id"),"repayed"));//还清笔数
			member_loan_info.put("late_repay", getRepayCount((String) loanMap.get("member_id"),"over"));//逾期笔数
			member_loan_info.put("late_repay_max", getRepayCount((String) loanMap.get("member_id"),"deOver"));//严重逾期笔数
			FnLoanRepay repay = getRepayList((String) loanMap.get("member_id"));
			Map<String,Object> map = getData((String) loanMap.get("member_id"));
			BigDecimal late_amount = BigDecimal.ZERO;
			if (repay != null) {
				late_amount = NumberUtils.add(repay.getOverdueFee(), repay.getOverdueInterest());
			}
			member_loan_info.put("loan_success_amount", map.get("loan_success_amount"));//总借入
			member_loan_info.put("wait_repay_total",map.get("wait_repay_total"));//待还
			member_loan_info.put("late_amount", late_amount);//逾期金额
			
			//应还利息
			BigDecimal amount = new BigDecimal((String)loanMap.get("amount"));
			BigDecimal apr = new BigDecimal((String)loanMap.get("apr"));
			Integer period = Integer.valueOf((String) loanMap.get("period"));
			Integer reType = Integer.valueOf((String) loanMap.get("repay_type"));
			TenderCondition repayCondtion = new TenderCondition();
			repayCondtion.setAmount(amount);
			repayCondtion.setApr(apr);
			repayCondtion.setCurrentTime(DateUtil.getCurrentTime());
			repayCondtion.setPeriod(period);
			repayCondtion.setRepayType(reType);
			Tender tender = RepayUtil.getRepayInfo(repayCondtion);
			List<TenderDetail> tenderDetail = tender.getRepayDetailList();
			if(reType == 1){//等额本息
				member_loan_info.put("interestTotal", tenderDetail.get(0).getAmount());
			}else if(reType == 2){//按季还款
				member_loan_info.put("interestTotal", tenderDetail.get(0).getInterest());
			}else if(reType == 3){//到期还本还息
				member_loan_info.put("interestTotal", tender.getAmountAll());
			}else if(reType == 4){//按月付息
				member_loan_info.put("interestTotal", tenderDetail.get(0).getInterest());
			}else if(reType == 5){//按天计息到期还本息
				member_loan_info.put("interestTotal", tender.getAmountAll());
			}else if(reType == 6){//先息后本
				member_loan_info.put("interestTotal", tender.getInterestAll());
			}
			
			//获取担保公司
			MbGuaranteeCompany bondingCompany = null;
			if(loanMap.get("vouch_company_id").toString() != null && !"0".equals(loanMap.get("vouch_company_id").toString())){
				QueryItem companyItem = new QueryItem();
				companyItem.getWhere().add(Where.eq("id", loanMap.get("vouch_company_id").toString()));
				companyItem.setFields("name");
				bondingCompany = this.getOneByEntity(companyItem, Module.MEMBER, Function.MB_VOUCHCOMPANY, MbGuaranteeCompany.class);
				if(bondingCompany != null){
					loanMap.put("vouch_company_info", bondingCompany.getName());
				}
			}
			String iscompany = "-1";//判断是企业发标还是个人发标
			MbRatingCompany company = null;
			Map<String,Object> companyMap = new HashMap<String,Object>();
			//获取图片服务器地址
			String imageHost = PropertiesUtil.getImageHost();
			List<Map<String, Object>> companyList = new ArrayList<Map<String, Object>>();
			if("1".equals(loanInfoMap.get("is_company").toString())){
				iscompany = "1";
				//获取企业信息
				QueryItem companyItem = new QueryItem();
				companyItem.getWhere().add(Where.eq("member_id", loanMap.get("member_id")));
				company = this.getOneByEntity(companyItem, Module.MEMBER, Function.MB_RATINGCOMPANY, MbRatingCompany.class);
				if(company != null){
					String companyPic = company.getEnterpriseMaterial();
					if(companyPic != null){
						SerializerUtil serializerUtil = new SerializerUtil();
						List<Map<String, Object>> configList = (List<Map<String, Object>>) serializerUtil.unserialize(companyPic.getBytes());
						if (configList != null) {
								for (int i = 0; i < configList.size(); i++) {
									if (configList.get(i) != null) {
										Map<String, Object> urlMap = new HashMap<String, Object>();
										urlMap.put("title", configList.get(i).get("title"));
										urlMap.put("imgurl", imageHost + configList.get(i).get("imgurl"));
										urlMap.put("minimg", configList.get(i).get("minimg"));
										companyList.add(urlMap);
									}
								}
						}
					}
					companyMap.put("name", company.getName() == null ? "未填写": company.getName());
					companyMap.put("account", company.getAccount() == null ? "未填写": company.getAccount()+"万元");
					companyMap.put("establishment_date", company.getEstablishmentDate() == null ? "未填写": DateUtil.dateFormat(company.getEstablishmentDate()));
//					companyMap.put("province", company.getProvince() == null ? "未填写": getAreas(company.getProvince()));
//					companyMap.put("city", company.getCity() == null ? "未填写": getAreas(company.getCity()));
					companyMap.put("place", company.getProvince() == null ? "未填写": getAreas(company.getProvince()) +" " +getAreas(company.getCity()));
					companyMap.put("address", company.getAddress() == null ? "未填写": company.getAddress());
					companyMap.put("company_intro", company.getCompanyIntro() == null ? "未填写": company.getCompanyIntro());
					companyMap.put("collateral", company.getCollateral() == null ? "未填写": company.getCollateral());
				}else{
					companyMap.put("name","未填写");
					companyMap.put("account","未填写");
					companyMap.put("establishment_date","未填写");
					companyMap.put("place","未填写");
					companyMap.put("address", "未填写");
					companyMap.put("company_intro", "未填写");
					companyMap.put("collateral", "未填写");
				}
			}
			//项目材料图片
			String attachment_ids = loanInfo.getAttachmentIds();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if(attachment_ids != null){
				SerializerUtil serializerUtil = new SerializerUtil();
				Object obj = serializerUtil.unserialize(attachment_ids.getBytes());
				if (!(obj instanceof Map)) {
					List<Map<String, Object>> configList = obj == null ? null : (List<Map<String, Object>>) obj;
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
				}
			}
			
			
			//投资列表
			QueryItem tenderItem = new QueryItem();
			tenderItem.getWhere().add(Where.eq("loan_id", id));
			tenderItem.setFields("member_name,amount,add_time,id");
			List<Map> tenderList = this.getListByMap(tenderItem, Module.LOAN, Function.LN_TENDER);
			BigDecimal amount_all = new BigDecimal(0);//投资总额
			
			if(tenderList != null && tenderList.size() > 0){
				for (Map tenderMap : tenderList) {
					amount_all = amount_all.add((BigDecimal) tenderMap.get("amount"));
					
					if(StringUtils.is_chinese(tenderMap.get("member_name").toString())){
						tenderMap.put("member_name", ((String) tenderMap.get("member_name")).substring(0,1)+"**");
					}else{
						tenderMap.put("member_name", ((String) tenderMap.get("member_name")).substring(0,2)+"**");
					}
					
				}
				loanMap.put("next_repay_time", getRecoverTime(Long.valueOf(tenderList.get(0).get("id").toString()), Integer.valueOf(loanMap.get("period").toString())));

			}
			QueryItem repayItem = new QueryItem();
			repayItem.getWhere().add(Where.eq("loan_id", id));
			repayItem.setFields("amount_total total,expire_time repay_date,status repay_type_name,next_repay_time,period_yes");
			repayItem.setOrders("period desc");
			List<Map> repayList = this.getListByMap(repayItem, Module.LOAN,Function.LN_REPAY);
			if(repayList != null && repayList.size() > 0){
				for (Map repayMap : repayList) {
					/*repayMap.put("type_name", "本+息");
					repayMap.put("repay_date", DateUtil.dateFormat(repayMap.get("repay_date")));
					repayMap.put("repay_type_name", "1".equals(repayMap.get("status"))?"已还完":"未还款");*/
					loanMap.put("period_yes", repayMap.get("period_yes"));
				}
			}
			
			repayItem = new QueryItem();
			repayItem.getWhere().add(Where.eq("loan_id", id));
			List<FnLoanRepayPeriod> repayListPeriod = this.getListByEntity(repayItem, Module.LOAN, Function.LN_REPAYPERIOD, FnLoanRepayPeriod.class);
			List<Map<String, Object>> newRepayListPeriod = new ArrayList<Map<String,Object>>();
			for(FnLoanRepayPeriod repayPeriod:repayListPeriod){
				Map<String,Object> newMap = new HashMap<String,Object>();
				newMap.put("type_name", "本+息");
				newMap.put("repay_date", DateUtil.dateFormat(repayPeriod.getRepayTime()));
				newMap.put("repay_type_name", 1==repayPeriod.getStatus() ?"已还完":"未还款");
				//提前还款不显示利息
				if(repayPeriod.getRepayType() == 3){
					newMap.put("total", repayPeriod.getPrincipal());
				}else{
					newMap.put("total", repayPeriod.getAmount());
				}				newMap.put("period_yes", loanMap.get("period_yes"));
				newRepayListPeriod.add(newMap);
			}
			
			Map roamMap = null;
			//查询流转标信息
			if("3".equals(String.valueOf(loanMap.get("category_type")))){
				QueryItem loanRoamItem = new QueryItem(Module.LOAN, Function.LN_ROAM);//借款标类型
				loanRoamItem.setFields("amount as tend_roam_min,portion_total,portion_yes");
				loanRoamItem.setWhere(Where.eq("loan_id", loanMap.get("id")));
				roamMap = this.getOneByMap(loanRoamItem, Module.LOAN, Function.LN_ROAM);
				loanMap.putAll(roamMap);
			}	
			
			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put("iscompany", iscompany);
			valueMap.put("company_info", companyMap);
			valueMap.put("loan_info", loanMap);
			valueMap.put("member", memberMap);
			valueMap.put("member_info", memberInfoMap);
			valueMap.put("repay_type", repayTypeMap);
			valueMap.put("member_approve", member_approve);
			valueMap.put("member_loan_info", member_loan_info);
			valueMap.put("attachment_ids", list.size() == 0 ? "" : list);
			valueMap.put("companyPic", companyList.size() == 0 ? "" : companyList);
			valueMap.put("tender_list", tenderList);
			valueMap.put("repay_plan", newRepayListPeriod);
			
			return valueMap;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 债权转让详情页面数据
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/transfer/transferInfo",method=RequestMethod.POST)
	public Map transferInfo(String id){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			QueryItem transferItem  = new QueryItem(Module.LOAN,Function.LN_TRANSFER);
			transferItem.getWhere().add(Where.eq("id", id));
			transferItem.setFields("tender_id,amount_money,member_id,loan_member_id,amount,loan_id,wait_principal,wait_interest,total_period,status,id,period");
			Map<String,Object> transferMap = (Map<String, Object>) this.getOne(transferItem, Map.class);
			
			
			if(!"2".equals(transferMap.get("status").toString())){
				BigDecimal wait_recover_principal = BigDecimal.ZERO;//待收本金
				BigDecimal wait_recover_interest = BigDecimal.ZERO;//待收利息
				FnTenderRecover recover = getRecoverMoney(transferMap.get("tender_id").toString());
				if(recover != null){
					wait_recover_principal = NumberUtils.sub(recover.getPrincipal(), recover.getPrincipalYes());
					wait_recover_interest = NumberUtils.sub(recover.getInterest(), recover.getInterestYes());
				}
				transferMap.put("wait_principal", wait_recover_principal);
				transferMap.put("wait_interest", wait_recover_interest);
				BigDecimal amount = new BigDecimal(transferMap.get("amount").toString());
				BigDecimal waitPrincipal = new BigDecimal(transferMap.get("wait_principal").toString());
				BigDecimal waitMoney = waitPrincipal.add(new BigDecimal(transferMap.get("wait_interest").toString())) ;
				transferMap.put("income", waitMoney.subtract(amount));}
			else {
				
				QueryItem item = new QueryItem();
				List<Where> whereList = new ArrayList<Where>();
				whereList.add(Where.eq("tender_id", transferMap.get("tender_id").toString()));
				whereList.add(Where.ge("period_no", transferMap.get("period").toString()));
				item.setFields("sum(principal) principal,sum(interest) interest");
				item.setWhere(whereList);
				FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
				
				transferMap.put("wait_principal",recover.getPrincipal());
				transferMap.put("wait_interest", recover.getInterest());
				BigDecimal amount = new BigDecimal(transferMap.get("amount").toString());
				BigDecimal waitPrincipal = new BigDecimal(transferMap.get("wait_principal").toString());
				BigDecimal waitMoney = waitPrincipal.add(new BigDecimal(transferMap.get("wait_interest").toString())) ;
			}
			
			
			BigDecimal amount_money = new BigDecimal(transferMap.get("amount_money").toString());
			BigDecimal amount = new BigDecimal(transferMap.get("amount").toString());
			BigDecimal waitPrincipal = new BigDecimal(transferMap.get("wait_principal").toString());
			BigDecimal waitMoney = waitPrincipal.add(new BigDecimal(transferMap.get("wait_interest").toString())) ;
			transferMap.put("income", waitMoney.subtract(amount));

			Map<String,Object> loanMap = this.getLoanInfo(Long.valueOf(transferMap.get("loan_id").toString()));
			
			loanMap.put("transfer_ret", transferMap);
			if(member!=null && member.getId().equals(Long.valueOf(transferMap.get("loan_member_id").toString()))){
				loanMap.put("is_self", 1);
			}
			if(member!=null && member.getId().equals(Long.valueOf(transferMap.get("member_id").toString()))){
				loanMap.put("is_self", 2);
			}
			return loanMap;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	
	/**
	 * 担保协议页面
	 * @return
	 */
	@RequestMapping(value="/loan/loanProtocolAgreement", method=RequestMethod.GET)
	public ModelAndView loanProtocol(Long loanId) {
		ModelAndView view = new ModelAndView("loan/loan/loanProtocol");
		try {
			
			String agreementId = RequestUtil.getString(this.getRequest(), "agreementId", "") ;
			QueryItem ctAgreement = new QueryItem(Module.CONTENT,Function.CT_AGREEMENT);
			ctAgreement.getWhere().add(Where.eq("id", Integer.parseInt(agreementId)));
			CtAgreement agreement = this.getOneByEntity(ctAgreement, Module.CONTENT, Function.CT_AGREEMENT, CtAgreement.class);
			view.addObject("agreement", agreement);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 担保协议页面
	 * @return
	 */
	@RequestMapping(value="/loan/loanProtocol", method=RequestMethod.GET)
	public ModelAndView loanProtocolOld(Long loanId) {
		ModelAndView view = new ModelAndView("loan/loan/loanProtocol");
		try {
			String type = RequestUtil.getString(this.getRequest(), "type", "") ;
			CtAgreement agreement = this.contentService.agreement(type);
			view.addObject("agreement", agreement);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 投资合同范本
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/loan/loanContract", method=RequestMethod.GET)
	public ModelAndView loanContract(Long id) {
		ModelAndView view = new ModelAndView("loan/loan/loanContract");
		try {
			String type = RequestUtil.getString(this.getRequest(), "type", "") ;
			CtAgreement agreement = this.contentService.agreement(StringUtils.isBlank(type)?"tender":type);
			view.addObject("agreement", agreement);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我要投资获取数据
	 */
	@ResponseBody
	@RequestMapping("tender/investData")
	public DyPhoneResponse investdata(String id) {
		try {
			return this.loanTenderService.investdata(id,null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 计算预期收益
	 */
	@ResponseBody
	@RequestMapping("tender/investInterest")
	public DyPhoneResponse investInterest(BigDecimal amount,Integer period,BigDecimal apr,Integer repay_type,Double award_scale,Integer additional_status, BigDecimal additional_apr, String loan_id) {
		try {
			return this.loanTenderService.investInterest(amount, period, apr, repay_type, award_scale, additional_status, additional_apr, this.getMemberId().toString(), loan_id);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	/**
	 * 投资提交
	 */
	@ResponseBody
	@RequestMapping("tender/tender")
	public DyPhoneResponse invest(Integer login_token,Integer id,BigDecimal amount,String paypassword,String password,String depositCertificate,String redbag) throws Exception {
		try {
			return this.loanTenderService.invest(login_token, id, amount, paypassword, password, depositCertificate, redbag);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/*
	 * 获取还款方式列表
	 */
	@ResponseBody
	@RequestMapping("/loan/getRepayTypeList")
	public DyPhoneResponse getRepayTypeList() {
		try {
			return this.loanService.getRepayTypeList();
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 获取联动值
	 * @throws Exception 
	 */
	private String getConfig(String id) throws Exception{
		//获取联动配置的值
		QueryItem linkageItem = new QueryItem();
		linkageItem.setFields("name");
		linkageItem.getWhere().add(Where.eq("id", id));
		SysSystemLinkage linkage = this.getOneByEntity(linkageItem,"system","linkage", SysSystemLinkage.class);
		return linkage == null?"":linkage.getName();
		
	}
	
	/**
	 * 根据linkpage_id插叙linkage
	 * @throws Exception 
	 */
	private String getLinkage(String lid) throws Exception{
		if(lid == null){
			return "未填";
		}
		QueryItem linkItem = new QueryItem();
		linkItem.setFields("name");
		linkItem.getWhere().add(Where.eq("id", lid));
		SysSystemLinkage linkage = this.getOneByEntity(linkItem, Module.SYSTEM, Function.SYS_LINKAGE, SysSystemLinkage.class);
		return linkage==null?"未填":linkage.getName();
		
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
	 * 获取地区
	 * @throws Exception 
	 */
	private String getAreas(Long id) throws Exception{
		//获取联动配置的值
		QueryItem areasItem = new QueryItem();
		areasItem.setFields("name");
		areasItem.getWhere().add(Where.eq("id", id));
		SysSystemAreas areas = this.getOneByEntity(areasItem,Module.SYSTEM,Function.SYS_AREAS,SysSystemAreas.class);
		return areas == null?"":areas.getName();
		
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
		Map<String, Object> valueMap = (Map<String, Object>) this.getOne(queryItem);
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
	
	
	/**
	 * 根据tenderid查询最近还款时间
	 * @throws DyServiceException 
	 */
	private String getRecoverTime(Long tenderId,Integer period) throws Exception{
		QueryItem item = new QueryItem(Module.LOAN,Function.LN_RECOVER);
		item.getWhere().add(Where.eq("tender_id", tenderId));
		item.getWhere().add(Where.eq("status",-1));
		item.setOrders(" recover_time asc");
		List<FnTenderRecover> recoverList = (List<FnTenderRecover>) this.getList(item, FnTenderRecover.class);
		String recoverTime = null;
		if(recoverList != null && recoverList.size() > 0){
			return recoverList.get(0).getRecoverTime().toString() ;
//			recoverTime = DateUtil.dateFormat(recoverList.get(0).getRecoverTime());
		}
		return recoverTime;
	}
	
	/**
	 * 根据tenderId获取待收本金，待收利息，待收总额
	 */
	private FnTenderRecover getRecoverMoney(String tenderId) throws Exception{
		QueryItem item = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("tender_id", tenderId));
		whereList.add(Where.eq("status", -1));
		item.setFields("sum(amount) amount,sum(amount_yes) amount_yes,sum(interest) interest,sum(interest_yes) interest_yes,sum(principal) principal ,sum(principal_yes) principal_yes");
		item.setWhere(whereList);
		FnTenderRecover recover = this.getOneByEntity(item, Module.LOAN, Function.LN_RECOVER, FnTenderRecover.class);
		return recover;
	} 
}
