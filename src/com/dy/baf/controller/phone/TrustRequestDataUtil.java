package com.dy.baf.controller.phone;

import java.util.Map;

/**
 * 
 * 
 * 托管请求参数处理，转换app数据与pc一样
 * <br>
 * app请求参数没有和pc端统一
 * </br>
 * @author 波哥
 * @date 2015年10月14日 上午10:33:44 
 * @version V1.0
 */
public class TrustRequestDataUtil {

	/**
	 * 提现
	 * @param paramMap
	 * @throws Exception
	 */
	public static void withdraw(Map<String, String> paramMap) throws Exception {
		paramMap.put("money", paramMap.get("amount"));
		paramMap.remove("amount");
		
	}
	
	/**
	 * 投资
	 * @param paramMap
	 * @throws Exception
	 */
	public static void tender(Map<String, String> paramMap) throws Exception {
		paramMap.put("id", paramMap.get("loanId"));
		paramMap.remove("loanId");
		
	}
	
	/**
	 * 债权购买
	 * @param paramMap
	 * @throws Exception
	 */
	public static void buyTransfer(Map<String, String> paramMap) throws Exception {
		paramMap.put("id", paramMap.get("transferId"));
		paramMap.remove("transferId");
		
	}
	
	/**
	 * 还款
	 * @param paramMap
	 * @throws Exception
	 */
	public static void repay(Map<String, String> paramMap) throws Exception {
		paramMap.put("repay_id", paramMap.get("repayId"));
		paramMap.remove("repayId");
		
	}
}
