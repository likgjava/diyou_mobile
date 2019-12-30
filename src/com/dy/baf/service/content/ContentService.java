package com.dy.baf.service.content;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.CtAgreement;
import com.dy.baf.entity.common.CtArticles;
import com.dy.baf.entity.common.CtNotice;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.StringUtils;


/**
 * 
 * 
 * @Description: 内容模块
 * @author 波哥
 * @date 2015年9月9日 下午3:23:22 
 * @version V1.0
 */
@Service("mobileContentService")
public class ContentService extends MobileService{

	@Autowired
	private BaseService baseService;
	
	public CtAgreement agreement(String type) throws DyServiceException{
		QueryItem item = new QueryItem(Module.CONTENT,Function.CT_AGREEMENT);
		item.getWhere().add(Where.eq("type", type));
		item.setFields("id,title,contents,upd_time");
		CtAgreement agree = (CtAgreement) baseService.getOne(item,CtAgreement.class);
		return agree;
	}
	
	

	/**
	 * 常见问题数据列表
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Page getArticleList(HttpServletRequest request) throws Exception{
		int pageNum = 1;
		String pageNumStr = request.getParameter("page");
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}
		QueryItem queryItem = new QueryItem(Module.CONTENT,Function.CT_ARTICLES);
		queryItem.setFields("id,category_id,title,summary,add_time,author,upd_time");
		queryItem.setPage(pageNum);
		queryItem.setLimit(5);
		queryItem.getWhere().add(Where.eq("status", 1));
		queryItem.getWhere().add(Where.eq("category_id", 41));
		queryItem.setOrders("sort_index asc,add_time desc");
		Page page = (Page) this.baseService.getPage(queryItem, CtArticles.class);
		return page;
	}
	
	/**
	 * 公告栏列表数据
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Page getNoticeList(HttpServletRequest request) throws Exception {
		Page page = new Page();
		int pageNum = 1;
		String pageNumStr = request.getParameter("page");
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}
		QueryItem queryItem = new QueryItem(Module.CONTENT,Function.CT_NOTICE);
		queryItem.setFields("id,title,summary,contents,add_time,sort_index,status,upd_time");
		queryItem.setPage(pageNum);
		queryItem.setLimit(5);
		queryItem.setOrders("sort_index asc,add_time desc");
		queryItem.setWhere(Where.eq("status", 1));
		page = (Page) this.baseService.getPage(queryItem, CtNotice.class);
		return page;
	}
	
	
	/**
	 * 公告详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse noticeDetail(String id) throws Exception{
		QueryItem queryItem = new QueryItem(Module.CONTENT,Function.CT_NOTICE);
		queryItem.getWhere().add(Where.eq("id", Long.valueOf(id)));
		CtNotice notice = this.baseService.getOne(queryItem, CtNotice.class);
		return successJsonResonse(notice);
	}
	
	/**
	 * 问题详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse articlesDetail(String id) throws Exception{
		QueryItem queryItem = new QueryItem(Module.CONTENT,Function.CT_ARTICLES);
		queryItem.getWhere().add(Where.eq("id", Long.valueOf(id)));
		CtArticles notice = this.baseService.getOne(queryItem, CtArticles.class);
		return successJsonResonse(notice);
	}
}
