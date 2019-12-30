package com.dy.baf.service.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMessage;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.dml.DmlItem;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.NameValue;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * @Description: TODO
 * @author 波哥
 * @date 2015年9月13日 下午2:28:17 
 * @version V1.0
 */
@Service("mobileMessageService")
public class MessageService extends MobileService {

	@Autowired
	private BaseService baseService;
	
	/**
	 * 用户站内信列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse messageLog(Integer page,String memberId) throws Exception {
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("用户登录标识不能为空");
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		QueryItem queryItem = new QueryItem(Module.MEMBER,Function.MB_MESSAGE);
		queryItem.setPage(page == null ? 1 : page);
		queryItem.setOrders("add_time desc");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.notEq("status", -1));
		whereList.add(Where.eq("member_id", user.getId()));
		queryItem.setWhere(whereList);
		queryItem.setLimit(10);
		Page pageObject = this.baseService.getPage(queryItem, Map.class);
		return successJsonResonse(pageObject);
	}
	
	/**
	 * 改变已读未读状态
	 */
	public DyPhoneResponse messageViewed(Long id) throws Exception {
		QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_MESSAGE);
		queryItem.setFields("status,contents,title,add_time");
		queryItem.setWhere(new Where("id", id));
		MbMessage message = this.baseService.getOne(queryItem,MbMessage.class);
		
		if(message != null && message.getStatus() != 2) {
			DmlItem dmlItem = new DmlItem();
			dmlItem.setModule(Module.MEMBER) ;
			dmlItem.setFunction(Function.MB_MESSAGE) ;
			List<NameValue> params = new ArrayList<NameValue>();
			params.add(new NameValue("status", 2));
			params.add(new NameValue("veiw_time", DateUtil.getCurrentTime()));
			dmlItem.setParams(params);
			List<Where> whereList = new ArrayList<Where>();
			whereList.add(new Where("id", id));
			dmlItem.setWhereList(whereList);
			this.baseService.update(dmlItem);
		}
		
		Map<String, Object> description = new HashMap<String, Object>(); 
		description.put("status", 2);
		description.put("contents", message == null ? null : message.getContents());
		description.put("add_time",message == null ? null : message.getAddTime()) ;
		description.put("title", message == null ? null :message.getTitle()) ;
		return successJsonResonse(description);
	}
	
	/**
	 * 删除站内信
	 */
	public DyPhoneResponse messagedel(String[] id) throws Exception {
		if(id == null)
			return errorJsonResonse("没有选择站内信息");
		
		StringBuffer ids = new StringBuffer();
		for(String temp : id) {
			ids.append(",").append(temp);
		}
		DmlItem dmlItem = new DmlItem();
		List<NameValue> params = new ArrayList<NameValue>();
		params.add(new NameValue("status", -1));
		params.add(new NameValue("del_time", DateUtil.getCurrentTime()));
		dmlItem.setParams(params);
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.in("id", ids.toString().substring(1)));
		dmlItem.setWhereList(whereList);
		this.baseService.update(dmlItem);
		
		return successJsonResonse("删除成功!");
	}
	
	/**
	 * 获取用户站内信条数
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse messageCount(String memberId) throws Exception {
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("用户登录标识不能为空");
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		
		QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_MESSAGE);
		queryItem.setFields("count(1) msgnum");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("member_id", user.getId()));
		queryItem.setWhere(whereList);
		Map<String, Object> map = this.baseService.getOne(queryItem);
		
		return successJsonResonse(Collections.singletonMap("msgNum", Integer.parseInt(map.get("msgnum").toString())));
	}
}