package com.dy.baf.controller.wap.finance;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberReviews;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.baf.service.finance.RechargeService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.StringUtils;

/**
 * 充值
 */
@Controller(value="wapRechargeController")
public class RechargeController extends WapBaseController{
	@Autowired
	private RechargeService rechargeService ;
	
	/**
	 * 充值
	 * @return
	 */
	@RequestMapping(value="/member/recharge",method=RequestMethod.GET)
	public ModelAndView recharge(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/recharge.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	@RequestMapping(value="/member/checkTrustAccount",method=RequestMethod.POST)
	@ResponseBody
	public DyPhoneResponse checkTrustAccount(){
		try {
			MbMember member = (MbMember) getSessionAttribute(Constant.SESSION_USER);
			member = this.getMember(member.getId());
			if (isTrust && StringUtils.isBlank(member.getTrustAccount())) {
				return this.successJsonResonse(0);
			}
			
			String riskFlag = this.getRequest().getParameter("riskFlag");
			//判断是否风险评测过
			if(StringUtils.isNotBlank(riskFlag) && "1".equals(riskFlag)){
				//是否提交过风评
				QueryItem queryReviews = new QueryItem();
				queryReviews.setWhere(Where.eq("member_id", member.getId()));
				queryReviews.setOrders("id,create_time asc");
				List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
				
				
				if(null == reviews || reviews.size() == 0){
					return this.successJsonResonse(5);
				}
				
				//判断评测是否有过期(测评时间满24个月)
				/*Calendar nowTime = Calendar.getInstance();   
			    Date nowDate = (Date) nowTime.getTime(); //得到当前时间  
			    Calendar afterTime = Calendar.getInstance();   
			    Date date = new Date(reviews.get(reviews.size()-1).getCreateTime()*1000);
			    afterTime.setTime(date);
			    afterTime.add(Calendar.YEAR, 2);
				if(afterTime.getTimeInMillis() < nowDate.getTime()){
					return this.successJsonResonse(6);//已到期，请重新进行风险评测
				}*/
			}
			
			boolean isRealname = member.getIsRealname() != 1 && member.getIsRealname() != -2;
			return this.successJsonResonse(isRealname ? 2 : (member.getIsEmail()  != 1 && isOpenEmail())? 3 : (member.getIsPhone() != 1 ? 4 : 1));
		} catch (Exception e) {
			return this.errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 邮箱验证是否开启
	 * @return
	 * @throws Exception
	 * add by panxh at 2016年8月22日  for 是否需要邮箱认证
	 */
	private boolean isOpenEmail() throws Exception{
		QueryItem queryItem = new QueryItem();
		queryItem.setWhere(Where.eq("nid", "email_auth"));
		SysSystemConfig config = this.getOneByEntity(queryItem, Module.SYSTEM, Function.SYS_CONFIG, SysSystemConfig.class);
		if("2".equals(config.getValue())){//关闭
			return false;
		}
		return true;
	}
	/**
	 * 充值方式
	 */
	@ResponseBody
	@RequestMapping("/recharge/getPaymentList")
	public DyPhoneResponse getPaymentList(HttpServletRequest request) {
		try {
			String page = request.getParameter("page");
			return this.rechargeService.getlist(page, this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}

	}
	
	/**
	 * 充值手续费
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/member/rechargefee",method=RequestMethod.POST)
	public DyPhoneResponse rechargeFee(HttpServletRequest request){
		try {
			String paymentNid = request.getParameter("paymentType") ;
			BigDecimal amount = new BigDecimal(request.getParameter("amount")) ;
			return this.rechargeService.rechargefee(amount, paymentNid, this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 充值
	 */
	@ResponseBody
	@RequestMapping("/recharge/recharge")
	public DyPhoneResponse recharge(HttpServletRequest request) {
		try {
			BigDecimal amount = new BigDecimal(request.getParameter("amount"));
			String query_type = request.getParameter("query_type");
			String type = request.getParameter("type");
			String payment = request.getParameter("payment");
			String valicode = request.getParameter("valicode");
			String memberId = this.getMemberId().toString();

			// 验证码校验
			if ("".equals(valicode))
				return errorJsonResonse("验证码不能为空!");
			String sessionVerifyCode = (String) this.getSessionAttribute(Constant.SESSION_VERIFY_CODE);
			if (!valicode.equalsIgnoreCase(sessionVerifyCode))
				return errorJsonResonse("验证码错误!");
			return this.rechargeService.recharge(query_type, type, payment, amount, valicode, memberId);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}
	}
	@RequestMapping(value="/recharge/rechargelog",method=RequestMethod.GET)
	public ModelAndView rechargelog(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/rechargeRecord.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	
	/**
	 * 充值记录列表
	 */
	@ResponseBody
	@RequestMapping("/recharge/rechargelog")
	public DyPhoneResponse rechargelog(HttpServletRequest request) {
		try {
			Integer page = Integer.valueOf(request.getParameter("page")==null?"1":request.getParameter("page"));
			Integer status = null;
			String memberId = ((MbMember)this.getSession().getAttribute(Constant.SESSION_USER)).getId().toString();
			Page pageObject =  this.rechargeService.rechargelog(page, status, memberId);
			return successJsonResonse(dataConvert(pageObject,"status_name:getRechargeStatus,type_name:getRechargeType"));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(logger);
		}

	}
}
