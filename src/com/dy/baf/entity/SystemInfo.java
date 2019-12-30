package com.dy.baf.entity;

import java.io.Serializable;

public class SystemInfo implements Serializable {

	private static final long serialVersionUID = 3651977658085084454L;

	private String keywords;

	private String description;

	private String siteName;

	private String siteIco;

	private String siteLogo;

	private String siteTel;

	private String title;

	private String themeDir;

	private String licenceCredit;

	private String licenceNorton;

	private String licencePolice;

	private String licenceEbs;

	private String siteCopyright;

	private String siteLicenseNumber;

	private String serviceTel;

	private String serviceHours;

	private String serviceQq;

	private String serviceQqGroup;

	private String serviceWeixinImgcode;

	private String serviceWeiboUrl;

	private String contentPage;

	public SystemInfo() {}
	
	public SystemInfo(String contentPage) {
		this.contentPage = contentPage;
	}
	
	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteIco() {
		return siteIco;
	}

	public void setSiteIco(String siteIco) {
		this.siteIco = siteIco;
	}

	public String getSiteLogo() {
		return siteLogo;
	}

	public void setSiteLogo(String siteLogo) {
		this.siteLogo = siteLogo;
	}

	public String getSiteTel() {
		return siteTel;
	}

	public void setSiteTel(String siteTel) {
		this.siteTel = siteTel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getThemeDir() {
		return themeDir;
	}

	public void setThemeDir(String themeDir) {
		this.themeDir = themeDir;
	}

	public String getLicenceCredit() {
		return licenceCredit;
	}

	public void setLicenceCredit(String licenceCredit) {
		this.licenceCredit = licenceCredit;
	}

	public String getLicenceNorton() {
		return licenceNorton;
	}

	public void setLicenceNorton(String licenceNorton) {
		this.licenceNorton = licenceNorton;
	}

	public String getLicencePolice() {
		return licencePolice;
	}

	public void setLicencePolice(String licencePolice) {
		this.licencePolice = licencePolice;
	}

	public String getLicenceEbs() {
		return licenceEbs;
	}

	public void setLicenceEbs(String licenceEbs) {
		this.licenceEbs = licenceEbs;
	}

	public String getSiteCopyright() {
		return siteCopyright;
	}

	public void setSiteCopyright(String siteCopyright) {
		this.siteCopyright = siteCopyright;
	}

	public String getSiteLicenseNumber() {
		return siteLicenseNumber;
	}

	public void setSiteLicenseNumber(String siteLicenseNumber) {
		this.siteLicenseNumber = siteLicenseNumber;
	}

	public String getServiceTel() {
		return serviceTel;
	}

	public void setServiceTel(String serviceTel) {
		this.serviceTel = serviceTel;
	}

	public String getServiceHours() {
		return serviceHours;
	}

	public void setServiceHours(String serviceHours) {
		this.serviceHours = serviceHours;
	}

	public String getServiceQq() {
		return serviceQq;
	}

	public void setServiceQq(String serviceQq) {
		this.serviceQq = serviceQq;
	}

	public String getServiceQqGroup() {
		return serviceQqGroup;
	}

	public void setServiceQqGroup(String serviceQqGroup) {
		this.serviceQqGroup = serviceQqGroup;
	}

	public String getServiceWeixinImgcode() {
		return serviceWeixinImgcode;
	}

	public void setServiceWeixinImgcode(String serviceWeixinImgcode) {
		this.serviceWeixinImgcode = serviceWeixinImgcode;
	}

	public String getServiceWeiboUrl() {
		return serviceWeiboUrl;
	}

	public void setServiceWeiboUrl(String serviceWeiboUrl) {
		this.serviceWeiboUrl = serviceWeiboUrl;
	}

	public String getContentPage() {
		return contentPage;
	}

	public void setContentPage(String contentPage) {
		this.contentPage = contentPage;
	}
}