package com.dy.baf.controller.phone.member;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.member.MemberInfoService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.StringUtils;
/**
 * 基础信息
 * @author Administrator
 *
 */
@Controller(value = "appMemberInfoController")
public class MemberInfoController extends AppBaseController {

	@Autowired
	private MemberInfoService memberInfoService;
	
	
	/**
	 * 个人基本信息
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/basicInfo")
	public DyPhoneResponse basicInfo(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.memberInfoService.basicInfo(login_token);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 个人基本信信息提交
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/basicInfoSub")
	public DyPhoneResponse basicInfoSub(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String educational_background = paramsMap.get("educational_background");
			String graduated = paramsMap.get("graduated");
			String marry_status = paramsMap.get("marry_status");
			String company_industry = paramsMap.get("company_industry");
			String company_scale = paramsMap.get("company_scale");
			String company_office = paramsMap.get("company_office");
			String monthly_income = paramsMap.get("monthly_income");
			String hometown_province = paramsMap.get("hometown_province");
			String hometown_city = paramsMap.get("hometown_city");
			String hometown_area = paramsMap.get("hometown_area");
			String hometown_post = paramsMap.get("hometown_post");
			
			MbMemberInfo memberInfo = new MbMemberInfo();
			
			memberInfo.setMemberId(Long.valueOf(login_token));
			if(StringUtils.isNotBlank(educational_background)){
				memberInfo.setEducationalBackground(Integer.valueOf(educational_background));
			}
			if(StringUtils.isNotBlank(graduated)){
				memberInfo.setGraduated(graduated);
			}
			if(StringUtils.isNotBlank(marry_status)){
				memberInfo.setMarryStatus(Integer.valueOf(marry_status));
			}
			if(StringUtils.isNotBlank(company_industry)){
				memberInfo.setCompanyIndustry(Integer.valueOf(company_industry));
			}
			if(StringUtils.isNotBlank(company_scale)){
				memberInfo.setCompanyScale(Integer.valueOf(company_scale));
			}
			if(StringUtils.isNotBlank(company_office)){
				memberInfo.setCompanyOffice(Integer.valueOf(company_office));
			}
			if(StringUtils.isNotBlank(monthly_income)){
				memberInfo.setMonthlyIncome(Integer.valueOf(monthly_income));
			}
			if(StringUtils.isNotBlank(hometown_province)){
				memberInfo.setHometownProvince(Long.valueOf(hometown_province));
			}
			if(StringUtils.isNotBlank(hometown_city)){
				memberInfo.setHometownCity(Long.valueOf(hometown_city));
			}
			if(StringUtils.isNotBlank(hometown_post)){
				memberInfo.setHometownPost(hometown_post);
			}
			if(StringUtils.isNotBlank(hometown_area)){
				memberInfo.setHometownArea(Long.valueOf(hometown_area));
			}
			return this.memberInfoService.basicInfoSub(memberInfo);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 公司信息
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/companyInfo")
	public DyPhoneResponse companyInfo(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("id");
			return this.memberInfoService.companyInfo(login_token);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 公司信息提交
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/companyInfoSub")
	public DyPhoneResponse companyInfoSub(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			if(StringUtils.isBlank(login_token)){
				return errorJsonResonse("用户登录标识不能为空");
			}
			String id = paramsMap.get("id");
			return this.memberInfoService.companyInfoSub(login_token,paramsMap.toString());
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
