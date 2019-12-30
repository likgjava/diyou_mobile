package com.dy.baf.controller.wechat.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.wechat.WechatBaseController;
import com.dy.baf.entity.WeChat;
import com.dy.baf.entity.common.SysWechatAttention;
import com.dy.baf.entity.common.SysWechatRecovery;
import com.dy.baf.entity.common.SysWechatSet;
import com.dy.baf.entity.wechat.message.req.TextMessage;
import com.dy.baf.entity.wechat.message.resp.Article;
import com.dy.baf.entity.wechat.message.resp.NewsMessage;
import com.dy.baf.utils.wechat.MessageUtil;
import com.dy.baf.utils.wechat.SignUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.StringUtils;
import com.dy.httpinvoker.WechatService;

/**
 * 
 * 
 * @Description: 微信校验
 * @author 波哥
 * @date 2015年9月29日 上午10:14:36
 * @version V1.0
 */
@Controller(value = "wechatController")
public class WechatController extends WechatBaseController {
	
	@Autowired
	private WechatService wechatService;
	
	@ResponseBody
	@RequestMapping(value = "/api", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public String validate(WeChat wc) {
		try {
			String signature = wc.getSignature(); // 微信加密签名
			String timestamp = wc.getTimestamp(); // 时间戳
			String nonce = wc.getNonce();// 随机数
			String echostr = wc.getEchostr();// 随机字符串
			
			QueryItem queryItem = new QueryItem(Module.SYSTEM, Function.SYS_WECHAT_SET);
			queryItem.setWhere(Where.eq("name", "token"));
			SysWechatSet wechatSet = (SysWechatSet) this.getOne(queryItem,SysWechatSet.class);
			
			// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
			if (SignUtil.checkSign(wechatSet.getValue(),signature, timestamp, nonce)) {
				return echostr;
			} else {
				return "请求非法";
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return "请求非法";
		}
	}

	@ResponseBody
	@RequestMapping(value = "/api", method = RequestMethod.POST ,produces = "application/json;charset=utf-8")
	public String getWechatMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 将请求、响应的编码均设置为UTF-8（防止中文乱码）
		request.setCharacterEncoding("UTF-8"); // 微信服务器POST消息时用的是UTF-8编码，在接收时也要用同样的编码，否则中文会乱码；
		response.setCharacterEncoding("UTF-8"); // 在响应消息（回复消息给用户）时，也将编码方式设置为UTF-8，原理同上；
		// 初始化配置文件
		String respMessage = this.processRequest(request);// 调用CoreService类的processRequest方法接收、处理消息，并得到处理结果；
		// 响应消息
		// 调用response.getWriter().write()方法将消息的处理结果返回给用户
		return respMessage;
	}

	/**
	 * 处理微信发来的请求
	 * 
	 * @param request
	 * @return xml
	 */
	private String processRequest(HttpServletRequest request) {
		// xml格式的消息数据
		String respXml = null;
		try {
			// 调用parseXml方法解析请求消息
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			// 发送方账号
			String fromUserName = requestMap.get("FromUserName");
			// 开发者微信号
			String toUserName = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");
			//解密加密消息
/*			String encrypt=requestMap.get("Encrypt");
			String xmlContent=SignUtil.decrypt(encrypt,"wx17c4758faa703bd1");
			String fromXML = String.format(xmlContent, encrypt);*/

			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			// 事件推送
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {

					// 将消息对象转换成xml
					QueryItem queryItem = new QueryItem();
					queryItem.setWhere(Where.eq("focus", "2"));
					queryItem.setLimit(1);
					List<SysWechatAttention> attentions = this.getListByEntity(queryItem, Module.SYSTEM, Function.SYS_WECHAT_ATTENTION, SysWechatAttention.class);
					if (attentions != null && attentions.size() > 0) {
						SysWechatAttention attention = attentions.get(0);
						if (attention.getType() == 2) {
							Article article = new Article();
							article.setTitle(String.valueOf(attention.getTitle()));
							article.setDescription(String.valueOf(attention.getDescription()));

							List<Article> articleList = new ArrayList<Article>();
							articleList.add(article);
							article.setPicUrl(String.valueOf(attention.getImg()));
							article.setUrl(String.valueOf(attention.getLink()));
							NewsMessage newsMessage = new NewsMessage();
							newsMessage.setToUserName(fromUserName);
							newsMessage.setFromUserName(toUserName);
							newsMessage.setCreateTime(new Date().getTime());
							if (attention.getType() == 2) {
								newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
							} else {
								newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
							}
							newsMessage.setArticleCount(articleList.size());
							newsMessage.setArticles(articleList);
							respXml = MessageUtil.messageToXml(newsMessage);
						} else {
							textMessage.setContent(String.valueOf(attention.getDescription()));
							respXml = MessageUtil.messageToXml(textMessage);
						}
					} else {
						textMessage.setContent("欢迎关注");
						respXml = MessageUtil.messageToXml(textMessage);
					}
				}
				// 取消订阅
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO 暂不做处理
				}
				// 自定义菜单点击事件
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// 事件KEY值，与创建菜单时的key值对应
					String eventKey = requestMap.get("EventKey");
					// 根据key值判断用户点击的按钮
					QueryItem queryItem = new QueryItem();
					queryItem.setWhere(Where.eq("keywords", eventKey));
					List<SysWechatRecovery> wechatRecoveries = this.getListByEntity(queryItem, Module.SYSTEM, Function.SYS_WECHAT_RECOVERY, SysWechatRecovery.class);
					if (wechatRecoveries != null && wechatRecoveries.size() > 0) {
						SysWechatRecovery wechatRecovery = wechatRecoveries.get(0);
						if (wechatRecovery.getType() == 2) {
							Article article = new Article();
							article.setTitle(String.valueOf(wechatRecovery.getTitle()));
							article.setDescription(String.valueOf(wechatRecovery.getDescription()));

							List<Article> articleList = new ArrayList<Article>();
							articleList.add(article);
							article.setPicUrl(String.valueOf(wechatRecovery.getImg()));
							article.setUrl(String.valueOf(wechatRecovery.getLink()));
							NewsMessage newsMessage = new NewsMessage();
							newsMessage.setToUserName(fromUserName);
							newsMessage.setFromUserName(toUserName);
							newsMessage.setCreateTime(new Date().getTime());
							if (wechatRecovery.getType() == 2) {
								newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
							} else {
								newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
							}
							newsMessage.setArticleCount(articleList.size());
							newsMessage.setArticles(articleList);
							respXml = MessageUtil.messageToXml(newsMessage);
						} else {
							textMessage.setContent(String.valueOf(wechatRecovery.getDescription()));
							respXml = MessageUtil.messageToXml(textMessage);
						}
					} 		
				}
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
//				textMessage.setContent(requestMap.get("Content"));
				QueryItem queryItem = new QueryItem();
				queryItem.setWhere(Where.eq("keywords", requestMap.get("Content")));
				List<SysWechatRecovery> wechatRecoveries = this.getListByEntity(queryItem, Module.SYSTEM, Function.SYS_WECHAT_RECOVERY, SysWechatRecovery.class);
				if (wechatRecoveries != null && wechatRecoveries.size() > 0) {
					SysWechatRecovery wechatRecovery = wechatRecoveries.get(0);
					if (wechatRecovery.getType() == 2) {
						Article article = new Article();
						article.setTitle(String.valueOf(wechatRecovery.getTitle()));
						article.setDescription(String.valueOf(wechatRecovery.getDescription()));

						List<Article> articleList = new ArrayList<Article>();
						articleList.add(article);
						article.setPicUrl(String.valueOf(wechatRecovery.getImg()));
						article.setUrl(String.valueOf(wechatRecovery.getLink()));
						NewsMessage newsMessage = new NewsMessage();
						newsMessage.setToUserName(fromUserName);
						newsMessage.setFromUserName(toUserName);
						newsMessage.setCreateTime(new Date().getTime());
						if (wechatRecovery.getType() == 2) {
							newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
						} else {
							newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
						}
						newsMessage.setArticleCount(articleList.size());
						newsMessage.setArticles(articleList);
						respXml = MessageUtil.messageToXml(newsMessage);
					} else {
						textMessage.setContent(String.valueOf(wechatRecovery.getDescription()));
						respXml = MessageUtil.messageToXml(textMessage);
					}
				} 				
//				else {
//					textMessage.setContent("未找到对应关键字");
//					respXml = MessageUtil.messageToXml(textMessage);
//				}

			}
			// 当用户发消息时
			else {
				textMessage.setContent("请通过菜单使用导航服务！");
				respXml = MessageUtil.messageToXml(textMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respXml;
	}
	
	/**
	 * 微信JS-SDK
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/jsapi", produces = "application/json;charset=utf-8")
	public Map validate(HttpServletRequest request, String url) throws Exception {
		String jsapi_ticket = wechatService.getJsApiTicket();
		Map map = SignUtil.sign(jsapi_ticket, url);
		QueryItem queryItem = new QueryItem(Module.SYSTEM, Function.SYS_WECHAT_SET);
		queryItem.setWhere(Where.eq("name", "AppID"));
		SysWechatSet wechatSet = (SysWechatSet) this.getOne(queryItem, SysWechatSet.class);
		map.put("appId", wechatSet.getValue());
		return map;
	}
	
}
