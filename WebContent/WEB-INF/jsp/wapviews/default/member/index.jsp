<%@ page language="java" pageEncoding="UTF-8"%>

<!-- <div class="nav-top-right">
  <a class="link" ng-click="loginOut()">退出</a>
</div>     -->  

<div class="page page-user" ng-controller="userindexCtrl">
<nav class="nav-top navbar-fixed-top">
    <a class="email" href="/wap/message/list"><i class="iconfont">&#xe61d;</i><span class="num ng-binding" ng-bind="message"></span></a>
    <span class="title">个人中心</span>
</nav>
  <div class="page-content user-index">
      <div class="user-data">
        <a href="/wap/member/account" class="user-data-row user-data-rate">              
          <div class="val" ng-bind="interest_award | number:2"></div>
          <div class="name"><i class="icon icon-rate"></i>预估累计收益(元)</div>
          <i class="iconfont next">&#xe614;</i>
        </a>
        <div class="user-data-row">
          <div class="user-data-total">
            <div class="name"><i class="icon icon-total"></i>账户总资产(元)</div>
            <div class="val" ng-bind="total_amount | number:2">***</div>
          </div>
          <div class="user-data-income">
            <div class="name"><i class="icon icon-income"></i>可用余额(元)</div>
            <div class="val" ng-bind="balance_amount | number:2">***</div>
          </div>
        </div>            
      </div>
      <div class="user-opts">
        <ul>
        <li>
          <a ng-click="pcNotice()">
            <i class="icon icon-pay"></i>
            <span>充值</span>
          </a>
        </li>
        <li>
          <a ng-href="/wap/member/cash">
            <i class="icon icon-take"></i>
            <span>提现</span>
          </a>
        </li>
        </ul>
      </div>
      <div class="user-nav">
        <div class="item-content">

          <a class="item col-xs-6" href="/wap/member/mytender">
            <i class="icon icon-invest"></i>
            <dl>
              <dt><span>我的项目</span></dt>
              <dd>我投资过的项目</dd>
              <dd><span>查看项目</span></dd>
            </dl>
          </a>
          <a class="item col-xs-6" href="/wap/account/accountLog">
            <i class="icon icon-record"></i>
            <dl>
              <dt><span>交易记录</span></dt>
              <dd>账户流水账</dd>
              <dd><span>快来看看吧</span></dd>
            </dl>
          </a>
        </div>
        <div class="item-content" style="border-bottom: 1px solid #e4e4e4;">
          <a class="item col-xs-6" href="/wap/spread/mySpread">
            <i class="icon icon-spread"></i>
            <dl>
              <dt><span>我的推广</span></dt>
              <dd>推广送积分</dd>
              <dd><span>小伙伴一起来赚钱</span></dd>
            </dl>
          </a>
          <a class="item col-xs-6" href="/wap/member/myaccountdata">
            <i class="icon icon-account"></i>
            <dl>
              <dt><span>账户详情</span></dt>
              <dd>我的账户明细</dd>
              <dd><span>看看我赚了多少钱</span></dd>
            </dl>
          </a>
        </div>
         <div class="item-content" >
          <a class="item col-xs-6" href="/wap/member/myBounty">
            <i class="icon icon-redbag"></i>
            <dl>
              <dt><span>红包</span></dt>
              <dd>我的红包</dd>
              <dd><span>查看更多</span></dd>
            </dl>
          </a>
        </div>
      </div>
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
  </div>
</div>


<!-- 网页底部 -->
<jsp:include page="../common/bottomPage.jsp">
  <jsp:param name="bright_personal" value="active" />
</jsp:include>