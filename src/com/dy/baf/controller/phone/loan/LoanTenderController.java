package com.dy.baf.controller.phone.loan;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.loan.LoanTenderService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.entity.Page;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 我要投资
 * @author 波哥
 * @date 2015年9月8日 下午6:06:25 
 * @version V1.0
 */
@Controller(value="appLoanTenderController")
public class LoanTenderController extends AppBaseController {
	@Autowired
	private LoanTenderService loanTenderService;
	
	/**
	 * 我要投资列表 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({"rawtypes"})
	@ResponseBody
	@RequestMapping("tender/index")
	public DyPhoneResponse getTenderIndexList(String xmdy,String diyou) {
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			Integer page = Integer.valueOf(paramsMap.get("page")); 
			String loan_type = paramsMap.get("loan_type"); 
			String amount_search = paramsMap.get("amount_search"); 
			String apr_search = paramsMap.get("apr_search"); 
			String period_search = paramsMap.get("period_search"); 
			String order = paramsMap.get("order"); 
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			
			String url = this.getWebDomain(request,"wap/loan/appinfoview#?id=");
			
			Page objectPage = this.loanTenderService.getTenderIndexList(page, loan_type, amount_search, apr_search, period_search, order,url);
			Page loanPage =(Page) dataConvert(objectPage,"repay_type_name:getRepayType,category_name:getBorrowType");
			return successJsonResonse(loanPage);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 获取借款标详情地址页
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	public DyPhoneResponse loanInfoUrl(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			
			Map<String,String> map = new HashMap<String, String>();
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			map.put("url", this.getWebDomain(request,"/wap/loan/loaninfoview#?id="+id));
			return successJsonResonse(map);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 我要投资获取数据
	 */
	@ResponseBody
	@RequestMapping("tender/investData")
	public DyPhoneResponse investdata(String xmdy,String diyou) {
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("loan_id");
			return this.loanTenderService.investdata(id, Integer.valueOf(login_token));
			
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 投资收益
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("tender/investInterest")
	public DyPhoneResponse investInterest(String xmdy,String diyou) {
		try {
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			BigDecimal amount = new BigDecimal(paramsMap.get("amount"));
			Integer period = Integer.valueOf(paramsMap.get("period"));
			BigDecimal apr = new BigDecimal(paramsMap.get("apr"));
			Integer repay_type = Integer.valueOf(paramsMap.get("repay_type"));
			Double award_scale = StringUtils.isNotBlank(paramsMap.get("award_scale")) ? Double.valueOf(paramsMap.get("award_scale")) : null;
			Integer additional_status = MapUtils.getInteger(paramsMap, "additional_status"); 
			BigDecimal additional_apr = new BigDecimal(MapUtils.getString(paramsMap, "additional_apr","0"));
			
			String login_token = paramsMap.get("login_token");
			String id = paramsMap.get("loan_id");
			
			return this.loanTenderService.investInterest(amount, period, apr, repay_type, award_scale, additional_status, additional_apr, login_token, id);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 立即投资提交
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("tender/tender")
	public DyPhoneResponse invest(String xmdy,String diyou){
		try {
			
			Map<String,String> paramsMap = AppSecurityUtil.getParamters(diyou);
			Integer login_token = Integer.valueOf(paramsMap.get("login_token"));
			BigDecimal amount = new BigDecimal(paramsMap.get("amount"));
			Integer id = Integer.valueOf(paramsMap.get("loan_id"));
			String paypassword = paramsMap.get("paypassword");
			String password = paramsMap.get("password");
			String depositCertificate = paramsMap.get("depositCertificate");
			String redId = paramsMap.get("redbag");
			return this.loanTenderService.invest(login_token, id, amount, paypassword, password, depositCertificate, redId);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
