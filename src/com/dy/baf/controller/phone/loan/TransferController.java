package com.dy.baf.controller.phone.loan;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.loan.TransferService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.entity.Page;
import com.dy.core.utils.StringUtils;
@Controller(value="appTransferController")
public class TransferController extends AppBaseController {

	@Autowired
	private TransferService transferService;
	
	/**
	 * 我的债权转让列表/购买记录列表获取
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/myTransferList")
	public DyPhoneResponse myTransferList(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String page = paramsMap.get("page");
			String login_token = paramsMap.get("login_token");
			String statusNid = paramsMap.get("status_nid");
			String epage = paramsMap.get("epage");
			if(StringUtils.isBlank(statusNid)){
				statusNid = "transfer";
			}
			
			Map<String,Object> transferMap = (Map<String, Object>) this.transferService.myTransfer(Long.valueOf(login_token)).getData();
			
			
			Map<String,Object> resonseMap = new HashMap<String, Object>();
			
			Page resonsePage = (Page) this.transferService.getTransferList(login_token,statusNid,page,epage).getData();
			
			resonseMap.put("epage", resonsePage.getEpage());
			resonseMap.put("items", resonsePage.getItems());
			resonseMap.put("page", resonsePage.getPage());
			resonseMap.put("total_items", resonsePage.getTotal_items());
			resonseMap.put("total_pages", resonsePage.getTotal_pages());
			resonseMap.putAll(transferMap);
			return successJsonResonse(resonseMap);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 债权转让提交
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/transferSub")
	public DyPhoneResponse transferSub(String xmdy,String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String tenderId = paramsMap.get("tender_id");
			String coefficient = paramsMap.get("coefficient");
			String paypassword = paramsMap.get("paypassword");
			return this.transferService.transferSub(login_token,tenderId,coefficient,paypassword);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 撤销提交
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/cancel")
	public DyPhoneResponse cancelSubmit(String xmdy,String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String transferId = paramsMap.get("transfer_id");
			return this.transferService.cancelSubmit(login_token,transferId);
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 债权转让详情
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/transferInfo")
	public DyPhoneResponse transferInfo(String xmdy,String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String tenderId = paramsMap.get("tender_id");
			if (StringUtils.isBlank(login_token)) {
				return errorJsonResonse("用户登录标识不能为空");
			}
			return this.transferService.transferInfo(login_token,tenderId);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 债权购买记录详情
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/myTransferBuyInfo")
	public DyPhoneResponse myTransferBuyInfo(String xmdy,String diyou) throws Exception{
		Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
		String login_token = paramsMap.get("login_token");
		String transferId = paramsMap.get("transfer_id");
		String page = paramsMap.get("page");
		String epage = paramsMap.get("epage");
		if (StringUtils.isBlank(login_token)) {
			return errorJsonResonse("用户登录标识不能为空");
		}
		Map<String,Object> map =  this.transferService.myTransferBuyInfo(login_token,transferId,page,epage);
		return successJsonResonse(map);
	}
}
