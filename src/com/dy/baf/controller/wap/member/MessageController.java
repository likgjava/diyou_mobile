package com.dy.baf.controller.wap.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.MessageService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.Constant;
import com.dy.core.utils.RequestUtil;
/**
 *	站内信 
 */
@Controller(value="wapMessageController")
public class MessageController extends WapBaseController  {
	@Autowired
	private MessageService messageService;
	/**
	 * 站内信列表
	 */
	@RequestMapping("/message/list")
	public ModelAndView messageList(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/msgList.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 站内信列表数据
	 */
	@ResponseBody
	@RequestMapping("/message/getList")
	public DyPhoneResponse messageList(HttpServletRequest request){
		try {
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			String page = RequestUtil.getString(request, "page", "1") ;
			return this.messageService.messageLog(Integer.parseInt(page), member.getId().toString());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 查看站内信
	 * @return
	 */
	@RequestMapping(value="/message/detail" , method=RequestMethod.GET)
	public ModelAndView messageDetail(){
		String id = this.getRequest().getParameter("id");
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/msgDetail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 站内信内容
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/message/detail" , method=RequestMethod.POST)
	public DyPhoneResponse messageDetail(HttpServletRequest request) throws Exception{
		return this.messageService.messageViewed(RequestUtil.getLong(request, "id", null)) ;
	}
	
	@ResponseBody
	@RequestMapping("/account/messageCount")
	public Map<String, Integer> messageCount() throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("count(1) msgnum");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("member_id", user.getId()));
		queryItem.setWhere(whereList);
		Map<String, Object> map = getOneByMap(queryItem, Module.MEMBER, Function.MB_MESSAGE);
		
		return Collections.singletonMap("msgNum", Integer.parseInt(map.get("msgnum").toString()));
	}
}
