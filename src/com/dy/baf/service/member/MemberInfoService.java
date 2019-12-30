package com.dy.baf.service.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dy.baf.entity.CompanyInfo;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberInfo;
import com.dy.baf.entity.common.MbRatingCompany;
import com.dy.baf.entity.common.SysSystemAreas;
import com.dy.baf.entity.common.SysSystemLinkage;
import com.dy.baf.service.MobileService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.exception.DyServiceException;
import com.dy.core.service.BaseService;
import com.dy.core.utils.DateUtil;
import com.dy.core.utils.PropertiesUtil;
import com.dy.core.utils.StringUtils;
import com.dy.core.utils.serializer.SerializerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 
 * @Description:用户基础信息
 * @author 波哥
 * @date 2015年9月10日 上午9:23:52
 * @version V1.0
 */
@Service("mobileMemberInfoService")
public class MemberInfoService extends MobileService {

	@Autowired
	private BaseService baseService;

	/**
	 * 个人信息获取
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse basicInfo(String memberId) throws Exception {
		// 学历
		List<SysSystemLinkage> education = getConfig(201);
		// 婚姻状况
		List<SysSystemLinkage> marray_status = getConfig(214);
		// 公司行业
		List<SysSystemLinkage> company = getConfig(210);
		// 公司规模
		List<SysSystemLinkage> scale = getConfig(211);
		// 职位
		List<SysSystemLinkage> office = getConfig(212);
		// 月收入
		List<SysSystemLinkage> income = getConfig(213);
		// 当前用户
		MbMember member = this.getMbMember(Long.valueOf(memberId));
		String phone = null;
		if (member.getIsPhone() == 1) {
			phone = String.valueOf(member.getPhone());
			if (phone != null) {
				phone = phone.substring(0, phone.length() - (phone.substring(3)).length()) + "****"
						+ phone.substring(7);
			}
		}
		// 用户信息
		Map member_info = getMemberInfo(member.getId());
		
		// 省份
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("member", member);
		map.put("member_info", member_info);
		map.put("education", education);
		map.put("marray_status", marray_status);
		map.put("company", company);
		map.put("scale", scale);
		map.put("office", office);
		map.put("income", income);

		return successJsonResonse(map);
	}

	/**
	 * 个人信息保存
	 */
	public DyPhoneResponse basicInfoSub(MbMemberInfo memberInfo) throws Exception {

		QueryItem infoItem = new QueryItem(Module.MEMBER, Function.MB_MEMBERINFO);
		infoItem.getWhere().add(Where.eq("member_id", memberInfo.getMemberId()));
		MbMemberInfo userInfo = (MbMemberInfo) this.baseService.getOne(infoItem, MbMemberInfo.class);
		if (userInfo == null)
			userInfo = new MbMemberInfo();
		userInfo.setEducationalBackground(memberInfo.getEducationalBackground());
		userInfo.setGraduated(memberInfo.getGraduated());
		userInfo.setMarryStatus(memberInfo.getMarryStatus());
		userInfo.setCompanyIndustry(memberInfo.getCompanyIndustry());
		userInfo.setCompanyScale(memberInfo.getCompanyScale());
		userInfo.setCompanyOffice(memberInfo.getCompanyOffice());
		userInfo.setMonthlyIncome(memberInfo.getMonthlyIncome());
		userInfo.setHometownProvince(memberInfo.getHometownProvince());
		userInfo.setHometownCity(memberInfo.getHometownCity());
		userInfo.setHometownArea(memberInfo.getHometownArea());
		userInfo.setHometownPost(memberInfo.getHometownPost());
		if (userInfo.getId() != null) {
			this.baseService.updateById(Module.MEMBER, Function.MB_MEMBERINFO, userInfo);
		} else {
			this.baseService.insert(Module.MEMBER, Function.MB_MEMBERINFO, userInfo);
		}
		return successJsonResonse("保存成功");
	}

