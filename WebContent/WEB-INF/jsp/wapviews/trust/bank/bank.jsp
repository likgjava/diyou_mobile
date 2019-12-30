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
      <div class="bank-card mb10" ng-if="is_bind!=0" ng-repeat="bank in bankCard" style="overflow: auto;">
      	 <!--  <a ng-href="/trust/trust/unBindBankCard"> -->
	          <div class="bank-name" ng-bind="bank.bank_name"></div>
	          <div class="bank-num" ng-bind="bank.account2Wap"></div>
	          <div class="bank-user" ng-bind="bank.realname"></div>
	          <a class="fr"  ng-click="delBank(bank.id)" style="font-size: 0.16rem;margin-top: -0.26rem;">删除</a>
          <!-- </a> -->
      </div>
      <a class="bank-card bank-add" ng-if="is_add==1" ng-click="addBank()">
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
