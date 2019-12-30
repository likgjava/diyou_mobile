 <%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">常见问题</span>
</nav>      
<div class="page page-article" ng-controller="articlesDetailCtrl">
  <div class="page-content">
    <div class="article">
      <div class="art-title" ng-bind="pageData.data.title"></div>
      <div class="art-time" ng-bind="pageData.data.addTime|timestamp|date:'yyyy-MM-dd HH:mm'"></div>
      <div class="art-cont" ng-bind-html="pageData.data.contents">
        
      </div>
    </div>
  </div>
  <div class="loader" ng-class="{hid:loading}">
    <div class="loader-inner"></div>
  </div>
</div>