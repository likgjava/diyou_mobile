<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>修改支付密码</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="setPaypwdCtrl" >
  <div class="page-content">
  <div class="page-view">
    <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
                       
          <div class="form-group">
            <input type="password" class="form-control" ng-model="PaypwdForm.raw" placeholder="原支付密码" required="" name="oldPaypwd" />
          </div>
          <div class="form-group">
            <input type="password" class="form-control" ng-model="PaypwdForm.newPwd" placeholder="输入新支付密码" required="" name="newPaypwd" />
          </div>
          <div class="form-group">
            <input type="password" class="form-control" ng-model="PaypwdForm.confirmPwd" placeholder="确认新支付密码" required="" name="cnewPaypwd" />
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.setPaypwdForm()" ng-disabled="setBtn">确认修改</a>
          </div>
        </form>
      </div>
  </div> 
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
    <div class="loader-inner"></div>
  </div>
</section>