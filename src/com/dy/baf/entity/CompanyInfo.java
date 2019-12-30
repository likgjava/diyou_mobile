package com.dy.baf.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CompanyInfo {

	private String name;

	private String account;

	private Date establishmentDate;

	private Long province;

	private Long city;

	private String address;

	private String tel;

	private String managerName;

	private String managerTel;

	private String idScan;

	private String logo;

	private String companyIntro;

	private String collateral;
	
	private List<Map<String, Object>> enterpriseMaterialTemp;
	
	private String businessLicense;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Date getEstablishmentDate() {
		return establishmentDate;
	}

	public void setEstablishmentDate(Date establishmentDate) {
		this.establishmentDate = establishmentDate;
	}

	public Long getProvince() {
		return province;
	}

	public void setProvince(Long province) {
		this.province = province;
	}

	public Long getCity() {
		return city;
	}

	public void setCity(Long city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getManagerTel() {
		return managerTel;
	}

	public void setManagerTel(String managerTel) {
		this.managerTel = managerTel;
	}

	public String getIdScan() {
		return idScan;
	}

	public void setIdScan(String idScan) {
		this.idScan = idScan;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getCompanyIntro() {
		return companyIntro;
	}

	public void setCompanyIntro(String companyIntro) {
		this.companyIntro = companyIntro;
	}

	public String getCollateral() {
		return collateral;
	}

	public void setCollateral(String collateral) {
		this.collateral = collateral;
	}

	public List<Map<String, Object>> getEnterpriseMaterialTemp() {
		return enterpriseMaterialTemp;
	}

	public void setEnterpriseMaterialTemp(
			List<Map<String, Object>> enterpriseMaterialTemp) {
		this.enterpriseMaterialTemp = enterpriseMaterialTemp;
	}

	public String getBusinessLicense() {
		return businessLicense;
	}

	public void setBusinessLicense(String businessLicense) {
		this.businessLicense = businessLicense;
	}
	
}