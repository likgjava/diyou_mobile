<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">系统设置</span>
</nav>      
<div class="page">
  <div class="page-content page-settings">
    <div class="list-block">
      <ul>
      <li>
        <a href="/wap/articles/notice">
        <div class="item-content">
          <div class="item-title">
            <i class="icon icon-vers"></i>网站公告
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
        </a>
      </li>
      <li>
        <a href="/wap/articles/articles">
        <div class="item-content">
          <div class="item-title">
            <i class="icon icon-ques"></i>常见问题
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
        </a>
      </li>
      <li>
        <div class="item-content" ng-click="contactsShow()">
          <div class="item-title">
            <i class="icon icon-cust"></i>联系客服
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
      </li>
      </ul>          
    </div>  
    <div class="page-view">
	    <c:if test="${empty is_login}">
	    	 <a class="btn btn-lg btn-block btn-blue" href="/wap/system/reglogin">登录</a>
	    </c:if>
	    <c:if test="${!empty is_login}">
	    	<a class="btn btn-lg btn-block btn-blue" ng-click="loginOut()">退出登录</a>
	    </c:if>
    </div>

    <!-- 联系客服 -->
    <div class="hotline-overlay" ng-click="contactsHide()"></div>
    <div class="hotline">
      <div class="hd">
        <h4>客服热线</h4>
        <h5>（工作日${system.serviceHours }）</h5>
      </div>
      <div class="bd">
        <a class="tel" href="tel:${system.serviceTel}">${system.serviceTel}</a>
      </div>
      <div class="fd">
        <a class="btn btn-lg btn-block" ng-click="contactsHide()">取消</a>
      </div>
    </div>
  </div>
</div>