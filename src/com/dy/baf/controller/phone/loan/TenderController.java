package com.dy.baf.controller.phone.loan;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.loan.TenderService;
import com.dy.baf.utils.AppSecurityUtil;

/**
 * 
 * 
 * @Description: 我的投资
 * @author 波哥
 * @date 2015年9月8日 下午6:06:45
 * @version V1.0
 */
@Controller(value="appTenderController")
public class TenderController extends AppBaseController {

	@Autowired
	private TenderService tenderService;

	@ResponseBody
	@RequestMapping("loan/isMyLoan")
	public DyPhoneResponse isMyLoan(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String loanId = paramsMap.get("loan_id");

			return this.tenderService.isMyLoan(login_token, loanId);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 我的投资统计
	 * 
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/tender/mytenderdata")
	public DyPhoneResponse myTenderData(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return successJsonResonse(this.tenderService.myTenderData(login_token));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}

	}

	/**
	 * 我的投资记录列表
	 * 
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/tender/myTenderList")
	public DyPhoneResponse mytenderlist(String xmdy, String diyou) {
		try {

			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			Integer page = Integer.valueOf(paramsMap.get("page"));
			String status = paramsMap.get("status");
			String start_time = paramsMap.get("start_time");
			String end_time = paramsMap.get("end_time");

			return this.tenderService.mytenderlist(login_token, page, status, start_time, end_time);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 收款详情获取数据
	 * 
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/tender/tenderInfo")
	public DyPhoneResponse tenderinfodata(String xmdy, String diyou) {

		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return successJsonResonse(this.tenderService.tenderinfodata(Long.valueOf(id)));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 获取投资提示信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/tender/getTenderTip")
	public DyPhoneResponse getLoanTip(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.tenderService.getLoanTip(Long.valueOf(id));
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}

}