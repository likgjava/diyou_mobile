package com.dy.baf.controller.wap.member;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberBank;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.finance.AccountService;
import com.dy.baf.service.loan.FnTenderService;
import com.dy.baf.service.member.ApproveRealnameService;
import com.dy.baf.service.member.FrontMemberService;
import com.dy.baf.service.system.CommonService;
import com.dy.baf.service.trust.TrustRequestService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.trust.TrustManager;
import com.dy.core.trust.entity.BankMap;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.DyHttpClient;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 用户中心
 * @author 波哥
 * @date 2015年9月15日 下午4:08:21 
 * @version V1.0
 */
@Controller(value="wapMemberCenterController")
public class MemberCenterController extends WapBaseController {
	@Autowired
	private CommonService commonService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private FrontMemberService frontMemberService ;
	@Autowired
	private ApproveRealnameService approveRealnameService ;
	@Autowired
	private FnTenderService fnTenderService;
	
	@Autowired
	private TrustRequestService trustRequestService;
	
	/**
	 * 个人中心
	 * @param request
	 * @return
	 */
	@RequestMapping("/member/index")
	public ModelAndView index(Model model) {
		ModelAndView view = new ModelAndView();
		try {
			MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			MbMember newuser = getMember(user.getId());
			SystemInfo system = new SystemInfo();
			//判断用户是否已经登陆过
			system.setContentPage("member/index.jsp");
			view = this.initCommonPageView(system);
			if(isTrust) {
				view.addObject("isTrustAccountOpen",newuser.getTrustAccount()==null||"".equals(newuser.getTrustAccount()) ? 0 : 1);
				view.addObject("groupStatus", TrustManager.CHINAPNR.equals(trustType) ? user.getGroupStatus() : "");
			}		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 个人中心账号数据
	 */
	@ResponseBody
	@RequestMapping("/member/accountData")
	public DyPhoneResponse accountData(){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
			return this.accountService.toMemberCenter(member.getId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 资金详情页面
	 * @return
	 */
	@RequestMapping(value="/member/account",method=RequestMethod.GET)
	public ModelAndView account(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/account.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 资金详情页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/account",method=RequestMethod.POST)
	public DyPhoneResponse account(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
			return this.accountService.memberAccount(member.getId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 账户详情
	 * @return
	 */
	@RequestMapping(value="/member/myaccountdata",method=RequestMethod.GET)
	public ModelAndView myaccountdata(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/details.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	/**
	 * 资金详情页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/myAccountdata",method=RequestMethod.POST)
	public DyPhoneResponse myAccountdata(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			MbMember user = this.getMember(this.getMemberId());
			
			//Modify 先刷新银行卡
			doUserBindCard(user);
						
			Map<String,String> isRealName = new HashMap<String, String>(); ;
			MbApproveRealname realname = approveRealnameService.getRealname(this.getMemberId()) ;
			Integer isRealname = -1 ;
			if(null != realname){
				isRealname = realname.getStatus() ;
			}
			if(-1 == isRealname){
				isRealName.put("url", "/wap/member/approverealname") ;
				isRealName.put("name", "未认证") ;
			}else if(-2 == isRealname){
				isRealName.put("url", "/wap/member/checkRealname") ;
				isRealName.put("name", "审核中") ;
			}else{
				isRealName.put("url", "/wap/member/checkRealname") ;
				isRealName.put("name", "已认证") ;
			}
			Map<String,String> isEmail = new HashMap<String, String>(); ;
			if(1 != user.getIsEmail()){
				isEmail.put("url", "/wap/member/checkEmail") ;
				isEmail.put("name", "未认证") ;
			}else{
				isEmail.put("url", "/wap/member/updateemailone") ;
				isEmail.put("name", "已认证") ;
			}
			Map<String,String> isPhone = new HashMap<String, String>(); ;
			if(1 != user.getIsPhone()){
				isPhone.put("url", "/wap/member/checkPhone") ;
				isPhone.put("name", "未绑定") ;
			}else{
				isPhone.put("url", "/wap/member/updatephoneone") ;
				isPhone.put("name", "已绑定") ;
			}
			Map<String,String> paypassword = new HashMap<String, String>(); ;
			if(StringUtils.isBlank(user.getPaypassword())){
				paypassword.put("url", "/wap/member/setPaypwd") ;
				paypassword.put("name", "未设置") ;
			}else{
				paypassword.put("url", "/wap/member/editpaypwd") ;
				paypassword.put("name", "已设置") ;
			}
			Map<String,String> trustData = new HashMap<String, String>(); ;
			if(isTrust){
				trustData.put("url", "");
				trustData.put("name", StringUtils.isBlank(user.getTrustAccount()) ? "" : user.getTrustAccount()) ;
			}
			
			DyPhoneResponse response = frontMemberService.getApprove(member.getId().toString()) ;
			Map responseMap = (Map) response.getData() ;
			responseMap.put("is_realname", isRealName) ;
			responseMap.put("is_email", isEmail) ;
			responseMap.put("is_phone", isPhone) ;
			responseMap.put("paypassword", paypassword) ;
			responseMap.put("trust_account", trustData) ;
			String account = responseMap.get("account") == null ? "" : (String)responseMap.get("account");
			if(StringUtils.isNotBlank(account)) {
				responseMap.put("account2Wap", account.substring(0, 4) + "*********" + account.substring(account.length() - 4));
			}
			return response;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 用户信息
	 * @return
	 */
	@RequestMapping(value="/member/center",method=RequestMethod.GET)
	public ModelAndView center(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/center/center.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 用户信息页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/center",method=RequestMethod.POST)
	public DyPhoneResponse center(HttpServletRequest request){
		try {
			return this.frontMemberService.getInfo(this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 上传头像
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/avatarSubmit",method=RequestMethod.POST)
	public DyResponse avatarSubmit(HttpServletRequest request) throws Exception{
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		QueryItem infoItem = new QueryItem();
		infoItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbMemberInfo info = this.getOneByEntity(infoItem, Module.MEMBER, Function.MB_MEMBERINFO, MbMemberInfo.class);
		if(info ==null) return createErrorJsonResonse("上传错误");
		Map<String, File> fileMap = new HashMap<String, File>();
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		//头像
		CommonsMultipartFile avatar = (CommonsMultipartFile) multiRequest.getFile("avatar");
		System.out.println("----"+avatar.getSize()+"-----");//8388608
		if(avatar !=null && avatar.getSize() > 2097152) return createErrorJsonResonse("上传图片大于2M");
		if(avatar != null) {
			DiskFileItem fileItem = (DiskFileItem) avatar.getFileItem();
			fileMap.put(avatar.getOriginalFilename(), fileItem.getStoreLocation());
		}
		String filePath = PropertiesUtil.getImageHost() ;
		//上传到图片服务器
		if(fileMap.size() > 0) {
			DyResponse response = DyHttpClient.doImageUpload("member", "member", fileMap);
			if(response == null || response.getStatus() != DyResponse.OK) return createErrorJsonResonse(this.getMessage("update.error"));
			
			List<Map<String, Object>> fileList = (List<Map<String, Object>>) response.getData();
			for(Map<String, Object> map : fileList) {
				if(avatar != null && avatar.getOriginalFilename().equals(map.get("name").toString()))
					info.setAvatar(map.get("id").toString());
					this.updateById(Module.MEMBER, Function.MB_MEMBERINFO, info);
			}
		}
		filePath += info.getAvatar() ;
		Map<String,Object> responseMap = new HashMap<String, Object>() ;
		responseMap.put("avater", filePath) ;
		return createSuccessJsonResonse(responseMap,"上传成功");
	}
	
	/**
	 * 认证信息校验
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/system/isApprove",method=RequestMethod.POST)
	public DyPhoneResponse isApprove(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
			if(member == null){
				return errorJsonResonse(-1);
			}
			return approveRealnameService.isApprove(member.getId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 实名认证申请
	 * @return
	 */
	@RequestMapping(value="/member/approverealname",method=RequestMethod.GET)
	public ModelAndView approveRealname(){
		ModelAndView view = new ModelAndView();
		try {
			QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_REALNAME);
			queryItem.setFields("realname,card_id,positive_card,back_card");
			queryItem.setWhere(Where.eq("member_id", getMemberId()));
			queryItem.getWhere().add(Where.eq("status", -2));
			MbApproveRealname realName = getOneByEntity(queryItem, Module.MEMBER, Function.MB_REALNAME, MbApproveRealname.class);
			
			SystemInfo system = new SystemInfo();
			if(realName != null) system.setContentPage("member/checkrealname.jsp");
			else system.setContentPage("member/realname.jsp");
			view = this.initCommonPageView(system);
			
			view.addObject("is_realname_open", commonService.getSystemConfigByNid("idcard"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 实名认证申请
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/approverealname",method=RequestMethod.POST)
	public DyPhoneResponse approverealname(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String realName = request.getParameter("realname") ;
		String cardId = request.getParameter("card_id");
		String memberId = this.getMemberId().toString() ;
		return this.approveRealnameService.realnameApprove(request, response, realName, cardId, memberId);
	}
	
	/**
	 * 实名认证信息
	 * @return
	 */
	@RequestMapping(value="/member/checkRealname",method=RequestMethod.GET)
	public ModelAndView checkRealname(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/checkrealname.jsp");
			view = this.initCommonPageView(system);
			view.addObject("is_realname_open", commonService.getSystemConfigByNid("idcard"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 已实名认证页面数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/checkRealname",method=RequestMethod.POST)
	public DyPhoneResponse checkRealname(HttpServletRequest request){
		try {
			return frontMemberService.getApprove(this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 我的红包
	 * @return
	 * add by panxh at 2016年8月29日  for 我的红包
	 */
	@RequestMapping(value="/member/myBounty",method=RequestMethod.GET)
	public ModelAndView myBounty(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/myBounty.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 我的红包列表数据
	 * @param request
	 * @return
	 * add by panxh at 2016年8月29日  for 我的红包列表数据
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping(value="/member/myBountydata",method=RequestMethod.POST)
	public DyPhoneResponse myBountydata(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		MbMember user = this.getMember(member.getId());
		String page = request.getParameter("page");
		
		QueryItem bountyItem = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		addWhereCondition(whereList, "member_id", user.getId());
		whereList.add(Where.notEq("status", -1));
		bountyItem.setWhere(whereList);// 设置where条件
		bountyItem.setLimit(10);
		bountyItem.setPage(StringUtils.isBlank(page) ? 1 : Integer.parseInt(page));
		bountyItem.setOrders("FIELD(`red_status`, 1,2,3 , -1)");
		
		bountyItem.setFields("id,ind,serial_id,category_id,red_status,member_name,amount,amount_min,add_time,end_time,use_time");
		Page pageObj = (Page) this.dataConvert(this.getPageByMap(bountyItem, Module.FINANCE, Function.FN_BOUNTY), "red_status:getRedStatus", null);
		
		return successJsonResonse(pageObj);
	}
	/**
	 * 用户中心资产	
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/tenderFinance",method=RequestMethod.POST)
	public DyResponse tenderFinance() throws Exception{
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);	
		QueryItem item = new QueryItem();
		List<Where> whereList = new ArrayList<Where>();
		item.setFields("sum(amount) allamount");
		whereList.add(Where.eq("member_id",user.getId()));
		item.setWhere(whereList);
		Map map = this.getOneByMap(item, Module.LOAN, Function.LN_TENDER);
		BigDecimal allamount=new BigDecimal(map.get("allamount").toString());
		String []color={"#317ef3", "#83c4f3","#b2e0ef","#d6f0e0", "#f3d887", "#ff7f2e"} ;
		int i=0;
		List<Map> tenderMap=fnTenderService.getTenderMoney(user.getId());
		for(Map maps:tenderMap){
			maps.put("color", color[i]);
			i++;
			BigDecimal amount=new BigDecimal(maps.get("amount").toString());
			BigDecimal per=NumberUtils.div(amount, allamount, 2);
			maps.put("per", per.multiply(new BigDecimal(100))+"%");
		}
		return createSuccessJsonResonse(tenderMap);
	}
	
	/**
	 * 汇付绑卡特殊处理（因没有同步通知导致异步通知调取不到）
	 * @Description: TODO
	 * @CreateTime: 2019年10月21日 下午5:53:57
	 * @author: Administrator
	 * @throws Exception
	 * @throws
	 */
	private void doUserBindCard(MbMember user) throws Exception {	
		
		/**
		 * 获取本地银行卡列表
		 */
		QueryItem bankItem = new QueryItem();
		bankItem.getWhere().add(Where.eq("member_id", user.getId()));
		bankItem.getWhere().add(Where.eq("status", 1));
		List<MbMemberBank> list = (List<MbMemberBank>) this.getListByEntity(bankItem, Module.MEMBER, Function.MB_BANK, MbMemberBank.class);
		List<MbMemberBank> bankTrustList = new ArrayList<MbMemberBank>();
		List<String> localList = new ArrayList<String>();
		for (MbMemberBank mbMemberBank : list) {
			localList.add(mbMemberBank.getAccount());
		}
		
		/**
		 * 获取托管方银行卡列表
		 * 并insert本地没有的卡号
		 */
		trustManager.setTrustRequestService(trustRequestService);
		List<Map<String,String>> trustBankList = trustManager.getCardInfo(user.getTrustAccount(), null);
		
		Map<String,String> trusBanktMap = new HashMap<String, String>();
		if(!CollectionUtils.isEmpty(trustBankList)){
			for (Map<String, String> map2 : trustBankList) {
				trusBanktMap.put(map2.get("CardId"), map2.get("ExpressFlag"));
				//如果本地不存在，则添加到本地
				QueryItem realnameItem = new QueryItem();
				realnameItem.setWhere(Where.eq("member_id", user.getId()));
				realnameItem.setFields("realname");
				MbApproveRealname realname = (MbApproveRealname) this.getOneByEntity(realnameItem, Module.MEMBER, Function.MB_REALNAME, MbApproveRealname.class);
				if(!localList.contains(map2.get("CardId"))){
					MbMemberBank newBank = new MbMemberBank();
					newBank.setMemberId(user.getId());
					newBank.setMemberName(user.getName());
					newBank.setBankNid(map2.get("BankId"));
					newBank.setIsQuickpay("Y".equals(map2.get("ExpressFlag")) ? 1 : -1);
					newBank.setName(new BankMap().get(map2.get("BankId")));
					newBank.setAccount(map2.get("CardId"));
					newBank.setRealname(realname.getRealname());
					newBank.setAddTime(DateUtil.getCurrentTime());
					newBank.setStatus(1);
					this.insert(Module.MEMBER, Function.MB_BANK, newBank);
					
					bankTrustList.add(newBank);
				}
			}
		}
		/**
		 * 本地存在托管方不存在的情况下解绑本地银行卡
		 * 
		 */
		for (MbMemberBank mbMemberBank : list) {
			if(!trusBanktMap.containsKey(mbMemberBank.getAccount())){
				mbMemberBank.setUpdateTime(DateUtil.getCurrentTime());
				mbMemberBank.setStatus(-1);
				this.update(Module.MEMBER, Function.MB_BANK, mbMemberBank);
			}else{
				if(mbMemberBank.getIsQuickpay() == 1 && !"Y".equals(trusBanktMap.get(mbMemberBank.getAccount()))){
					mbMemberBank.setUpdateTime(DateUtil.getCurrentTime());
					mbMemberBank.setIsQuickpay(-1);
					this.update(Module.MEMBER, Function.MB_BANK, mbMemberBank);
				}
			}
		}
	}
	
	
}