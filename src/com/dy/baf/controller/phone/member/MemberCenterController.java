package com.dy.baf.controller.phone.member;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbCreditRank;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.service.finance.AccountService;
import com.dy.baf.service.member.FrontMemberService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DyHttpClient;
/**
 * 会员中心
 * @author Administrator
 *
 */
@Controller(value="appMemberCenterController")
public class MemberCenterController extends AppBaseController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private FrontMemberService frontMemberService;
	
	/**
	 * 用户中心
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/memberCenter")
	public DyPhoneResponse toMemberCenter(String xmdy, String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.accountService.toMemberCenter(login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
		
	}
	
	
	
	/**
	 * 获取会员信息
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/member/userInfo")
	public DyPhoneResponse getInfo(String xmdy, String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String login_token = paramsMap.get("login_token");
			return this.frontMemberService.getInfo(login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	
	/**
	 * 上传头像页面
	 */
	@RequestMapping("/member/avatar")
	public ModelAndView avatar() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("member/center/avatar.jsp");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 上传头像提交
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/member/avatarSubmit")
	public DyResponse avatarSubmit(HttpServletRequest request) throws Exception{
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		QueryItem infoItem = new QueryItem();
		infoItem.getWhere().add(Where.eq("member_id", user.getId()));
		MbMemberInfo info = this.getOneByEntity(infoItem, Module.MEMBER, Function.MB_MEMBERINFO, MbMemberInfo.class);
		if(info ==null) return createErrorJsonResonse("上传错误");
		Map<String, File> fileMap = new HashMap<String, File>();
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		//头像
		CommonsMultipartFile avatar = (CommonsMultipartFile) multiRequest.getFile("avatar");
		System.out.println("----"+avatar.getSize()+"-----");//8388608
		if(avatar !=null && avatar.getSize() > 2097152) return createErrorJsonResonse("上传图片大于2M");
		if(avatar != null) {
			DiskFileItem fileItem = (DiskFileItem) avatar.getFileItem();
			fileMap.put(avatar.getOriginalFilename(), fileItem.getStoreLocation());
		}
		//上传到图片服务器
		if(fileMap.size() > 0) {
			DyResponse response = DyHttpClient.doImageUpload("member", "member", fileMap);
			if(response == null || response.getStatus() != DyResponse.OK) return createErrorJsonResonse(this.getMessage("update.error"));
			
			List<Map<String, Object>> fileList = (List<Map<String, Object>>) response.getData();
			for(Map<String, Object> map : fileList) {
				if(avatar != null && avatar.getOriginalFilename().equals(map.get("name").toString()))
					info.setAvatar(map.get("id").toString());
					this.updateById(Module.MEMBER, Function.MB_MEMBERINFO, info);
			}
		}
		return createSuccessJsonResonse(null,"上传成功");
	}
	/**
	 * 判断早中晚
	 * @return
	 */
	public String getDateSx(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		String returnStr = "";
		if (hour >= 6 && hour < 8) {
			returnStr = "早上好";
		} else if (hour >= 8 && hour < 11) {
			returnStr = "上午好";
		} else if (hour >= 11 && hour < 13) {
			returnStr = "中午好";
		} else if (hour >= 13 && hour < 18) {
			returnStr = "下午好";
		} else {
			returnStr = "晚上好";
		}
		return returnStr;
	}
	
	/**
	 * 判断安全等级
	 */
	public String getLevelName(MbMember user){
		Integer count = 0;
		String returnStr="";
		if(user.getIsRealname() == 1){
			count +=1;
		}
		if(user.getIsEmail() == 1){
			count +=1;
		}
		if(user.getIsPhone() == 1){
			count +=1;
		}
		if(user.getPaypassword() != null){
			count +=1;
		}
		if(count<1){
			returnStr = "低";
		}else if(count <3 && count >=1){
			returnStr = "中";
		}else{
			returnStr = "高";
		}
		return returnStr;
	}
	/**
	 * 积分等级
	 */
	public String getLevelCredit(String value) throws Exception{
		QueryItem item=new QueryItem(new Where("start_point",value,"<="));
		item.setWhere(new Where("end_point", value, ">="));   
		MbCreditRank data=this.getOneByEntity(item, Module.MEMBER, Function.MB_RANKCONFIG,MbCreditRank.class);	   		
		return data.getIcon();
	}
	/**
	 * 积分记录历史
	 */
	@RequestMapping("/credit/index")
	public ModelAndView creditIndex(Integer user_role) {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("member/credit/index.jsp");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 获取积分记录信息
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/credit/creditlog")
	public Page getCreditLog(Integer page) throws Exception {
		MbMember user = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		QueryItem queryItem = new QueryItem(new Where("member_id", user.getId()));
		queryItem.setPage(page == null ? 1 : page);
		queryItem.setFields("id,member_name,type,category_id,point,remark,add_time");
		Page pageObj = (Page) this.dataConvert(getPageByMap(queryItem, Module.MEMBER, Function.MB_CREDITLOG), "type:getCreditType,category_id:getCreditCate", "add_time");
		//计算用户总积分
		BigDecimal amountTotal = BigDecimal.ZERO;
		List<Map> mapList = pageObj.getItems();
		for (Map map : mapList) {
			amountTotal = amountTotal.add(new BigDecimal(map.get("point").toString()));
		}
		pageObj.setParams(amountTotal);
		return pageObj;

	}
	
	
}
