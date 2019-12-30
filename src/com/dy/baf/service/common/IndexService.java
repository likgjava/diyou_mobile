package com.dy.baf.service.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.common.FnLoanInfo;
import com.dy.baf.entity.common.FnLoanPreview;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.NumberUtils;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;

/**
 * 
 * 
 * @Description: 首页信息
 * @author 波哥
 * @date 2015年9月9日 下午6:55:23
 * @version V1.0
 */
@Service("mobileIndexService")
public class IndexService {

	@Autowired
	private BaseService baseService;

	/**
	 * 首页banner
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> banner() throws Exception{
		String imageUrl = PropertiesUtil.getImageHost();
		QueryItem bannerQuseryItem = new QueryItem(Module.CONTENT, Function.CT_ADVERT);
		bannerQuseryItem.setWhere(new Where("category_id", "2"));
		bannerQuseryItem.setWhere(new Where("status", "1"));
		bannerQuseryItem.setOrders("sort_index");
		bannerQuseryItem.setFields("image,jumpurl");
		
		List<Map> bannerList = (List<Map>) this.baseService.getList(bannerQuseryItem);
		for (Map map : bannerList) {
			map.put("image", imageUrl+map.get("image"));
		}
		return bannerList ;
	}
	
	
	/**
	 * 首页标
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> loan() throws Exception{
		
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		loanItem.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_type,category_id category_name");
		List<Where> whereList = new ArrayList<Where>();
		
		//查是否有满足条件新手标
		whereList.add(Where.expression("(status = 3 and (overdue_time = 0 or overdue_time > " + DateUtil.getCurrentTime() + "))", false));
		whereList.add(new Where("repay_type",3,"="));
		whereList.add(new Where("additional_status",1,"="));
		whereList.add(new Where("progress",100,"<"));
		whereList.add(Where.notEq("member_id", -1));
		loanItem.setWhere(whereList);
		loanItem.setOrders("id desc");
		loanItem.setLimit(1);
		
		List<Map> newLoan = (List<Map>)this.baseService.getList(loanItem);
		if(null == newLoan || 0 == newLoan.size()){
			QueryItem ordinaryLoan = new QueryItem(Module.LOAN, Function.LN_LOAN);
			ordinaryLoan.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_type,category_id category_name");
			List<Where> whereOrdinaryList = new ArrayList<Where>();
			whereOrdinaryList.add(Where.expression("(status = 3 and (overdue_time = 0 or overdue_time > " + DateUtil.getCurrentTime() + "))", false));
			whereOrdinaryList.add(new Where("repay_type",3,"="));
			whereOrdinaryList.add(Where.expression("(case when category_type=3 then status=2 else status=3 end)", false));
			whereOrdinaryList.add(new Where("progress",100,"<"));
			whereOrdinaryList.add(Where.notEq("member_id", -1));
			ordinaryLoan.setWhere(whereOrdinaryList);
			ordinaryLoan.setOrders("id desc");
			ordinaryLoan.setLimit(1);
			List<Map> newOrdinaryLoan = (List<Map>)this.baseService.getList(ordinaryLoan);
			
			if(null == newOrdinaryLoan || 0 == newOrdinaryLoan.size()){
				QueryItem queryTopOne = new QueryItem(Module.LOAN, Function.LN_LOAN);
				queryTopOne.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_type,category_id category_name");
				queryTopOne.setWhere(Where.expression("((case when status = 3 then (overdue_time >="+ DateUtil.getCurrentTime() +") else (1=1) end) or overdue_time = 0 or status = 4)", false));
				queryTopOne.setWhere(new Where("status",-7,"!="));
				queryTopOne.setWhere(new Where("status",2,">"));
				queryTopOne.setWhere(new Where("hidden_status",1));
				queryTopOne.setWhere(new Where("member_id",-1,"!="));
				queryTopOne.setOrders("status asc,additional_status desc,progress asc,add_time desc");
				queryTopOne.setLimit(1);
				
				return (List<Map>)this.baseService.getList(queryTopOne);
			}else{
				return newOrdinaryLoan;
			}
		}else{
			return newLoan;
		}
	}
	
	/**
	 * 首页新手标
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> newHandLoan() throws Exception{
		
		QueryItem loanItem = new QueryItem(Module.LOAN, Function.LN_LOAN);
		loanItem.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_id,category_type,category_id category_name,marker_type,hidden_status,marker_type,additional_status");
		List<Where> whereList = new ArrayList<Where>();
		
		//查是否有满足条件新手标
		whereList.add(Where.expression("(status = 3 and (overdue_time = 0 or overdue_time > " + DateUtil.getCurrentTime() + "))", false));
		whereList.add(new Where("repay_type",3,"="));
		whereList.add(new Where("additional_status",1,"="));
		whereList.add(new Where("progress",100,"<"));
		whereList.add(new Where("hidden_status",1));
		whereList.add(Where.notEq("member_id", -1));
		loanItem.setWhere(whereList);
		loanItem.setOrders("id desc");
		loanItem.setLimit(1);		
		List<Map> newLoan = (List<Map>)this.baseService.getList(loanItem);
		
		//查询流转标类型
		QueryItem loanCategoryItem = new QueryItem(Module.LOAN, Function.LN_CATEGORY);//借款标类型
		loanCategoryItem.setFields("id,is_roam,pic,nid");
		List<Map> cateList = (List<Map>) this.baseService.getList(loanCategoryItem);
		
		for(Map map:newLoan){
			QueryItem liQueryItem = new QueryItem(Module.LOAN, Function.LN_LOANINFO);
			liQueryItem.setFields("id,contents");
			liQueryItem.setWhere(Where.eq("loan_id", map.get("id")));
			FnLoanInfo loanInfo = this.baseService.getOne(liQueryItem,FnLoanInfo.class);
			
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();	
			String url = this.getWebDomain(request,"wap/loan/appinfoview#?id=");
			map.put("url", url+map.get("id"));
			map.put("id", map.get("id").toString());	//统一转换成字符串类型
			
			//分享信息
			map.put("share_url", url+map.get("id"));
			map.put("share_content", loanInfo.getContents());
			map.put("share_title",map.get("name"));
			
			int  categoryId= (Integer) map.get("category_id");
			DecimalFormat df =new DecimalFormat("######0.00");
			map.put("progress",df.format(map.get("progress")));
			map.put("apr",df.format(map.get("apr")) );
			for(Map categoryMap:cateList){
				if(Integer.valueOf(categoryMap.get("id").toString())==categoryId){					
					map.put("toDayAndMonth", categoryMap.get("nid").equals("day")?map.get("period")+"天":map.get("period")+"个月");
				};
			}
		}
		
		return newLoan;
	}
	
	/**
	 * 首页借款标
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> newLoan() throws Exception{		
			QueryItem ordinaryLoan = new QueryItem(Module.LOAN, Function.LN_LOAN);
			ordinaryLoan.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_type,category_id category_name");
			List<Where> whereOrdinaryList = new ArrayList<Where>();
			whereOrdinaryList.add(Where.expression("(status = 3 and (overdue_time = 0 or overdue_time > " + DateUtil.getCurrentTime() + "))", false));
			whereOrdinaryList.add(new Where("repay_type",3,"="));
			whereOrdinaryList.add(new Where("additional_status",1,"!="));
			whereOrdinaryList.add(Where.expression("(case when category_type=3 then status=2 else status=3 end)", false));
			whereOrdinaryList.add(new Where("progress",100,"<"));
			whereOrdinaryList.add(Where.notEq("member_id", -1));
			ordinaryLoan.setWhere(whereOrdinaryList);
			ordinaryLoan.setOrders("id desc");
			ordinaryLoan.setLimit(2);
			List<Map> newOrdinaryLoan = (List<Map>)this.baseService.getList(ordinaryLoan);
			
			if(null == newOrdinaryLoan || 0 == newOrdinaryLoan.size()){
				QueryItem queryTopOne = new QueryItem(Module.LOAN, Function.LN_LOAN);
				queryTopOne.setFields("id,serialno,name,apr,period,amount,repay_type,repay_type repay_type_name,status,progress,category_type,category_id category_name");
				queryTopOne.setWhere(Where.expression("((case when status = 3 then (overdue_time >="+ DateUtil.getCurrentTime() +") else (1=1) end) or overdue_time = 0 or status = 4)", false));
				queryTopOne.setWhere(new Where("status",-7,"!="));
				queryTopOne.setWhere(new Where("status",2,">"));
				queryTopOne.setWhere(new Where("additional_status",1,"!="));
				queryTopOne.setWhere(new Where("hidden_status",1));
				queryTopOne.setWhere(new Where("member_id",-1,"!="));
				queryTopOne.setOrders("status asc,additional_status desc,progress asc,add_time desc");
				queryTopOne.setLimit(1);				
				return (List<Map>)this.baseService.getList(queryTopOne);
			}
			
			return newOrdinaryLoan;
	}
	
	/**
	 * 新标预告
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map preview() throws Exception{
		Map<String,Object> previewMap = new HashMap<String, Object>();
		QueryItem queryItem = new QueryItem("loan", "preview");
		queryItem.getWhere().add(Where.eq("id", 1L));
		FnLoanPreview loanPreview = (FnLoanPreview) this.baseService.getOne(queryItem, FnLoanPreview.class);
		
		previewMap.put("name", loanPreview.getName());
		previewMap.put("loan", loanPreview.getLoan());
		previewMap.put("apr", loanPreview.getApr());
		previewMap.put("period", loanPreview.getPeriod());
		//新标预告奖励保留两位小数
		if(loanPreview.getAwardAmount()!=null && StringUtils.isNotBlank(loanPreview.getAwardAmount())){
			previewMap.put("award_amount", NumberUtils.format(new BigDecimal(loanPreview.getAwardAmount().replace("%", "")), new DecimalFormat("0.00"))+"%");
		}else{
			previewMap.put("award_amount", null);
		}		
		previewMap.put("repay_type_id", loanPreview.getRepayTypeId());
		previewMap.put("repay_type_name", loanPreview.getRepayTypeId());
		previewMap.put("begin_time", loanPreview.getBeginTime());
		return previewMap;
	}
	/**
	 * 首页banner
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> phonebanner() throws Exception{
		String imageUrl = PropertiesUtil.getImageHost();
		QueryItem bannerQuseryItem = new QueryItem(Module.CONTENT, Function.CT_ADVERT);
		bannerQuseryItem.setWhere(new Where("category_id", "2"));
		bannerQuseryItem.setWhere(new Where("status", "1"));
		bannerQuseryItem.setOrders("sort_index");
		bannerQuseryItem.setFields("image,jumpurl");
		
		List<Map> bannerList = (List<Map>) this.baseService.getList(bannerQuseryItem);
		for (Map map : bannerList) {
			map.put("image", imageUrl+map.get("image"));
		}
		return bannerList ;
	}
	/**
	 * 拼接网站访问地址
	 * @param url
	 * @return
	 */
	private String getWebDomain(HttpServletRequest request, String url) {
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
