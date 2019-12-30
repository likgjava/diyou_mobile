package com.dy.baf.controller.phone.system;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.dy.baf.controller.FrontBaseController;
import com.dy.core.entity.DyResponse;
import com.dy.core.utils.DyHttpClient;
import com.dy.core.utils.PropertiesUtil;
import com.google.gson.Gson;

@Controller
@RequestMapping("/system")
public class FrontSystemController extends FrontBaseController {

	
	
	/**
	 * 图片上传
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/public/upload", method=RequestMethod.POST, produces="text/html;charset=UTF-8")
	public String upload(HttpServletRequest request) throws Exception{
		Gson gson = new Gson();
		Map<String, File> fileMap = new HashMap<String, File>();
		//CommonsMultipartFile multiRequest = (CommonsMultipartFile) request;
		CommonsMultipartFile file = (CommonsMultipartFile) ((MultipartRequest) request).getFile("file");
		if(file == null) return gson.toJson(createErrorJsonResonse("上传图片不能为空"));
		if(file != null) {
			DiskFileItem fileItem = (DiskFileItem) file.getFileItem();
			fileMap.put(file.getOriginalFilename(), fileItem.getStoreLocation());
		}
		//上传到图片服务器
		String path ="";
		if(fileMap.size() > 0) {
			DyResponse response = DyHttpClient.doImageUpload("member", "memberInfo", fileMap);
			if(response == null || response.getStatus() != DyResponse.OK) return gson.toJson(createErrorJsonResonse(this.getMessage("update.error")));
			
			List<Map<String, Object>> fileList = (List<Map<String, Object>>) response.getData();
			for(Map<String, Object> map : fileList) {
				if(file != null && file.getOriginalFilename().equals(map.get("name").toString())){
					path = map.get("id").toString();
				}
			}
		}
		//图片地址头部
		String imgPath = PropertiesUtil.getImageHost();
		String url = "";
		if(!"".equals(path))url = imgPath+"/"+path;
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> newMap = new HashMap<String,Object>();
		map.put("code", 200);
		map.put("file_url", path);
		map.put("url", url);
		newMap.put("file", map);
		return gson.toJson(createSuccessJsonResonse(newMap));
	}
}