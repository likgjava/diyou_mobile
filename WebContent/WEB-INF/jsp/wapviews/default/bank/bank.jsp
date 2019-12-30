<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">银行卡管理</span>
</nav>      
<div class="page page-user" ng-controller="bankCtrl">
  <div class="page-content"> 
    <div class="page-view user-bank">
      <div class="form-group c-red" ng-if="isNotice">提现需先绑定银行卡</div>
      <!--银行卡信息-->
      <div class="bank-card mb10" ng-if="is_bind==1" ng-repeat="bank in bankCard">
      	 <a ng-href="/wap/bank/editBank">
	          <div class="bank-name" ng-bind="bank.bank_name"></div>
	          <div class="bank-num" ng-bind="bank.account"></div>
	          <div class="bank-user" ng-bind="bank.realname"></div>
          </a>
      </div>

      <div class="bank-change" ng-if="is_bind==1">
        <a ng-href="/wap/bank/editBank">点击银行卡可进行更换</a>
      </div>

      <!-- href="/wap/bank/addbank" -->

      <a class="bank-card bank-add" ng-if="is_bind!=1" ng-click="addBank()">
        <i class="iconfont">&#xe620;</i>
        <span class="name">添加银行卡</span>
      </a>
    </div>

    <!-- 加载状态 -->
    <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
    </div>
  </div>
</div>