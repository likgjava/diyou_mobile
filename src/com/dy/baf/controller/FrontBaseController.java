package com.dy.baf.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.controller.BaseController;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.BaseEntity;
import com.dy.core.entity.DateFormatType;
import com.dy.core.service.BaseService;
import com.dy.core.utils.Constant;

public class FrontBaseController extends BaseController {
	
	protected Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	private BaseService baseService;
	
	public DyPhoneResponse successJsonResonse(Object data) {
		DyPhoneResponse response = new DyPhoneResponse();
		response.setCode(DyPhoneResponse.OK);
		response.setData(data);
		response.setResult(DyPhoneResponse.SUCCESS);
		return response;
	}

	public DyPhoneResponse errorJsonResonse(Object errorMsg) {
		DyPhoneResponse response = new DyPhoneResponse();
		response.setCode(DyPhoneResponse.NO);
		response.setDescription(errorMsg);
		response.setResult(DyPhoneResponse.ERROR);
		return response;
	}

	/**
	 * 设置日期转换格式
	 */
	@Override
	protected DateFormatType getDateFormatType() {
		return DateFormatType.DATETIME;
	}
	
	@Deprecated
	public Object getOne(QueryItem queryItem) throws Exception {
		return baseService.getOne(queryItem);
	}
	
	@Deprecated
	public Object getOne(QueryItem queryItem, Class<?> clazz) throws Exception {
		return baseService.getOne(queryItem, clazz);
	}
	
	@Deprecated
	public Object getList(QueryItem queryItem) throws Exception {
		return baseService.getList(queryItem, Map.class);
	}

	@Deprecated
	public Object getList(QueryItem queryItem, Class<?> clazz) throws Exception {
		return baseService.getList(queryItem, clazz);
	}
	
	public Long insert(String module, String function, String pkColumn, BaseEntity baseEntity) throws Exception {
		return baseService.insert(module, function, pkColumn, baseEntity);
	}

	public Integer updateById(String module, String function, BaseEntity baseEntity) throws Exception {
		return baseService.updateById(module, function, baseEntity);
	}
	
	/**
	 * 获取当前登陆用户ID
	 * @return
	 */
	public Long getMemberId() {
		MbMember member = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		return member.getId();
	}
	
	/**
	 * 根据id获取用户
	 * @throws Exception 
	 */
	public MbMember getMember(Long memberId) throws Exception{
		QueryItem userItem = new QueryItem();
		userItem.getWhere().add(Where.eq("id", memberId));
		MbMember user = this.getOneByEntity(userItem, Module.MEMBER, Function.MB_MEMBER, MbMember.class);
		return user;
	}
	
	/**
	 * 存在性校验
	 * @param fieldValue 要校验字段的值
	 * @param viewName 界面显示的名称
	 * @param moudle 模块名
	 * @param function 功能
	 * @param whereList 过滤条件
	 * @return 不存在：null, 存在：错误提示信息(xxx(xxx)已存在)
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public String validateExist(Object fieldValue, String viewName, String moudle, String function, List<Where> whereList) throws Exception {
		if(whereList == null || whereList.size() <= 0) return null;
		
		QueryItem queryItem = new QueryItem();
		queryItem.setWhere(whereList);
		queryItem.setFields("1");
		
		List result = (List)this.getListByMap(queryItem, moudle, function);
		if(result == null || result.size() <= 0) return null;
		
		return this.getMessage("validate.exist", new String[]{viewName, fieldValue.toString()});
	}
	
	/**
	 * 拼接网站访问地址
	 * @param url
	 * @return
	 */
	public String getWebDomain(HttpServletRequest request, String url) {
		String domain = request.getScheme() + "://" + request.getServerName();
		int port = request.getServerPort();
		if (port == 80) {
			domain = domain + request.getContextPath() + "/" + url;
		} else {
			domain = domain + ":" + request.getServerPort() + request.getContextPath() + "/" + url;
		}
		return domain;
	}
	
}