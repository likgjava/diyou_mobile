package com.dy.baf.controller.wap.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.FriendsLink;
import com.dy.baf.entity.Partner;
import com.dy.baf.entity.Recommend;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.CtArticles;
import com.dy.baf.entity.common.CtLinks;
import com.dy.baf.entity.common.SysSystemSitepage;
import com.dy.baf.service.content.ContentService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 内容模块
 * @author 波哥
 * @date 2015年9月9日 下午6:34:53 
 * @version V1.0
 */

@Controller(value="wapContentController")
public class ContentController extends WapBaseController {
	
	protected Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	private ContentService contentService;
	
	/**
	 * 常见问题页面
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/articles/articles")
	public ModelAndView articleIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/articlesList.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 常见问题页面（APP专用）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/articles/appArticles")
	public ModelAndView appArticles() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/appArticlesList.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	/**
	 * 常见问题数据列表
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping(value="/articles/articleslist", method=RequestMethod.POST)
	public Page getArticleList(HttpServletRequest request){
		try {
			return this.contentService.getArticleList(request);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 公告栏列表页面
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/notice")
	public ModelAndView noticeIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/noticeList.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 公告栏列表页面（App专用）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/appNotice")
	public ModelAndView appNoticeIndex() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/appNoticeList.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 公告栏列表数据
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/articles/noticelist", method=RequestMethod.POST)
	public Page getNoticeList(HttpServletRequest request) {
		try {
			return this.contentService.getNoticeList(request);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 公告详情页面
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/noticeDetailPage")
	public ModelAndView noticeDetailPage() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/noticeDetail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 公告详情
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/articles/noticeDetail", method=RequestMethod.POST)
	public DyPhoneResponse noticeDetail(String id) {
		try {
			return this.contentService.noticeDetail(id);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	
	/**
	 * 问题详情页面
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/articlesDetailPage")
	public ModelAndView articlesDetailPage() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/articlesDetail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 问题详情
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/articles/articlesdetail", method=RequestMethod.POST)
	public DyPhoneResponse articlesDetail(String id) {
		try {
			return this.contentService.articlesDetail(id);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 网站服务协议
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/index/agreement")
	public ModelAndView agreement() {
		ModelAndView view = new ModelAndView();
		try {
			String type = RequestUtil.getString(this.getRequest(), "type", "reg") ;
			SystemInfo system = new SystemInfo();
			system.setContentPage("common/agreement.jsp");
			view = this.initIndexPageView(system);
			view.addObject("agreement",this.contentService.agreement(type));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	
	
	/**
	 * 问题详情页面（app）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/apparticlesdetail")
	public ModelAndView apparticlesdetail() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/appArticlesDetail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	

	/**
	 * 公告详情页面（app）
	 * @return
	 */
	@ResponseBody
	@RequestMapping("articles/appnoticedetail")
	public ModelAndView appNoticeDetail() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("articles/appNoticeDetail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	
	/**
	 * 注册协议
	 * @return
	 */
	@RequestMapping(value="/index/appAgreement", method=RequestMethod.GET)
	public ModelAndView appAgreement() {
		ModelAndView view = new ModelAndView();
		try {
			
			SystemInfo system = new SystemInfo();
			system.setContentPage("system/agreement.jsp");
			CtAgreement agreement =  this.contentService.agreement("reg");
			view = this.initCommonPageView(system);
			view.addObject("agreement", agreement);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 友情链接
	 */
	@ResponseBody
	@RequestMapping(value="/friendslink", method=RequestMethod.POST)
	public DyResponse getFriendsLink() {
		QueryItem queryItem = new QueryItem();
		queryItem.setFields("id,category_id,name,summary,jumpurl,logo");
		queryItem.getWhere().add(Where.eq("status", 1));
		queryItem.getWhere().add(Where.eq("category_id", 1));
		queryItem.setOrders("sort_index asc");
		try {
			List<CtLinks> ctLinkList = this.getListByEntity(queryItem, Module.CONTENT, Function.CT_LINKS, CtLinks.class);
			List<FriendsLink> friendsLinkList = new ArrayList<FriendsLink>();
			if (ctLinkList != null && ctLinkList.size() > 0) {
				FriendsLink friendsLink = null;
				for (CtLinks ctLinks : ctLinkList) {
					friendsLink = new FriendsLink();
					friendsLink.setJumpUrl(ctLinks.getJumpurl());
					friendsLink.setName(ctLinks.getName());
					friendsLink.setLogo(PropertiesUtil.getImageHost() + ctLinks.getLogo());
					friendsLinkList.add(friendsLink);
				}
			}
			return createSuccessJsonResonse(friendsLinkList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createErrorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 合作伙伴
	 */
	@ResponseBody
	@RequestMapping(value="/partners", method=RequestMethod.POST)
	public DyResponse getPartners() {
		QueryItem pQueryItem = new QueryItem();
		pQueryItem.setFields("id,category_id,name,summary,jumpurl,logo");
		pQueryItem.getWhere().add(Where.eq("status", 1));
		pQueryItem.getWhere().add(Where.eq("category_id", 2));
		pQueryItem.setOrders("sort_index asc");
		try {
			List<CtLinks> ctLinkList = this.getListByEntity(pQueryItem, Module.CONTENT, Function.CT_LINKS, CtLinks.class);
			List<Object[]> partnerList = new ArrayList<Object[]>();
			int num = 0;
			int size = ctLinkList.size();
			if (ctLinkList != null && ctLinkList.size() > 0) {
				Object[] partners=new Object[10];
				Partner partner = null;
				for (CtLinks ctLinks : ctLinkList) {
					partner = new Partner();
					partner.setJumpUrl(ctLinks.getJumpurl());
					partner.setName(ctLinks.getName());
					partner.setLogo(PropertiesUtil.getImageHost() + ctLinks.getLogo());
					partners[num] = partner;
					if(size<10 && size-1 == num){
						partnerList.add(partners);
					}else if(num!=0 && num%10==0){
						partnerList.add(partners);
					}					
					num++;
					if(num==10){
						num = 1;
					}
				}
			}
			return createSuccessJsonResonse(partnerList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.createErrorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 网站协议
	 * @throws Exception 
	 */
	@RequestMapping(value="/agreement/getone", method=RequestMethod.POST)
	public ModelAndView getone(String type,Model model) {
		ModelAndView view = new ModelAndView();
		try {
			QueryItem item = new QueryItem();
			item.getWhere().add(Where.eq("type", type));
			CtAgreement agree = this.getOneByEntity(item, Module.CONTENT, Function.CT_AGREEMENT, CtAgreement.class);
			String contents =  agree.getContents();
			model.addAttribute("contents", contents);
			SystemInfo system = new SystemInfo("content/agreement/reg_agreement.jsp");
			view = this.initIndexPageView(system);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 美银动态
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/mydtarticles", method=RequestMethod.POST)
	public DyResponse mydtArticles() {
		List<Recommend> recommendList = new ArrayList<Recommend>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "mydt"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());
					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);			
					recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	/**
	 * 美银头条
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/myttarticles", method=RequestMethod.POST)
	public DyResponse myttArticles() {
		List<Recommend> recommendList = new ArrayList<Recommend>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "mytt"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());
					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);			
					recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	/**
	 * 美银社区
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/mysqarticles", method=RequestMethod.POST)
	public DyResponse mysqArticles() {
		List<Recommend> recommendList = new ArrayList<Recommend>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "mysq"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());
					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);			
					recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	/**
	 * 美银先知
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/myxzarticles", method=RequestMethod.POST)
	public DyResponse myxzArticles() {
		List<Recommend> recommendList = new ArrayList<Recommend>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "myxz"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());
					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);			
					recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	/**
	 * 宣传片
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/xcparticles", method=RequestMethod.POST)
	public DyResponse xcpArticles() {
		List<Recommend> recommendList = new ArrayList<Recommend>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "xcp"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());
					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);			
					recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	
	/**
	 * 平台荣誉
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/articles/ptryarticles", method=RequestMethod.POST)
	public DyResponse ptryArticles() {
		List<Object[]> recommendList = new ArrayList<Object[]>();
		try {
			QueryItem queryItem = new QueryItem("system", "sitepage");
			queryItem.setFields("id,pid,name,router,type,description");
			queryItem.getWhere().add(Where.eq("status", 1));
			queryItem.getWhere().add(Where.eq("nid", "ptry"));
			queryItem.setOrders("sort_index asc");
			List<SysSystemSitepage> list = (List<SysSystemSitepage>) this.getList(queryItem, SysSystemSitepage.class);
			
			List<Object[]> ptryarticlesList = new ArrayList<Object[]>();
			int num = 0;
			int size = list.size();
			if (list != null && list.size() > 0) {
				Recommend recommend = null;
				for (SysSystemSitepage sitepage : list) {
					
					Object[] recommends=new Object[10];
					
					recommend = new Recommend();
					recommend.setId(sitepage.getId());
					recommend.setName(sitepage.getName());
					recommend.setRouter(sitepage.getRouter());
					recommend.setAddTime(sitepage.getAddTime());					
					QueryItem pageQueryItem = new QueryItem("content", "articles");
					
					//pageQueryItem.setLimit(5);
					pageQueryItem.setFields("id,category_id,title,summary,add_time,author,concat('" + PropertiesUtil.getImageHost()+ "'[c]thumbs) thumbs,contents");
					pageQueryItem.getWhere().add(Where.eq("status", 1));
					pageQueryItem.getWhere().add(Where.eq("category_id", sitepage.getId()));
					pageQueryItem.setOrders("sort_index asc,add_time desc");
					List<CtArticles> arList = (List<CtArticles>) super.getList(pageQueryItem, CtArticles.class);
					recommend.setArticleList(arList);		
					
					recommends[num] = recommend;
					if(size<10 && size-1 == num){
						recommendList.add(recommends);
					}else if(num!=0 && num%10==0){
						recommendList.add(recommends);
					}					
					num++;
					if(num==10){
						num = 1;
					}
					//recommendList.add(recommend);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return createSuccessJsonResonse(recommendList);
	}
	
}