package com.dy.baf.service.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.dy.core.utils.SecurityUtil;

/**
 * 丰付支付
 * @author Administrator
 *
 */
public class SumaPay {

	/**
	 * 获取参数
	 * @param configMap
	 * @param amount
	 * @param ind
	 * @return
	 */
	public String getParams(Map configMap,BigDecimal amount,String ind){
	Map<String,Object> weiboMap = new HashMap<String,Object>();
	weiboMap.put("submitUrl", "https://www.sumapay.com/sumapay/unitivepay_bankPayForNoLoginUser");//正式环境
	weiboMap.put("requestId", ind);//流水号（必输）
	weiboMap.put("tradeProcess", (String) configMap.get("Account"));//商户号(必输)
	weiboMap.put("totalBizType", "BIZ01101");// 业务类型，默认BIZ00800 （必输）
	weiboMap.put("totalPrice", String.valueOf(amount));// 充值的金额 （必输）
	weiboMap.put("backurl", "http://www.diyou.cc/Paynotices/sumaReturn");// 同步跳转地址 （必输）
	weiboMap.put("returnurl", "http://www.diyou.cc/Paynotices/sumaReturn");// 未支付跳转地址 （必输）
	weiboMap.put("noticeurl", "http://www.diyou.cc/Paynotices/sumaNotify");// 异步通知跳转地址 （必输）
	weiboMap.put("description", "");// 描述信息 （选输）
	weiboMap.put("goodsDesc", "");// 商品描述 （选输）
	weiboMap.put("allowRePay", "0");// 是否可重新支付 0：不允许；1：允许 （选输）
	weiboMap.put("rePayTimeOut", "");// 重新支付有效时间 （选输）
	weiboMap.put("userIdIdentity", ind);// 商户用户唯一标识 （选输）
	weiboMap.put("bankCardType", "");// 网银支付借贷分离标记 不输入或0：不区分借贷1：借记卡 2：贷记卡 （选输）
	weiboMap.put("payTag", "");// 支付方式标签显示 （选输）
	weiboMap.put("productId", "zfcz");// 产品的编码 （必输）
	weiboMap.put("productName", "p2p_recharge");// 产品的名称 （必输）
	weiboMap.put("fund", String.valueOf(amount));// 产品定价 （必输）
	weiboMap.put("merAcct", configMap.get("merAcct"));// 供应商编码 （必输）
	weiboMap.put("bizType", "BIZ01101");// 产品业务类型 （必输）
	weiboMap.put("productNumber", "1");// 产品订购数量 默认1 （必输）
	//组装签名
	String sbOld = (String)weiboMap.get("requestId") + (String)weiboMap.get("tradeProcess")+ (String)weiboMap.get("totalBizType") +(String)weiboMap.get("totalPrice") +(String)weiboMap.get("backurl")+(String)weiboMap.get("returnurl")+(String)weiboMap.get("noticeurl")+(String)weiboMap.get("description");
	String sign = HmacMd5(sbOld,(String) configMap.get("password"));
	weiboMap.put("mersignature", sign);//// 签名（必输）
	weiboMap.put("postmethod", "POST");
	
	return this.applyForm (weiboMap);
	}
	
	/**
	 * 构造表单
	 */
	public String applyForm(Map<String,Object> weiboMap){
		String tmpForm = "";
		tmpForm = "<form name='applyForm' id='applyForm' target='_blank' method='"+weiboMap.get("postmethod")+"' action='"+weiboMap.get("submitUrl")+"'>";
	
		for( String key :weiboMap.keySet()) {
			Object value = weiboMap.get(key);
			tmpForm = tmpForm + "<input type='hidden' name='"+key+"' value='"+value+"'>";
		}
		tmpForm = tmpForm+"</form>";
	
		tmpForm = tmpForm +"<script>document.forms['applyForm'].submit();</script>";
		return tmpForm;
	}
	
	/**
	 * 丰付加密
	 */
	private String HmacMd5(String sbOld, String password) {
//		$key = iconv ( "UTF-8", "GB2312", $key );
//		$data = iconv ( "UTF-8", "GB2312", $data );
//		
//		$b = 64; // byte length for md5
//		if (strlen ( $key ) > $b) {
//			$key = pack ( "H*", md5 ( $key ) );
//		}
//		$key = str_pad ( $key, $b, chr ( 0x00 ) );
////		$ipad = str_pad ( '', $b, chr ( 0x36 ) );
//		$opad = str_pad ( '', $b, chr ( 0x5c ) );
//		$k_ipad = $key ^ $ipad;
//		$k_opad = $key ^ $opad;
//		
//		return md5 ( $k_opad . pack ( "H*", md5 ( $k_ipad . $data ) ) );
		return SecurityUtil.md5(sbOld+password);
	}
}
