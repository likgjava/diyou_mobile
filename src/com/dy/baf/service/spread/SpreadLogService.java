package com.dy.baf.service.spread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.Page;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DataConvertUtil;
import com.dy.core.utils.OptionUtil;
import com.dy.core.utils.StringUtils;

/**
 * 我的推广记录
 * @author Administrator
 *
 */
@Service("mobileSpreadLogService")
public class SpreadLogService extends MobileService{

	@Autowired
	private BaseService baseService;
	@Autowired
	private OptionUtil optionUtil;
	
	/**
	 * 推广记录列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse getSpreadLog(String memberId,String name,String page,String epage) throws Exception{
		MbMember user = getMbMember(Long.valueOf(memberId));
		QueryItem item = new QueryItem(Module.MEMBER, Function.MB_SPREADLOG);
		item.setPage(page == null ? 1: Integer.valueOf(page));
		item.setOrders("add_time desc");
		item.setLimit(epage == null ? 10 : Integer.valueOf(epage));
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("member_id", user.getId()));
		if(StringUtils.isNotBlank(name)){
			whereList.add(Where.eq("spreaded_member_name", name));
		}
		item.setWhere(whereList);
		Page pageObj = this.baseService.getPage(item,Map.class);
		List<Map<String,Object>> list = pageObj.getItems();
		for(Map<String,Object> map : list){
			map.put("proportion", map.get("proportion").toString() + "%");
		}
		return successJsonResonse(new DataConvertUtil(pageObj).setStatus("spread_type", optionUtil.getLogSpreadType()).setStatus("amount_type", optionUtil.getLogAmountType()).setDate("add_time", "yyyy-MM-dd").convert());
	}
}
