<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>注册</span>
  </div>
</nav>
<section class="page" ng-controller="register2Ctrl">
  <div class="page-content">
    <div class="page-view">
      <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <span class="g9">注册账号</span>
            <span ng-bind="account"></span>
          </div>
          <div class="form-group mb1">                  
            <div class="form-group-code">
              <input type="text" class="form-control" value="" placeholder="请输入验证码" ng-model="register2.phone_code" required="" name="phoneCode"/>
            </div>
            <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode()" ng-disabled="isSend"></a>
          </div>
          <div class="form-group mb1">
            <input type="password" class="form-control" value="" placeholder="请输入6-15位，数字和字母组合的密码" ng-model="register2.password" required="" name="password" ng-minlength="6" ng-maxlength="15">
            <!-- <span class="iconfont visual">&#xe615;</span> -->
          </div>
          <div class="form-group form-agree">
            <label for="agree">注册即视为同意<a href="/wap/index/agreement">《网站服务协议》</a></label>
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.register2Form()">注册</a>
          </div>
          <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
        </form>
      </div>
      
    </div>
  </div>
</section>