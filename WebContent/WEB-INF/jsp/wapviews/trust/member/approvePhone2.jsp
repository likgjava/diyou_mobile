<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>手机绑定</span>
  </div>
</nav>
<section class="page" ng-controller="bindPhoneCtrl">
  <div class="page-content">
    <div class="page-view">
      <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <span class="g9">手机号码</span>
            <span ng-bind="phone"></span>
          </div>
          <div class="form-group m0">                  
            <div class="form-group-code">
              <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="phoneForm2.phone_code" required="" name="phoneCode"/>
            </div>
            <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode()" ng-disabled="isSend"></a>
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.bindPhone2()">确认绑定</a>
          </div>
          <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
        </form>
      </div>
      
    </div>
  </div>
</section>