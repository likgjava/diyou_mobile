package com.dy.baf.service.trust;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.MobileService;

/**
 * 
 * 
 * @Description: 注册（托管）
 * @author 波哥
 * @date 2015年9月9日 下午6:55:23
 * @version V1.0
 */
@Service("mobileTrustRegisterService")
public class TrustRegisterService extends MobileService {


	/**
	 * 获取托管注册状态
	 * @param memberId
	 * @return
	 * @throws Exception
	 */
	public DyPhoneResponse isRegister(String memberId) throws Exception {

		MbMember member = this.getMbMember(Long.valueOf(memberId));
		Map<String,Object> mapResonse = new HashMap<String, Object>();
		mapResonse.put("register", member.getTrustAccount() != null ? 1:-1);
		mapResonse.put("phone", member.getIsPhone());
		mapResonse.put("real_name", member.getIsRealname());
		String emailAuth=this.getSysValue("email_auth");
		if("2".equals(emailAuth)){
			mapResonse.put("is_email", 1) ;
		}else{
			mapResonse.put("is_email", member.getIsEmail()) ;
		}
		mapResonse.put("authorization", member.getIsAuto());
		
		//add by panxh at 2016年9月21日  控制身份正图片是否需要显示上传
		mapResonse.put("is_realname_open", this.getSysValue("idcard"));
		//end by panxh at 2016年9月21日  控制身份正图片是否需要显示上传
		return successJsonResonse(mapResonse);
	}
}
