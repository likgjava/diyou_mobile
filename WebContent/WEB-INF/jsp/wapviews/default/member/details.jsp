<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <a onclick="javascript:window.location.href = 'wap/member/index'" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">账户详情</span>
</nav>      
<div class="page page-user" ng-controller="uDetailsCtrl">
  <div class="page-content user-details"> 

    <!--用户信息-->
    <div class="list-block">
      <div class="list-title">用户信息</div>
      <ul>
      <li>
      <%--/wap/member/center --%>
        <a href="javascript:void(0);" class="item-content">
          <div class="col-xs-5 item-title "><i class="icon icon-user"></i>用户信息</div>
          <div class="col-xs-7 item-value c-blue" ng-bind="account.member_name"></div>
          <%--
          <i class="item-arrow iconfont">&#xe614;</i>
           --%>
        </a>
      </li>
      <li>
        <a class="item-content" ng-href="/wap/bank/index">
          <div class="col-xs-5 item-title"><i class="icon icon-bank"></i>银行卡管理</div>
          <div class="col-xs-7 item-value c-blue">
            <span ng-bind="account.bankName"></span>
            <span ng-bind="account.account"></span>
          </div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      <%-- 
      <li>
        <a ng-click="pcNotice()" class="item-content">
          <div class="col-xs-5 item-title"><i class="icon icon-cust"></i>资产信息</div>
          <div class="col-xs-7 item-value g9">查看</div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li> --%>
      </ul>          
    </div> 

    <!--安全认证-->
    <div class="list-block">
      <div class="list-title">安全认证</div>
      <ul>
      <li>
        <a ng-href="{{account.is_phone.url}}" class="item-content">
          <div class="col-xs-5 item-title"><i class="icon icon-phone"></i>手机绑定</div>
          <div class="col-xs-7 item-value c-blue" ng-bind="account.is_phone.name"></div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      <li>
        <a ng-href="{{account.is_email.url}}" class="item-content">
          <div class="col-xs-5 item-title"><i class="icon icon-email"></i>邮箱认证</div>
          <div class="col-xs-7 item-value c-blue" ng-bind="account.is_email.name"></div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      <li>
        <a ng-href="{{account.is_realname.url}}" class="item-content">
          <div class="col-xs-5 item-title">
            <i class="icon icon-real"></i>实名认证
          </div>
          <div class="col-xs-7 item-value c-blue" ng-bind="account.is_realname.name"></div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      <%--
      <li>
        <a ng-click="pcNotice()" class="item-content">
          <div class="col-xs-5 item-title">
            <i class="icon icon-data"></i>材料认证
          </div>
          <div class="col-xs-7 item-value g9">查看</div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      --%>
      </ul>          
    </div>

    <!--密码设置-->
    <div class="list-block">
      <div class="list-title">密码设置</div>
      <ul>
      <li>
        <a href="/wap/member/editpwd" class="item-content">
          <div class="col-xs-5 item-title"><i class="icon icon-pass"></i>登录密码</div>
          <div class="col-xs-7 item-value c-blue">修改</div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      <!-- <li>
        <a href="" class="item-content">
          <div class=" col-xs-5 item-title"><i class="icon icon-pass2"></i>手势密码</div>
          <div class=" col-xs-7 item-value c-blue">修改</div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li> -->
      <li>
        <a ng-href="{{account.paypassword.url}}" class="item-content">
          <div class="col-xs-5 item-title"><i class="icon icon-pass3"></i>支付密码</div>
          <div class="col-xs-7 item-value c-blue" ng-bind="account.paypassword.name"></div>
          <i class="item-arrow iconfont">&#xe614;</i>
        </a>
      </li>
      </ul>          
    </div>

  </div>
</div>