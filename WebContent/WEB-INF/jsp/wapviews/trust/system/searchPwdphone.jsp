<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>找回密码</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="searchPwdphoneCtrl" >
  <div class="page-content">
  <div class="page-view">
    <!-- 手机找回 -->
    <div class="ui-form reset-form">
      <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
        <div class="form-group">
          <div class="form-group-code">
            <input type="text" class="form-control" placeholder="请输入您的验证码" ng-model="search.phoneCode" required="" name="phoneCode">
          </div>
          <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode()" ng-disabled="isSend"></a>
        </div>
        <div class="form-group">
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
        </div>
        <div class="form-group">
          <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.searchForm()">下一步</a>
        </div>
        <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
      </form>
    </div>

  </div> 
  </div> 
</section>