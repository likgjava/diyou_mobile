package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.FrontBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.FnLoanApply;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemAreas;
import com.dy.baf.service.loan.LoanMobileService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.IpUtil;
import com.dy.core.utils.StringUtils;

/**
 * 在线申请
 * @author Administrator
 */
@Controller
public class LoanApplyController extends FrontBaseController {

	@Autowired
	private LoanMobileService loanService;
	
	/**
	 * 在线申请
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loan/loanApply")
	public DyPhoneResponse loanApply(String xmdy, String diyou) {
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
		try {
			String sessionCode = (String) this.getSessionAttribute(Constant.SESSION_PHONE_CODE);
			return this.loanService.loanApplySub(paramsMap,sessionCode,IpUtil.ipStrToLong(this.getRemoteIp()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/loanApply/getApplyData", method = RequestMethod.POST)
	public DyResponse getLoanApplyData(Long id) {
		QueryItem queryItem = new QueryItem();
		queryItem.setWhere(Where.eq("id", id));
		try {
			Map<String, Object> data = this.getOneByMap(queryItem, Module.LOAN, Function.LN_APPLY);
			return this.createSuccessJsonResonse(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createErrorJsonResonse(e.getMessage());
		}
	}

	@ResponseBody
	@RequestMapping(value = "/loanApply/applySubmit", method = RequestMethod.POST)
	public DyResponse loanApplySubmit(HttpServletRequest request) {
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		try {
			boolean isLogin = false;
			if (member != null && member.getId() != null) {
				isLogin = true;
				member = getMember(member.getId());
				if (member.getIsRealname() != 1 || member.getIsPhone() != 1 || member.getIsEmail() != 1 || member.getPaypassword() == null) {
					return createApproveError("请先进行认证!");
				}
			}
			FnLoanApply loanApply = new FnLoanApply();
			if (!isLogin) {
				String member_name = request.getParameter("member_name");
				if (StringUtils.isBlank(member_name)) {
					return this.createErrorJsonResonse("姓名不能为空");
				}
				String phone = request.getParameter("phone");
				if (StringUtils.isBlank(member_name)) {
					return this.createErrorJsonResonse("手机号码不能为空");
				}
				String phone_code = request.getParameter("phone_code");
				if (StringUtils.isBlank(phone_code)) {
					return this.createErrorJsonResonse("手机验证码不能为空");
				}
				String email = request.getParameter("email");
				if (StringUtils.isBlank(email)) {
					return this.createErrorJsonResonse("电子邮箱不能为空");
				}
				String sessionCode = (String) this.getSessionAttribute("sys_code");
				if (!phone_code.equals(sessionCode)) {
					return createErrorJsonResonse("手机验证码错误");
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
	
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			if (StringUtils.isBlank(name)) {
				return this.createErrorJsonResonse("项目名称不能为空");
			}
			String province = request.getParameter("province");
			String city = request.getParameter("city");
			String amount = request.getParameter("amount");
			if (StringUtils.isBlank(amount)) {
				return this.createErrorJsonResonse("借款金额不能为空");
			}
			String period = request.getParameter("period");
			if (StringUtils.isBlank(period)) {
				return this.createErrorJsonResonse("借款期限不能为空");
			}
			String apr = request.getParameter("apr");
			if (StringUtils.isBlank(apr)) {
				return this.createErrorJsonResonse("年化回报率不能为空");
			}
			String contents = request.getParameter("contents");
			if (StringUtils.isBlank(contents)) {
				return this.createErrorJsonResonse("详细描述不能为空");
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
				this.updateById(Module.LOAN, Function.LN_APPLY, loanApply);
			} else {
				loanApply.setAddTime(DateUtil.getCurrentTime());
				loanApply.setAddIp(IpUtil.ipStrToLong(this.getRemoteIp()));
				this.insert(Module.LOAN, Function.LN_APPLY, "id", loanApply);
			}
			String jumpUrl = null;
			if (isLogin) {
				jumpUrl = "loan/apply/myapplylist";
			}
			return this.createSuccessJsonResonse(jumpUrl, "申请成功");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createSuccessJsonResonse(null, "申请失败");
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/loanApply/getApplyList", method = RequestMethod.POST)
	public DyResponse getLoanApplyList(HttpServletRequest request) {
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if (member == null || member.getId() == null) {
				return this.createErrorJsonResonse("您未登陆！");
			}
			int pageNum = 1;
			String pageNumStr = request.getParameter("page");
			if (StringUtils.isNotBlank(pageNumStr)) {
				pageNum = Integer.valueOf(pageNumStr);
			}
			String status = request.getParameter("status");
			String startTime = request.getParameter("start_time");
			String endTime = request.getParameter("end_time");

			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id,status,member_name,user_name,phone,province,city,area,areas,name,amount,period,apr,email,contents,add_time");
			if (StringUtils.isNotBlank(status)) {
				queryItem.setWhere(Where.eq("status", status));
			}
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
			queryItem.setOrders("add_time desc");
			queryItem.setPage(pageNum);
			queryItem.setLimit(20);
			Page page = this.getPageByMap(queryItem, Module.LOAN, Function.LN_APPLY);
			return createSuccessJsonResonse(page);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return createErrorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 查看借款申请
	 * @return
	 */
	@RequestMapping(value = "/loanApply/getView", method = RequestMethod.GET)
	public ModelAndView viewLoanApply(Long id) {
		ModelAndView view = new ModelAndView("loan/loanApply/loan_apply_content");
		try {
			QueryItem queryItem = new QueryItem();
			queryItem.setFields("id,status,areas,name,amount,period,apr,contents,add_time");
			queryItem.setWhere(Where.eq("id", id));
			Map<String, Object> data = this.getOneByMap(queryItem, Module.LOAN, Function.LN_APPLY);
			view.addObject("loan_apply", this.dataConvert(data, "status:getApplyStatus", "add_time"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
}