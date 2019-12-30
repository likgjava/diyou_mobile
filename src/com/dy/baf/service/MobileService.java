package com.dy.baf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.SysSystemConfig;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.Condition;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.HttpInvokerUtil;
import com.dy.core.utils.sms.SmsUtil;

public class MobileService {
	//查询条件
	public static final String EQ = Condition.EQ;
	public static final String GT = Condition.GT;
	public static final String GE = Condition.GE;
	public static final String LT = Condition.LT;
	public static final String LE = Condition.LE;
	public static final String IN = Condition.IN;
	public static final String NEQ = Condition.NEQ;
	public static final String LIKE = Condition.LIKE;
	public static final String NOT_IN = Condition.NOT_IN;
	public static final String LIKE_ALL = Condition.LIKE_ALL;
	public static final String NULL = Condition.NULL;
	public static final String NOT_NULL = Condition.NOT_NULL;
	
	@Autowired
	private BaseService baseService;
	@Autowired
	protected SmsUtil smsUtil;
	@Autowired
	private HttpInvokerUtil httpInvokerUtil;
	
	public DyPhoneResponse successJsonResonse(Object data) {
		DyPhoneResponse response = new DyPhoneResponse();
		response.setCode(DyPhoneResponse.OK);
		response.setData(data);
		response.setDescription(data);
		response.setResult(DyPhoneResponse.SUCCESS);
		return response;
	}
	
	public DyPhoneResponse successJsonResonse(int code, Object data) {
		DyPhoneResponse response = new DyPhoneResponse();
		response.setCode(code);
		response.setData(data);
		response.setDescription(data);
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
	
	public MbMember getMbMember(Long memberId) throws DyServiceException{
		QueryItem userItem = new QueryItem(Module.MEMBER, Function.MB_MEMBER);
		userItem.getWhere().add(Where.eq("id", memberId));
		MbMember user = this.baseService.getOne(userItem, MbMember.class);
		return user;
	}
	
	/**
	 * 根据nid获取系统配置
	 * @throws DyServiceException 
	 * @throws Exception 
	 */
	protected String getSysValue(String nid) throws DyServiceException {
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_CONFIG);
		item.getWhere().add(Where.eq("nid", nid));
		item.setFields("value");
		SysSystemConfig config = this.baseService.getOne(item, SysSystemConfig.class);
		return config == null ? "": config.getValue();
	}
	/**
	 * 查询单条记录
	 * @return 返回Entity对象
	 */
	protected Object getOne(String module,String function,QueryItem item,Class<?> clazz) throws DyServiceException{
		item.setModule(module) ;
		item.setFunction(function) ;
		return this.baseService.getOne(item, clazz) ;
	}
	/**
	 * 查询单条记录
	 * @return 返回Map对象
	 */
	protected Map<String,Object> getOne(String module,String function,QueryItem item) throws DyServiceException{
		item.setModule(module) ;
		item.setFunction(function) ;
		return this.baseService.getOne(item) ;
	}
	
	/**
	 * 查询列表，返回List<BaseEntity>
	 * @param queryItem
	 * @param module
	 * @param function
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getListByEntity(QueryItem queryItem, String module, String function, Class<T> clazz) throws Exception {
		return httpInvokerUtil.getList(queryItem, module, function, clazz);
	}
	/**
	 * 查询单条记录，返回BaseEntity
	 * @param queryItem
	 * @param module
	 * @param function
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public <T> T getOneByEntity(QueryItem queryItem, String module, String function, Class<T> clazz) throws Exception {
		return httpInvokerUtil.getOne(queryItem, module, function, clazz);
	}
	/**
	 * 查询列表，返回Page<Map>
	 * @param queryItem
	 * @param module
	 * @param function
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Page<Map> getPageByMap(QueryItem queryItem, String module, String function) throws Exception {
		return httpInvokerUtil.getPage(queryItem, module, function);
	}
	
	/**
	 * 添加where条件到whereList
	 * @param whereList
	 * @param column 数据库字段名
	 * @param value 值
	 */
	public void addWhereCondition(List<Where> whereList, String column, Object value) throws Exception {
		this.addWhereCondition(whereList, column, EQ, value, true);
	}
	
	/**
	 * 添加where条件到whereList
	 * @param whereList
	 * @param column 数据库字段名
	 * @param condition 查询条件(=,>,<,>等)
	 * @param value 值
	 */
	public void addWhereCondition(List<Where> whereList, String column, String condtion, Object value) throws Exception {
		this.addWhereCondition(whereList, column, condtion, value, true);
	}
	