	/**
	 * 公司信息获取
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public DyPhoneResponse companyInfo(String memberId) throws Exception {
		// 用户公司信息
		MbRatingCompany ratingCompany = getRatingCompany(Long.valueOf(memberId));
		// 省份
		List<SysSystemAreas> province = getProvince();
		// 图片地址头部
		String imgPath = PropertiesUtil.getImageHost();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> newMap = new HashMap<String, Object>();
		if (ratingCompany != null) {
			if (StringUtils.isNotBlank(ratingCompany.getEnterpriseMaterial())) {
				// 上传资料反序列化
				String enterpriseMaterial = ratingCompany.getEnterpriseMaterial();
				SerializerUtil serializerUtil = new SerializerUtil();
				List<Map<String, Object>> configList = (List<Map<String, Object>>) serializerUtil
						.unserialize(enterpriseMaterial.getBytes());
				if (configList != null) {
					for (int i = 0; i < configList.size(); i++) {
						Map<String, Object> urlMap = new HashMap<String, Object>();
						urlMap.put("title", configList.get(i).get("title"));
						urlMap.put("imgurl", imgPath + configList.get(i).get("imgurl"));
						urlMap.put("minimg", configList.get(i).get("minimg"));
						list.add(urlMap);
					}
				}
			}
			newMap.put("name", ratingCompany.getName());
			newMap.put("account", ratingCompany.getAccount());
			newMap.put("establishmentDate", DateUtil.dateFormat(ratingCompany.getEstablishmentDate()));
			newMap.put("province", ratingCompany.getProvince());
			newMap.put("city", ratingCompany.getCity());
			newMap.put("address", ratingCompany.getAddress());
			newMap.put("tel", ratingCompany.getTel());
			newMap.put("managerName", ratingCompany.getManagerName());
			newMap.put("managerTel", ratingCompany.getManagerTel());
			if (StringUtils.isNotBlank(ratingCompany.getIdScan())) {
				newMap.put("idScan", imgPath + ratingCompany.getIdScan());
			}
			if (StringUtils.isNotBlank(ratingCompany.getLogo())) {
				newMap.put("logo", imgPath + ratingCompany.getLogo());
			}
			newMap.put("companyIntro", ratingCompany.getCompanyIntro());
			newMap.put("collateral", ratingCompany.getCollateral());
			newMap.put("enterpriseMaterial", list);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("company_info", newMap);
		map.put("province", province);
		return successJsonResonse(map);
	}

	/**
	 * 企业信息保存
	 */
	public DyPhoneResponse companyInfoSub(String memberId,String jsonData) throws Exception {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		CompanyInfo comInfo = gson.fromJson(jsonData, CompanyInfo.class);
		MbMember user = this.getMbMember(Long.valueOf("memberId"));
		// 图片地址头部
		String imgPath = PropertiesUtil.getImageHost();
		MbRatingCompany companyInfo = getRatingCompany(user.getId());
		if (companyInfo == null) {
			companyInfo = new MbRatingCompany();
			companyInfo.setAddTime(DateUtil.getCurrentTime());
		}
		companyInfo.setMemberId(user.getId());
		companyInfo.setMemberName(user.getName());
		companyInfo.setName(comInfo.getName());
		companyInfo.setAccount(comInfo.getAccount());
		companyInfo.setEstablishmentDate(comInfo.getEstablishmentDate());
		companyInfo.setProvince(comInfo.getProvince());
		companyInfo.setCity(comInfo.getCity());
		companyInfo.setAddress(comInfo.getAddress());
		companyInfo.setTel(comInfo.getTel());
		companyInfo.setManagerName(comInfo.getManagerName());
		companyInfo.setManagerTel(comInfo.getManagerTel());
		companyInfo.setIdScan(comInfo.getIdScan().toString().replace(imgPath, ""));
		companyInfo.setLogo(comInfo.getLogo().toString().replace(imgPath, ""));
		if (comInfo.getEnterpriseMaterialTemp() != null && comInfo.getEnterpriseMaterialTemp().size() > 0) {
			for (Map<String, Object> map : comInfo.getEnterpriseMaterialTemp()) {
				map.put("imgurl", map.get("imgurl").toString().replace(imgPath, ""));
			}
			companyInfo.setEnterpriseMaterial(
					new String(new SerializerUtil().serialize(comInfo.getEnterpriseMaterialTemp())));
		} else {
			companyInfo.setEnterpriseMaterial("0000000");
		}
		companyInfo.setCompanyIntro(comInfo.getCompanyIntro());
		companyInfo.setCollateral(comInfo.getCollateral());
		companyInfo.setStatus(-2);
		if (companyInfo.getId() != null) {
			this.baseService.updateById(Module.MEMBER, Function.MB_RATINGCOMPANY, companyInfo);
		} else {
			this.baseService.insert(Module.MEMBER, Function.MB_RATINGCOMPANY, companyInfo);
		}
		return successJsonResonse("保存成功");
	}

