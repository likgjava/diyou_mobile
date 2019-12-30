<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>找回密码</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="searchPwdCtrl" >
  <div class="page-content">
  <div class="page-view">
    <!-- 手机找回 -->
    <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <input type="hidden" ng-model="search.type" >
            <input type="phone" name="phone" placeholder="请输入手机号" class="form-control" ng-model="search.phone" required="" ng-pattern="/^(?:13\d|14\d|15\d|17\d|18\d)\d{5}(\d{3}|\*{3})$/" ng-if="search.type=='phone'">
            <input type="email" name="email" placeholder="请输入邮箱地址" class="form-control" ng-model="search.email" required="" ng-if="search.type=='email'">
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.searchNext()">下一步</a>
          </div>
        </form>
    </div>

    <div class="loginbar navbar-fixed-bottom">
      <a class="btn" ng-click="searchType('phone')" ng-class="{on:search.type=='phone'}">手机找回</a>
      <a class="btn" ng-click="searchType('email')" ng-class="{on:search.type=='email'}">邮箱找回</a>
    </div> 

  </div> 
  </div> 
</section>