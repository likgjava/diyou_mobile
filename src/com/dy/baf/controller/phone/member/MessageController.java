package com.dy.baf.controller.phone.member;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.member.MessageService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.baf.utils.HtmlFilterUtil;
import com.dy.core.entity.Page;

/**
 * 
 * @Description: TODO
 * @author 波哥
 * @date 2015年9月13日 下午2:28:17 
 * @version V1.0
 */
@Controller(value="appMessageController")
public class MessageController extends AppBaseController {

	@Autowired
	private MessageService messageService;
	/** 
	 * 站内信列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/member/messageLog")
	public DyPhoneResponse messageLog(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			String page = paramsMap.get("page");
			
			DyPhoneResponse phoneResponse =  this.messageService.messageLog(Integer.parseInt(page), login_token);
			Page pageResponse= (Page) phoneResponse.getData();
			
			List<Map> itemList = pageResponse.getItems();
			for (Map map : itemList) {
				map.put("contents", HtmlFilterUtil.getTextFromHtml(map.get("contents").toString()));
			}
			return phoneResponse;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 改变已读未读状态
	 */
	@ResponseBody
	@RequestMapping("/member/messageView")
	public DyPhoneResponse messageViewed(String xmdy, String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String id = paramsMap.get("id");
			return this.messageService.messageViewed(Long.valueOf(id));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
}