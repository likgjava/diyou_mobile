package com.dy.baf.service.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.SysAppInfo;
import com.dy.baf.entity.common.SysSystemAreas;
import com.dy.baf.entity.custom.Tender;
import com.dy.baf.entity.custom.TenderCondition;
import com.dy.baf.entity.custom.TenderDetail;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.RepayUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 公共信息
 * @author 波哥
 * @date 2015年9月9日 下午6:55:23
 * @version V1.0
 */
@Service("mobileCommonService")
public class CommonService extends MobileService{

	@Autowired
	private BaseService baseService;

	/**
	 * 省份、市区信息
	 * @return
	 * @throws DyServiceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> getProvinceCity() throws DyServiceException {
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_AREAS);
		item.getWhere().add(Where.eq("status", 1));
		item.setOrders("id,sort_index");
		item.setFields("id,name,nid,pid,province,city");
		List<Map> cityList = (List<Map>) baseService.getList(item);
		List<Map> allMap = new ArrayList<Map>();
		if (cityList != null && cityList.size() > 0) {
			List<Map> areasMap;
			for (Map provinceMap : cityList) {
				if (provinceMap.get("pid").equals(0)) {
					areasMap = new ArrayList();
					for (Map cityMap : cityList) {
						if (cityMap.get("pid").equals(provinceMap.get("id"))) {

							areasMap.add(cityMap);
						}
					}
					provinceMap.put("city_list", areasMap);
					allMap.add(provinceMap);
				}
			}
		}
		return allMap;
	}
	
	/**
	 * 省份信息
	 * @return
	 * @throws DyServiceException
	 */
	public List<SysSystemAreas> getProvince() throws DyServiceException{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_AREAS);
		item.getWhere().add(Where.notIn("id", "1,21,40,61"));
		item.getWhere().add(Where.in("pid", "0,1,21,40,61"));
		item.getWhere().add(Where.eq("status", 1));
		item.setOrders("id,sort_index");
		item.setFields("id,name,nid,pid,province,city");
		List<SysSystemAreas> cityList = baseService.getList( item,SysSystemAreas.class);
		return cityList;
	}
	
	
	/**
	 * 城市信息
	 * 
	 * @param pid
	 * @return
	 */
	public List<SysSystemAreas>  getCity(String pid) throws DyServiceException{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_AREAS);
		item.getWhere().add(Where.eq("pid", pid));
		item.getWhere().add(Where.eq("status", 1));
		item.setOrders("sort_index");
		item.setFields("id,name,nid,pid,province,city");
		List<SysSystemAreas> cityList = baseService.getList( item,SysSystemAreas.class);
		return cityList;
	}
	
	/**
	 * 城市信息
	 * 
	 * @param pid
	 * @return
	 */
	public List<SysSystemAreas>  getArea(String cid) throws DyServiceException{
		QueryItem item = new QueryItem(Module.SYSTEM, Function.SYS_AREAS);
		item.getWhere().add(Where.eq("pid", cid));
		item.getWhere().add(Where.eq("status", 1));
		item.setOrders("sort_index");
		item.setFields("id,name,nid,pid,province,city");
		List<SysSystemAreas> cityList = baseService.getList( item,SysSystemAreas.class);
		return cityList;
	}
	
	public DyPhoneResponse updateVersion(String version,String phone_type) throws Exception{
		QueryItem queryItem = new QueryItem(Module.SYSTEM,Function.SYS_APPINFO);
		
		String type = "1".equals(phone_type)?"andriod":("2".equals(phone_type)?"ios":"");
		if(StringUtils.isBlank(type)){
			return errorJsonResonse("手机类型错误");
		}
		
		
		queryItem.setWhere(new Where("type",type));
		queryItem.setWhere(new Where("status",1));
		
		SysAppInfo appInfo = this.baseService.getOne(queryItem, SysAppInfo.class);
		if(appInfo == null){
			return errorJsonResonse("已经是最新的版本了");
		}else{
			
			Map<String,Object> mapResonse = new HashMap<String, Object>();
			if(version.compareTo(appInfo.getVersion()) < 0){
				if("v".equalsIgnoreCase(appInfo.getVersion().substring(0,1))){
					mapResonse.put("version", appInfo.getVersion().substring(1));
				}else{
					mapResonse.put("version", appInfo.getVersion());
				}
				
				mapResonse.put("url", appInfo.getZip());
				return successJsonResonse(mapResonse);
			}else{
				//return errorJsonResonse("已经是最新的版本了");
				/**
				 * 安卓和ios返回格式不一样
				 */
				if("andriod".equals(type)){
					return errorJsonResonse("已经是最新的版本了");
				}else{
					return successJsonResonse("已经是最新的版本了");
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println("v1.0.2".compareTo("v1.0.3") == -1);
	}
	
	/**
	 * 计算器
	 * @param responseMap
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse calculator1(Map<String,String> responseMap) throws Exception{
		TenderCondition condition = new TenderCondition() ;
		String amount = responseMap.get("amount") ;
		if(!StringUtils.isMoney(amount)){
			return errorJsonResonse("请输入正确的金额！") ;
		}
		String apr = responseMap.get("apr") ;
		if(!StringUtils.isMoney(apr)){
			return errorJsonResonse("请输入正确的利率!");
		}
		String period = responseMap.get("period") ;
		if(!StringUtils.isNumeric(period)){
			return errorJsonResonse("请输入正确的期限!");
		}
		String repayType= responseMap.get("repay_type") ;
		if(StringUtils.isBlank(repayType)){
			return errorJsonResonse("请输入正确的还款方式!");
		}
		condition.setAmount(new BigDecimal(amount)) ;
		condition.setApr(new BigDecimal(apr)) ;
		condition.setPeriod(Integer.valueOf(period)) ;
		condition.setRepayType(Integer.valueOf(repayType)) ;
		Tender tender = RepayUtil.getRepayInfo(condition);
		//计算月利率 利息/本金/月
//		BigDecimal monthApr = NumberUtils.div(NumberUtils.div(tender.getInterestAll(), tender.getPrincipalAll()), new BigDecimal(times)) ;
		BigDecimal monthApr = NumberUtils.div(new BigDecimal(apr),new BigDecimal(12));
		Map<String,Object> datas = new HashMap<String, Object>() ;
		datas.put("month_apr", monthApr ) ;
		datas.put("amount_total", tender.getAmountAll()) ;
		List<TenderDetail> repayList = tender.getRepayDetailList() ;
		datas.put("repay_month", repayList.get(0).getAmount()) ;
		datas.put("interest_total", tender.getInterestAll()) ;
		return successJsonResonse(datas);
	}
}