	/**
	 * 添加where条件到whereList
	 * @param whereList
	 * @param column 数据库字段名
	 * @param condition 查询条件(=,>,<,>等)
	 * @param value 值
	 * @param isDateNeedCovert 时间是否需要转为long，默认为true
	 */
	public void addWhereCondition(List<Where> whereList, String column, String condtion, Object value, boolean isDateNeedCovert) throws Exception {
		if(StringUtils.isEmpty(column) || value == null) return;
		if(value instanceof String && StringUtils.isEmpty(value.toString())) return;
		
		if(whereList == null) whereList = new ArrayList<Where>();
		if(isDateNeedCovert && value instanceof Date) value = DateUtil.convert((Date) value);
		whereList.add(new Where(column, value, condtion));
	}
	
	/**
	 * 添加and条件，如：add_time > startValue and add_time < endValue
	 * @param whereList
	 * @param column 数据库字段名
	 * @param startValue
	 * @param endValue
	 */
	public void addAndWhereCondition(List<Where> whereList, String column, Object startValue, Object endValue) throws Exception {
		this.addAndWhereCondition(whereList, column, startValue, endValue, true);
	}
	
	/**
	 * 添加and条件，如：add_time > startValue and add_time < endValue
	 * @param whereList
	 * @param column 数据库字段名
	 * @param startValue
	 * @param endValue
	 * @param formatType 格式化类型(date/datetime),默认为date
	 */
	public void addAndWhereCondition(List<Where> whereList, String column, Object startValue, Object endValue, String formatType) throws Exception {
		this.addAndWhereCondition(whereList, column, startValue, endValue, true);
	}
	
	/**
	 * 添加and条件，如：add_time > startValue and add_time < endValue
	 * @param whereList
	 * @param column 数据库字段名
	 * @param startValue
	 * @param endValue
	 * @param isDateNeedCovert 时间是否需要转为long，默认为true
	 */
	public void addAndWhereCondition(List<Where> whereList, String column, Object startValue, Object endValue, boolean isDateNeedCovert) throws Exception {
		this.addAndWhereCondition(whereList, column, startValue, endValue, isDateNeedCovert, "date");
	}
	
	/**
	 * 添加and条件，如：add_time > startValue and add_time < endValue
	 * @param whereList
	 * @param column 数据库字段名
	 * @param startValue
	 * @param endValue
	 * @param isDateNeedCovert 时间是否需要转为long，默认为true
	 * @param formatType 格式化类型(date/datetime),默认为date
	 */
	public void addAndWhereCondition(List<Where> whereList, String column, Object startValue, Object endValue, boolean isDateNeedCovert, String formatType) throws Exception {
		if(StringUtils.isEmpty(column)) return;
		
		if(whereList == null) whereList = new ArrayList<Where>();
		
		List<NameValue> ands = new ArrayList<NameValue>();
		if(startValue != null) {
			if(isDateNeedCovert && startValue instanceof Date) {
				if("date".equals(formatType)) {
					String startDate = DateUtil.dateFormat((Date) startValue) + " 00:00:00";
					startValue = DateUtil.convert(startDate);
				} else {
					startValue = DateUtil.convert((Date) endValue);
				}
			}
			ands.add(new NameValue(column, startValue, GE));
		}
		if(endValue != null) {
			if(isDateNeedCovert && endValue instanceof Date) {
				if("date".equals(formatType)) {
					String endDate = DateUtil.dateFormat((Date) endValue) + " 23:59:59";
					endValue = DateUtil.convert(endDate);
				} else {
					endValue = DateUtil.convert((Date) endValue);
				}
			}
			ands.add(new NameValue(column, endValue, LE));
		}
		if(ands.size() > 0) whereList.add(new Where(ands));
	}
	
	/**
	 * DyResponse 转 DyPhoneResponse
	 * @param response
	 * @return
	 */
	public DyPhoneResponse pcTurnApp(DyResponse response){
		DyPhoneResponse phoneResponse = new DyPhoneResponse();
		if(response.getStatus() ==  response.OK){
			phoneResponse.setCode(DyPhoneResponse.OK);
			phoneResponse.setResult(DyPhoneResponse.SUCCESS);
			phoneResponse.setData(response.getData());
			phoneResponse.setDescription(response.getDescription());
		}else{
			phoneResponse.setCode(DyPhoneResponse.NO);
			phoneResponse.setResult(DyPhoneResponse.ERROR);
			phoneResponse.setDescription(response.getDescription());
		}
		return phoneResponse;
	}
}
