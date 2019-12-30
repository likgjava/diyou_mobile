package com.dy.baf.controller.phone.finance;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dy.core.utils.SecurityUtil;

/**
 * 微博支付
 * @author Administrator
 *
 */
public class WeiboPay {

	/**
	 * 获取参数
	 * @param configMap
	 * @param amount
	 * @param ind
	 * @return
	 */
	public String getParams(Map configMap,BigDecimal amount,String ind){
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
	String request_time = df.format(new Date());
	Map<String,Object> weiboMap = new HashMap<String,Object>();
	weiboMap.put("account", (String) configMap.get("Account"));
	weiboMap.put("password", (String) configMap.get("password"));
	weiboMap.put("submitUrl", "https://gate.pay.sina.com.cn/mas/gateway.do");//正式环境
	//weiboMap.put("submitUrl", "https://testgate.pay.sina.com.cn/mas/gateway.do");//测试环境
	weiboMap.put("return_url", "http://www.diyou.cc/Paynotices/weiboReturn"); // 同步返回（可空）
	weiboMap.put("notify_url", "http://www.diyou.cc/Paynotices/weiboNotify");// 异步返回（可空）
	weiboMap.put("service", "create_instant_order");//接口名称（必输）
	weiboMap.put("version", "1.0");//接口版本(必输)
	weiboMap.put("request_time", request_time);//请求时间(必输）
	weiboMap.put("partner_id", (String) configMap.get("Account"));//合作者身份id商户号（必输）
	weiboMap.put("_input_charset", "UTF-8");//参数编码字符集(必输)
	weiboMap.put("sign_type", "MD5");//签名方式(必输)
	weiboMap.put("sign_version", "");//签名版本号(可空)
	weiboMap.put("encrypt_version", "");//加密版本号(可空)
	weiboMap.put("memo", "");//备注(可空)
	weiboMap.put("out_trade_no", ind);//流水号(必输)
	weiboMap.put("buyer_identity_id", "");//买家标志(可空)
	weiboMap.put("buyer_identity_type", "");//买家标志类型(可空)
	weiboMap.put("seller_identity_id", (String) configMap.get("Account"));//卖家标志（必输）
	weiboMap.put("seller_identity_type", "MEMBER_ID");//卖家标志类型(必输)默认都是MEMBER_ID
	weiboMap.put("amount", String.valueOf(amount));//充值金额(必输)
	weiboMap.put("product_desc", "平台充值");//商品描述(可空)
	weiboMap.put("can_repeat", "N");//支付失败后是否可以再次支付 （可空）Y是、N否(忽略大小写)
	weiboMap.put("expired_time", "");//订单过期时间 （可空）默认 1d 1天
	weiboMap.put("split_list", "");//分账信息列表 （可空）
	weiboMap.put("extend_param", "");//扩展信息 （可空）
	weiboMap.put("payer_ip", "");//付款ip （可空）
	weiboMap.put("device_id", "");//设备mac地址 （可空）
	weiboMap.put("pay_method", "");//支付方式 （可空）
	weiboMap.put("postmethod", "POST");
	weiboMap.put("sign", getSignMsg(weiboMap));//// 签名（必输）
	
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
	 * 加密方式
	 */
	public String getSignMsg(Map<String,Object> map){
		if("MD5".equals(map.get("sign_type"))){
			return SecurityUtil.md5((String) map.get("password"));
		}else{
			return null;
		}
	}
}
