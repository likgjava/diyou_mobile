<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="page page-user new-index" ng-controller="userindexCtrl">
	<nav class="nav-top navbar-fixed-top">
      <img ng-if="accountData.memberInfo.avatar" class="fl avatars" ng-src="{{accountData.imgPath}}{{accountData.memberInfo.avatar}}" alt=""><!-- {{accountData.imgPath}}{{accountData.memberInfo.avatar}} -->
      <img ng-if="!accountData.memberInfo.avatar" class="fl avatars" src="wapassets/trust/images/member/avatar.png" alt="">
      <span class="name" ng-bind="accountData.member_name"></span>
      <a class="email" href="/wap/message/list"><i class="newIconfont">&#xe659;</i><span class="num ng-binding" ng-bind="message"></span></a>
	</nav>
  <div class="page-content user-index" style="padding-top:40px;padding-bottom: 50px;overflow-y: auto;">
  	<c:choose>
		<c:when test="${isTrustAccountOpen == 1 || groupStatus == 1 || groupStatus == 2}">
		</c:when>
		<c:otherwise>
		      <div class="user-nopen">
                <span>您还未开通第三方支付账号？<a ng-click="OpenTrust();">立即开通</a></span>
              </div>
		</c:otherwise>
	</c:choose>
      <div class="user-data">
        <a href="/wap/member/account" class="user-data-row user-data-rate">
          <div class="val" ng-bind="interest_award | number:2"></div>
          <div class="name"><i class="newIconfont">&#xe626;</i>总累计收益(元)</div>
        </a>
        <div class="user-data-row new-row">
          <div class="user-data-total">
            <div class="val" ng-bind="total_amount | number:2">***</div>
            <div class="name">账户总资产(元)</div>
          </div>
          <div class="user-data-income">
            <div class="val" ng-bind="balance_amount | number:2">***</div>
            <div class="name">可用余额(元)</div>
          </div>
        </div>
      </div>
      <div class="user-opts btns">
        <ul>
        <li>
          <a ng-href="/wap/member/cash">提现</a>
        </li>
        <li>
          <a class="recharge" ng-click="recharge()">充值</a>
        </li>
        </ul>
      </div>
      <div class="user-nav">
        <ul>
          <li>
            <a href="/wap/member/mytender">
              <i class="newIconfont left-icon">&#xe608;</i>
              <span>我的项目</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>
          </li>
          <li>
            <a href="/wap/account/accountLog">
              <i class="newIconfont left-icon">&#xe60e;</i>
              <span>交易记录</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>
          </li>
          <li>
            <a href="/wap/spread/mySpread">
              <i class="newIconfont left-icon">&#xe91a;</i>
              <span>我的推广</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>
          </li>
          <li>
            <a href="/wap/member/myaccountdata">
              <i class="newIconfont left-icon">&#xe625;</i>
              <span>账户详情</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>
          </li>
          <li>
            <a href="/wap/member/myBounty">
              <i class="newIconfont left-icon">&#xe6cc;</i>
              <span>现金红包</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>
            </li>
          <li>
              <a ng-click="risk()">
              <i class="newIconfont left-icon">&#xe601;</i>
              <span>风险测评</span>
              <i class="fr newIconfont">&#xe768;</i>
            </a>

          </li>
        </ul>
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
  <jsp:param name="light" value="listimg4" />
</jsp:include>
