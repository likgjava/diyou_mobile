<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">资金详情</span>
</nav>      
<div class="page" ng-controller="accountCtrl">
  <div class="page-content page-user">

    <div class="wealth-data">
      <div class="user-data-row">              
        <div class="val" ng-bind="account.total_amount|number:2"></div>
        <div class="name"><i class="iconfont">&#xe61a;</i>总资产(元)</div>
      </div>       
    </div>

    <div class="wealth-detail">
      <ul>
        <li class="fn-clear">
          <i class="icon i1"></i>
          <span class="name">可用余额</span>
          <span class="val" ng-bind="account.balance_amount|number:2"></span>
          <span class="unit">元</span>
        </li>
        <li class="fn-clear">
          <i class="icon i2"></i>
          <span class="name">冻结余额</span>
          <span class="val" ng-bind="account.freeze_amount|number:2"></span>
          <span class="unit">元</span>
        </li>
        <li class="fn-clear">
          <i class="icon i3"></i>
          <span class="name">已收利息</span>
          <span class="val" ng-bind="account.interest_yes_total|number:2"></span>
          <span class="unit">元</span>
        </li>
        <li class="fn-clear">
          <i class="icon i4"></i>
          <span class="name">待收本金</span>
          <span class="val" ng-bind="account.principal_wait_total|number:2"></span>
          <span class="unit">元</span>
        </li>
        <li class="fn-clear">
          <i class="icon i5"></i>
          <span class="name">待收利息</span>
          <span class="val" ng-bind="account.interest_wait_total|number:2"></span>
          <span class="unit">元</span>
        </li>
      </ul>
    </div> 
    <div class="loginbar navbar-fixed-bottom">
      <a class="btn on" href="/wap/member/recharge">充值</a>
      <a class="btn on" href="/wap/member/cash">提现</a>
    </div> 
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
  </div>
</div>