	/**
	 * 保存营业执照信息
	 * 
	 * @throws Exception
	 */
	public DyPhoneResponse saveCompany(String companycode, String memberId) throws Exception {
		MbMember user = this.getMbMember(Long.valueOf(memberId));
		MbRatingCompany companyInfo = getRatingCompany(user.getId());
		if (companyInfo == null) {
			companyInfo = new MbRatingCompany();
			companyInfo.setAddTime(DateUtil.getCurrentTime());
		}
		companyInfo.setMemberId(user.getId());
		companyInfo.setMemberName(user.getName());
		if (StringUtils.isNotBlank(companycode)) {
			companyInfo.setBusinessLicense(companycode);
		}
		companyInfo.setStatus(-2);
		QueryItem queryItem = new QueryItem(Module.MEMBER, Function.MB_RATINGCOMPANY);
		queryItem.setWhere(Where.eq("business_license", companycode));
		Map<String, Object> map = this.baseService.getOne(queryItem);
		if (map != null && map.size() > 0) {
			DyResponse dyResponse = new DyResponse();
			dyResponse.setStatus(dyResponse.ERROR);
			dyResponse.setDescription("公司执照编号已存在");
			return pcTurnApp(dyResponse);
		}
		if (companyInfo.getId() != null) {
			this.baseService.updateById("member", "ratingcompany", companyInfo);
		} else {
			this.baseService.insert("member", "ratingcompany", companyInfo);
		}

		return successJsonResonse("保存成功");
	}

	/**
	 * 获取联动值
	 * 
	 * @throws Exception
	 */
	private List<SysSystemLinkage> getConfig(Integer pid) throws Exception {
		// 获取联动配置的值
		QueryItem linkageItem = new QueryItem();
		linkageItem.getWhere().add(Where.eq("pid", pid));
		List<SysSystemLinkage> linkageList = this.getListByEntity(linkageItem, "system", "linkage",
				SysSystemLinkage.class);
		return linkageList;

	}
	
	/**
	 * 联动值
	 * @return
	 * @throws DyServiceException
	 */
	private List<Map> getLinkages() throws DyServiceException{
		QueryItem eduQryItem = new QueryItem( Module.SYSTEM, Function.SYS_LINKAGE);
		eduQryItem.setFields("id, name");
		eduQryItem.setOrders("id asc");
		eduQryItem.setWhere(Where.in("pid", "201,214,212,213,211,210"));
		return this.baseService.getList(eduQryItem,Map.class);
	}

	/**
	 * 根据memberid获取memberinfo
	 * 
	 * @throws Exception
	 */
	private Map getMemberInfo(Long memberId) throws Exception {
		QueryItem item = new QueryItem(Module.MEMBER, Function.MB_MEMBERINFO);
		item.getWhere().add(Where.eq("member_id", memberId));
		Map memberInfo = this.baseService.getOne(item, Map.class);
		
		List<Map> list = this.getLinkages();
		for (Map map : list) {
			if(map.get("id") .equals(memberInfo.get("educational_background") )){
				memberInfo.put("educational_background_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("company_scale") )){
				memberInfo.put("company_scale_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("marry_status") )){
				memberInfo.put("marry_status_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("company_industry") )){
				memberInfo.put("company_industry_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("hometown_post") )){
				memberInfo.put("hometown_post_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("hometown_area") )){
				memberInfo.put("hometown_area_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("company_office_city") )){
				memberInfo.put("company_office_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("hometown_province") )){
				memberInfo.put("hometown_province_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("monthly_income") )){
				memberInfo.put("monthly_income_name", map.get("name"));
			}
			if(map.get("id") .equals(memberInfo.get("hometown_city") )){
				memberInfo.put("hometown_city_name", map.get("name"));
			}
		}
		return memberInfo;
	}

	
	
	/**
	 * 获取省份
	 * 
	 * @throws Exception
	 */
	private List<SysSystemAreas> getProvince() throws Exception {
		QueryItem item = new QueryItem();
		item.getWhere().add(Where.eq("pid", 0));
		List<SysSystemAreas> provinceList = this.getListByEntity(item, Module.SYSTEM, Function.SYS_AREAS,
				SysSystemAreas.class);
		return provinceList;

	}

	/**
	 * 根据memberid获取公司信息
	 * 
	 * @throws Exception
	 */
	private MbRatingCompany getRatingCompany(Long memberId) throws Exception {
		// 获取企业信息
		QueryItem companyItem = new QueryItem();
		companyItem.getWhere().add(Where.eq("member_id", memberId));
		MbRatingCompany ratingCompany = (MbRatingCompany) super.getOneByEntity(companyItem, Module.MEMBER,
				Function.MB_RATINGCOMPANY, MbRatingCompany.class);
		return ratingCompany;
	}

}
