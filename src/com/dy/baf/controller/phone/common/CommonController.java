package com.dy.baf.controller.phone.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.common.CommonService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.trust.entity.Bank;
import com.dy.core.trust.entity.BankMap;
import com.dy.core.utils.StringUtils;

/**
 * 
 * @Description: 系统常用数据获取类
 * @author 波哥
 * @date 2015年9月3日 下午2:17:32
 * @version V1.0
 */
@Controller(value = "appCommonController")
public class CommonController extends AppBaseController {

	@Autowired
	private CommonService commonService;

	/**
	 * 省份城市信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/getProvinceCity")
	public DyPhoneResponse getProvinceCity() {
		try {
			return successJsonResonse(commonService.getProvinceCity());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 省份信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/getProvince")
	public DyPhoneResponse getProvince() {
		try {
			return successJsonResonse(commonService.getProvince());
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 城市信息
	 * 
	 * @param pid
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/getCity")
	public DyPhoneResponse getCity(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String pid = paramsMap.get("pid");
			if ( StringUtils.isBlank(pid)) {
				return errorJsonResonse("请传入省份ID");
			}
			return successJsonResonse(commonService.getCity(pid));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 地区信息
	 * 
	 * @param cid
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/getArea")
	public DyPhoneResponse getArea(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String cid = paramsMap.get("cid");
			if ( StringUtils.isBlank(cid)) {
				return errorJsonResonse("请传入市ID");
			}
			return successJsonResonse(commonService.getArea(cid));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 银行列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/bank/bankList")
	public DyPhoneResponse bankList() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
			String fileName = "default" ;
			if(isTrust){
				fileName = "trust" ;
			}
			List<Map> listMap = new ArrayList<Map>();;
			List<Bank> bankList = BankMap.getAllBank();
			for (Bank bank : bankList) {
				Map<String,String> map = new HashMap<String, String>();
				map.put("code", bank.getBankNid());
				map.put("name", bank.getBankName());
				map.put("url", this.getWebDomain(request,"wapassets/"+ fileName + "/egimages/bank/" + bank.getBankNid() + ".jpg")) ;
				listMap.add(map);
			}
			return successJsonResonse(listMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 版本更新
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/member/updateVersion")
	public DyPhoneResponse updateVersion(String xmdy, String diyou) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String version = paramsMap.get("version");
			String phoneType = paramsMap.get("phone_type");
			if (StringUtils.isBlank(version)) {
				return errorJsonResonse("请输入设备类型");
			}
			if (StringUtils.isBlank(phoneType)) {
				return errorJsonResonse("请输入版本号");
			}

			return this.commonService.updateVersion(version, phoneType);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}

	/**
	 * 利息计算器
	 * @param xmdy
	 * @param diyou
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/common/calculator")
	public DyPhoneResponse calculator1(String xmdy, String diyou){
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			return this.commonService.calculator1(paramsMap);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
