<%@ page language="java" pageEncoding="UTF-8"%> 
<div class="page page-article" ng-controller="noticeDetailCtrl">

  <div class="article">
    <div class="art-title" ng-bind="pageData.data.title"></div>
    <div class="art-time" ng-bind="pageData.data.addTime|timestamp|date:'yyyy-MM-dd HH:mm'"></div>
    <div class="art-cont" ng-bind-html="pageData.data.contents"></div>
  </div>

  <div class="loader" ng-class="{hid:loading}">
    <div class="loader-inner"></div>
  </div>
</div>