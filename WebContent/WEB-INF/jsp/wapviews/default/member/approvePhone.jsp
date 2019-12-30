<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>手机绑定</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="bindPhoneCtrl" >
  <div class="page-content">
  <div class="page-view">
    <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">           
          <div class="form-group">
            <input type="text" class="form-control" ng-model="phoneForm.phone" placeholder="请输入手机号" required="" name="phone" ng-pattern="/^(?:13\d|15\d|18\d|14\d|17\d)\d{5}(\d{3}|\*{3})$/" />
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.bindPhone()" ng-disabled="btnStatus">下一步</a>
          </div> 
        </form>
      </div>
  </div> 
  </div> 
</section>