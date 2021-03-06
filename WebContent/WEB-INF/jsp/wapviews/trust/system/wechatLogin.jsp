<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <%-- <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a> --%>
    <span>填写绑定账号</span>
  </div>
</nav>
<section class="page">
  <div class="page-content">
    <div class="page-view" ng-controller="regloginWechatCtrl">
      <div class="ui-form reset-form">

        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <input type="text" name="account" placeholder="请输入手机号/账号/邮箱" class="form-control" ng-model="register.account" required="">
          </div>
 		  <div class="form-group">
            <input type="password" name="password" placeholder="登录密码" class="form-control" ng-model="register.password" required="">
          </div>
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.regloginForm()">绑定</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</section>