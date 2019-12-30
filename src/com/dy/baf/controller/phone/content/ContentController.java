package com.dy.baf.controller.phone.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.service.content.ContentService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * @Description: 内容部分获取
 * @author 波哥
 * @date 2015年9月3日 下午2:24:56 
 * @version V1.0
 */
@Controller(value="appContentController")
public class ContentController extends AppBaseController {
	
	protected Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private ContentService contentService;
	
	/**
	 * 获取公告栏（这里只返回url地址，公告栏页面由服务端提供）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/articles/noticeList")
	public DyPhoneResponse noticeList(HttpServletRequest request){
		Map<String,String> urlMap = new HashMap<String,String>();
		urlMap.put("url", this.getWebDomain(request, "wap/articles/appNotice"));
		return successJsonResonse(urlMap);
	}
	
	/**
	 * 获取常见问题栏目（这里只返回url地址，公告栏页面由服务端提供）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/articles/articlesList")
	public DyPhoneResponse articlesList(HttpServletRequest request){
		Map<String,String> urlMap = new HashMap<String,String>();
		urlMap.put("url", this.getWebDomain(request,"wap/articles/appArticles"));
		return successJsonResonse(urlMap);
	}
	
	/**
	 * 获取单条协议
	 * @param type
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/articles/agreement")
	public DyPhoneResponse agreement(String xmdy, String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String type = paramsMap.get("type");
			if(StringUtils.isBlank(type)){
				return errorJsonResonse("协议类型不能为空");
			}
			CtAgreement agree = contentService.agreement(type);
			if(agree == null){
				return errorJsonResonse("协议不存在");
			}
			return successJsonResonse(agree);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return errorJsonResonse(e.getMessage());
		}
	}
}