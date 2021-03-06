<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a href="/wap/member/index" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">站内信列表</span>
</nav>      
<div class="page page-article page-letter" ng-controller="messageListCtrl">
  <div class="page-content">
    <div class="list-block">
      <ul>
      <li ng-repeat="list in pageData.items">
        <a class="item-content" ng-href="/wap/message/detail#?id={{list.id}}&status={{list.status}}">
          <div class="item-title">
            <div class="time"><i ng-if="list.status==1"></i><span ng-bind="list.add_time|timestamp|date:'yyyy-MM-dd HH:mm'"></span></div>
            <div class="title" ng-bind="list.title|truncate:15"></div>
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      </ul>          
    </div>
    <div class="page-view">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus && !loader">点击加载更多</a>
        <div class="preloader" ng-class="{loading:loader}"></div>
        <div class="txt-cent" ng-if="!getMoreStatus">没有更多数据</div>
    </div>
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
  </div>
</div>