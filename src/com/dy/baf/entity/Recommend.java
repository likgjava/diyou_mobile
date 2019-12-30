package com.dy.baf.entity;

import java.io.Serializable;
import java.util.List;

import com.dy.baf.entity.common.CtArticles;

public class Recommend implements Serializable {

	private static final long serialVersionUID = -6012435887184816215L;

	private Long id;
	private String name;
	private String router;
	private Long addTime;

	private List<CtArticles> articleList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRouter() {
		return router;
	}

	public void setRouter(String router) {
		this.router = router;
	}

	public Long getAddTime() {
		return addTime;
	}

	public void setAddTime(Long addTime) {
		this.addTime = addTime;
	}

	public List<CtArticles> getArticleList() {
		return articleList;
	}

	public void setArticleList(List<CtArticles> articleList) {
		this.articleList = articleList;
	}

}