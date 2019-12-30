package com.dy.baf.controller.phone.loan;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.loan.BuyTransferService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.entity.Page;
import com.dy.core.utils.StringUtils;

@Controller(value="appBuyTransferController")
public class BuyTransferController extends AppBaseController {
	@Autowired
	private BuyTransferService buyTransferService;

	
	/**
	 * 债权转让列表 
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/index")
	public DyPhoneResponse buyTransferList(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String page = paramsMap.get("page");
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			String url = this.getWebDomain(request,"wap/transfer/appTransferView#?id=");
			Page objectPage = this.buyTransferService.buyTransferList(null, null, null, Integer.valueOf(page), url);
			return successJsonResonse(objectPage);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
	
	/**
	 * 购买信息数据
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/TransferBuyInfo")
	public DyPhoneResponse getTransferBuyData(String xmdy,String diyou) throws Exception{
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
		String login_token = paramsMap.get("login_token");
		String transferId = paramsMap.get("transfer_id");
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		Map<String,Object> map = this.buyTransferService.getTransferBuyData(transferId,login_token);
		return successJsonResonse(map);
	}
	
	/**
	 * 开始购买
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/buy")
	public DyPhoneResponse buySubmit(String xmdy,String diyou) throws Exception{
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
		String login_token = paramsMap.get("login_token");
		String transferId = paramsMap.get("transfer_id");
		String loanId = paramsMap.get("loan_id");
		String paypassword = paramsMap.get("paypassword");
		return this.buyTransferService.buySubmit(transferId,loanId,paypassword,login_token,this.getRemoteIp());
	}
	
	/**
	 * 是否是自己的债权
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/transfer/isMyTransfer")
	public DyPhoneResponse isMyTransfer(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String transfer_id = paramsMap.get("transfer_id");

			return this.buyTransferService.isMyTransfer(login_token, transfer_id);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
}
