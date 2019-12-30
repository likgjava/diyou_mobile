<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a class="go-back" href="/wap/system/reglogin"><span class="iconfont">&#xe604;</span></a>
    <span>登录</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="loginCtrl" >
  <div class="page-content">
  <div class="page-view" ng-include="currentDiv">
    
  </div> 
    <script type="text/ng-template" id="type1.html">
        <div class="ui-form reset-form">
          <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
            <div class="form-group">
              <span class="g9">登录账号</span>
              <span ng-bind="account"></span>
            </div>
            <div class="form-group">
              <div class="form-group-code">
                <input type="text" class="form-control" placeholder="请输入您的验证码" ng-model="login.phoneCode" required="" name="phoneCode">
              </div>
              <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode()" ng-disabled="isSend"></a>
            </div>
            <div class="form-group">
              <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
            </div>
            <div class="form-group">
              <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.loginForm()">立即登录</a>
            </div>
            <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
            <div class="form-group form-foot">
              <a href="/wap/system/searchPwd" class="">忘记密码？</a>
            </div>
          </form>
        </div>
    </script>
    <script type="text/ng-template" id="type2.html">
      <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">                
          <div class="form-group">
            <span class="g9">登录账号</span>
            <span ng-bind="account"></span>
          </div>
          <div class="form-group">
            <input type="password" class="form-control" ng-model="login.password" placeholder="请输入您的注册时设置的密码" required="" name="password" />
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.loginForm()">立即登录</a>
          </div>
          <div class="form-group form-foot">
            <a href="/wap/system/searchPwd" class="">忘记密码？</a>
          </div>
        </form>
      </div>
    </script>
  </div>
  <div class="loginbar navbar-fixed-bottom">
    <a class="btn" ng-click="loginType(2)" ng-class="{on:login.logintype==2}">密码登录</a>
    <a class="btn" ng-click="loginType(1)" ng-class="{on:login.logintype==1}">验证码登录</a>
    
  </div> 
</section>