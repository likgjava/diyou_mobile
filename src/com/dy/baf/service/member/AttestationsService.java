package com.dy.baf.service.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbAttestations;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;

@Service("mobileAttestationsService")
public class AttestationsService extends MobileService {

	@Autowired
	private BaseService baseService;
	/**
	 * 获取材料认证
	 * @param memberId
	 * @return
	 */
	public DyPhoneResponse attestations(String memberId) throws Exception{
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		QueryItem atteItem = new QueryItem(Module.MEMBER,Function.MB_ATTESTATIONSTYPE);
		atteItem.setFields("id,name");
		List<Map> category = this.baseService.getList(atteItem, Map.class);
		return successJsonResonse(category);
	}
	
	
	/**
	 * 提交审核
	 */
	public DyPhoneResponse authenticationUpload(String images,String memberId,String category_id) throws Exception {
		String[] image = images.split(";");
		String img = "";
		for(int i = 0;i<image.length;i++){
			if(i==0){
				img = image[0].substring(0,image[0].lastIndexOf("|")).trim().toLowerCase();
			}else{
				img = img + "|" + image[i].substring(0,image[i].lastIndexOf("|")).trim().toLowerCase();
			}
		}
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		QueryItem atteItem = new QueryItem("member","attestations");
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(new Where("member_id",user.getId()));
		whereList.add(new Where("category_id",category_id));
		atteItem.setWhere(whereList);
		MbAttestations cuAtte = (MbAttestations) this.baseService.getOne(atteItem, MbAttestations.class);
		if(cuAtte !=null){
			cuAtte.setAttachmentIds(img);
			cuAtte.setStatus(-2);
			this.baseService.updateById(Module.MEMBER, Function.MB_ATTESTATIONS, cuAtte);
		}else{
			MbAttestations atte = new MbAttestations();
			atte.setMemberId(user.getId());
			atte.setMemberName(user.getName());
			atte.setAttachmentIds(img);
			atte.setCategoryId(Long.valueOf(category_id));
			atte.setCredit(0);
			atte.setAddTime(DateUtil.getCurrentTime());
			this.baseService.insert("member", "attestations", atte);
		}
		return successJsonResonse("上传成功");
	}
}
