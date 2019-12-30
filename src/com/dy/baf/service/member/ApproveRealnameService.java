package com.dy.baf.service.member;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbApproveRealname;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.baf.service.system.CommonService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.DyHttpClient;
import com.dy.core.utils.IdCardUtil;
import com.google.gson.Gson;

/**
 * 
 * 
 * @Description: 实名认证
 * @author 波哥
 * @date 2015年9月9日 上午9:16:25 
 * @version V1.0
 */
@Service("mobileApproveRealnameService")
public class ApproveRealnameService extends MobileService {
	@Autowired
	private BaseService baseService;
	@Autowired
	private CommonService commonService;
	
	/**
	 * 用户实名认证
	 * @param request
	 * @param response
	 * @param realname
	 * @param card_id
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse realnameApprove(HttpServletRequest request,HttpServletResponse response, String realname, String card_id,String memberId) throws Exception {
		if(StringUtils.isBlank(memberId)){
			return errorJsonResonse("登录标识不能为空");
		}
		if(StringUtils.isBlank(card_id)){
			return errorJsonResonse("身份证号码不能为空");
		}
		if(StringUtils.isBlank(realname)){
			return errorJsonResonse("真实姓名不能为空");
		}
		if(!com.dy.core.utils.StringUtils.is_chinese(realname)){
			return errorJsonResonse("请输入正确的姓名");
		}
		if(!IdCardUtil.validateCard(card_id)){
			return errorJsonResonse("身份证号码不正确");
		}
		
		boolean isRealNameOpen = "1".equals(commonService.getSystemConfigByNid("idcard"));
		Gson gson = new Gson();
		MbMember member = this.getMbMember(Long.valueOf(memberId));
		Map<String, File> fileMap = new HashMap<String, File>();
		MultipartHttpServletRequest multiRequest = null ;
		try {
			multiRequest = (MultipartHttpServletRequest) request;
		} catch (Exception e) {
			if(isRealNameOpen)
				return errorJsonResonse("请上传身份证图片");
		}
		//正面
		Object frontObj = multiRequest == null ? null : multiRequest.getFile("front") ;
		if(isRealNameOpen && null == frontObj){
			return errorJsonResonse("身份证正面不能为空");
		}
		CommonsMultipartFile front = (CommonsMultipartFile) frontObj;
		if(front !=null && front.getSize() > 2097152) return errorJsonResonse("上传图片不能大于2M");
		if(front != null) {
			DiskFileItem fileItem = (DiskFileItem) front.getFileItem();
//			fileMap.put(front.getOriginalFilename(), fileItem.getStoreLocation());
			fileMap.put("front" + front.getOriginalFilename(), fileItem.getStoreLocation());
		}
		//反面
		Object versoObj = multiRequest == null ? null : multiRequest.getFile("verso") ;
		if(isRealNameOpen && null == versoObj){
			return errorJsonResonse("身份证反面不能为空");
		}
		CommonsMultipartFile verso = (CommonsMultipartFile) versoObj;
		if(verso != null) {
			DiskFileItem fileItem = (DiskFileItem) verso.getFileItem();
			fileMap.put("verso" + verso.getOriginalFilename(), fileItem.getStoreLocation());
		}
		if(verso !=null && verso.getSize() > 2097152) return errorJsonResonse("上传图片不能大于2M");
		//非空校验
		String errorMsg = null;
		if(isRealNameOpen) 
			validateNull(new Object[]{realname, card_id, front, verso}, new String[]{"真实姓名", "身份证号码", "身份证正面", "身份证反面"});
		else
			validateNull(new Object[]{realname, card_id}, new String[]{"真实姓名", "身份证号码"});
		if(StringUtils.isNotEmpty(errorMsg)) return errorJsonResonse(errorMsg);
		
		card_id = card_id.trim();
		//判断身份证号码是否存在
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("card_id", card_id));
		whereList.add(Where.notEq("status", -1));
		
		QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_REALNAME);
		queryItem.setFields("1");
		queryItem.setWhere(whereList);
		Object objResult = this.baseService.getList(queryItem);
		if(objResult != null && ((List)objResult).size() > 0) return errorJsonResonse("身份证号已经存在");
		
		//上传到图片服务器
		MbApproveRealname approveRealname = getRealname(member.getId());
		if (approveRealname == null) {
			approveRealname = new MbApproveRealname();
		}
		if(fileMap.size() > 0) {
			DyResponse imgResponse = DyHttpClient.doImageUpload(Module.MEMBER, Function.MB_REALNAME, fileMap);
			if(imgResponse == null || imgResponse.getStatus() != DyResponse.OK) return errorJsonResonse("修改出错");
			
			List<Map<String, Object>> fileList = (List<Map<String, Object>>) imgResponse.getData();
			for(Map<String, Object> map : fileList) {
				if(front != null && ("front" + front.getOriginalFilename()).equals(map.get("name").toString()))
					approveRealname.setPositiveCard(map.get("id").toString());
				if(verso != null && ("verso" + verso.getOriginalFilename()).equals(map.get("name").toString()))
					approveRealname.setBackCard(map.get("id").toString());
			}
		}
		approveRealname.setRealname(realname);
		approveRealname.setCardId(card_id);
		approveRealname.setStatus(-2);
		approveRealname.setMemberId(member.getId());
		approveRealname.setMemberName(member.getName());
		approveRealname.setAddTime(DateUtil.getCurrentTime());
		if (approveRealname.getId() == null) {
			this.baseService.insert(Module.MEMBER, Function.MB_REALNAME, approveRealname);
		} else {
			this.baseService.updateById(Module.MEMBER, Function.MB_REALNAME, approveRealname);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		String cardId = String.valueOf(card_id);
		cardId = cardId.substring(0, cardId.length() - (cardId.substring(3)).length()) + "******";
		realname = realname.substring(0, 1) + "**";
		map.put("realname", realname);
		map.put("card_id", cardId);
		return successJsonResonse(map);
	}
	
	/**
	 * 获取验证信息
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse isApprove(String memberId) throws Exception {
		Map<String,Object> response = new HashMap<String, Object>() ;
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		if (user == null || user.getId() == null) {
			return errorJsonResonse("您未登录或登录超时");
		}
		
		MbApproveRealname  realname= this.getRealname(Long.valueOf(memberId));
		
		response.put("realname_status", realname == null ? 0 : realname.getStatus()) ;
		response.put("is_realname", user.getIsRealname()) ;
		String emailAuth=this.getSysValue("email_auth");
		if("2".equals(emailAuth)){
			response.put("is_email", 1) ;
		}else{
			response.put("is_email", user.getIsEmail()) ;
		}
		response.put("is_phone", user.getIsPhone()) ;
		response.put("paypassword", user.getPaypassword() == null ? null : 1) ;
		response.put("is_trust", StringUtils.isNotBlank(user.getTrustAccount()) ? 1 : -1) ;
		
		return this.successJsonResonse(response);
	}
	
	/**
	 * 获取实名认证信息
	 * @throws Exception 
	 */
	public MbApproveRealname getRealname(Long memberId) throws Exception{
		MbApproveRealname realname = null ;
		try {
			QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_REALNAME);
			queryItem.setWhere(Where.eq("member_id", memberId));
			queryItem.getWhere().add(Where.notEq("status", -1));
			List<MbApproveRealname> realNameList = baseService.getList(queryItem, MbApproveRealname.class);
			
			if(realNameList != null && realNameList.size() > 0) realname = realNameList.get(0);
		} catch (Exception e) {
			realname = null ;
		}
		
		return realname;
		
	}
	
	/**
	 * 非空校验
	 * @param obj 需要校验的内容
	 * @param viewName 界面显示的名称
	 * @return
	 */
	private String validateNull(Object[] obj, String[] viewName) {
		if(obj == null || viewName == null || obj.length != viewName.length) return null;
		
		for(int i=0;i<obj.length;i++) {
			Object value = obj[i];
			if(obj[i] == null || (value instanceof String && StringUtils.isEmpty(value.toString()))) {
				return viewName[i]+"不能为空";
			}
		}
		
		return null;
	}
}