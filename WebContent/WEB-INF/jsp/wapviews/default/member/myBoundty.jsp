<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<nav class="nav-top navbar-fixed-top">
  <a href="/wap/member/index" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">我的红包</span>
  <a href="/wap/message/list" class="iconfont setting">
        <img src="/wapassets/trust/css/images/newupdate/masico.png" alt="">
    </a>
</nav>      
<div class="page">
  <div class="page-content page-bounty" ng-controller="mybountyCtrl">
    <ul>
       <li ng-repeat="bounty in bountyData.items" ng-class="{'undefalut':bounty.status!='已过期'&&bounty.status!='已使用','defalut':bounty.status=='已使用','used':bounty.status=='已过期'}" >
          <div class="lileft">
            <div class="letop">
              <span class="dw">￥</span>
              <span class="rednumber" ng-bind="bounty.amount|number:2">5</span>
            </div>
             <div class="lebot" ng-bind="bounty.bounty_type"></div>
          </div>
          <div class="liright">
            <div class="toptit">单笔投资年化金额满<span ng-bind="bounty.bounty_min"></span>元使用</div>
            <div class="toptime" ng-bind="bounty.end_time">2016-03-27到期</div>
          </div>
       </li>
    </ul>
    <div class="page-view">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus && !loader">点击加载更多</a>
        <div class="preloader" ng-class="{loading:loader}"></div>
        <div class="txt-cent" ng-if="!getMoreStatus">没有更多数据</div>
    </div>
  </div>
  
<!-- 加载状态 -->
 <!--  <div class="loader" ng-class="{hid:loading}">
     <div class="loader-inner"></div>
 </div>   -->
</div>
