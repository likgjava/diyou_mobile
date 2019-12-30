<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>修改登录密码</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="editPwdCtrl" >
  <div class="page-content">
  <div class="page-view">
    <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
                       
          <div class="form-group">
            <input type="password" class="form-control" ng-model="pwdForm.raw" placeholder="输入原密码" required="" name="oldPwd" />
          </div>
          <div class="form-group">
            <input type="password" class="form-control" ng-model="pwdForm.newPwd" placeholder="输入新密码" required="" name="newPwd" />
          </div>
          <div class="form-group">
            <input type="password" class="form-control" ng-model="pwdForm.confirmPwd" placeholder="确认新密码" required="" name="cnewPwd" />
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.editPwdForm()" ng-disabled="setBtn">确认修改</a>
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