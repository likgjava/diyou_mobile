<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">用户信息</span>
</nav>      
<div class="page page-user" ng-controller="userInfoCtrl">
  <div class="page-content user-info">
    <div class="user-box">
      <div class="hd">
        <form class="avatar" id="avatarForm">  
          <img ng-src="{{avatar}}" /> 
          <input class="avatar-file" type="file" name="avatar">
          <span class="avatar-loading">头像上传中</span>
        </form>
        <span class="name" ng-bind="userInfo.member_name"></span>
        <!-- <span class="leve v3"></span> -->
      </div>
      <ul class="bd">
        <li>
          <span class="val" ng-bind="userInfo.credit_point"></span>
          <span class="tit">我的积分</span>
        </li>
        <li>
          <!-- <span class="leve leve-hr"></span> -->
          <img class="leve" ng-src="${system.themeDir}/images/credit/{{userInfo.credit_name}}">
          <span class="tit">信用等级</span>
        </li>
      </ul>
    </div>

    <div class="list-block">
      <ul>
      <li>
        <div class="item-content" ng-click="pcNotice()">
          <div class="item-title">
            <i class="icon icon-user"></i>个人信息
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
      </li>
      <li>
        <div class="item-content" ng-click="pcNotice()">
          <div class="item-title">
            <i class="icon icon-company"></i>公司信息
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
      </li>
      <li>
        <div class="item-content" ng-click="pcNotice()">
          <div class="item-title">
            <i class="icon icon-asset"></i>资产信息
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </div>
      </li>
      </ul>          
    </div>  

  </div>
</div>