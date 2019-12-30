package com.dy.baf.entity;

import java.io.Serializable;

public class Partner implements Serializable {
	private static final long serialVersionUID = 1467490456540548751L;

	private int id;
	
	private String addTime;
	
	private int attachementId;
	
	private int categoryId;
	
	private String jumpUrl;
	
	private String logo;
	
	private String name;
	
	private int sortIndex;
	
	private String status;
	
	private String summary;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public int getAttachementId() {
		return attachementId;
	}

	public void setAttachementId(int attachementId) {
		this.attachementId = attachementId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getJumpUrl() {
		return jumpUrl;
	}

	public void setJumpUrl(String jumpUrl) {
		this.jumpUrl = jumpUrl;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}