<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a href="/wap/message/list" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">站内信详情</span>
</nav>      
<div class="page page-article" ng-controller="messageDetailCtrl">
  <div class="page-content">
    <div class="article">
      <div class="art-title" ng-bind="pageData.title"></div>
      <div class="art-time" ng-bind="pageData.add_time|timestamp|date:'yyyy-MM-dd HH:mm:ss'"></div>
      <div class="art-cont" ng-bind-html="pageData.contents"></div>
    </div>
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
  </div>
</div>