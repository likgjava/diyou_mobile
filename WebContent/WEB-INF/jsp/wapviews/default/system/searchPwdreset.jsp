<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>设置新的登录密码</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="searchPwdresetCtrl" >
  <div class="page-content">
  <div class="page-view">
     <!-- 重置密码 -->
    <div class="ui-form reset-form">
      <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
        <div class="form-group">
            <input type="password" name="newPwd" class="form-control" ng-model="search.password" placeholder="设置新密码" required=""  ng-minlength="6" ng-maxlength="16"/>
          </div>
          <div class="form-group">
            <input type="password" name="cnewPwd" class="form-control" ng-model="search.against_password" placeholder="确认新密码" required=""/>
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.setPwdForm()" ng-disabled="setBtn">确认修改</a>
          </div>
      </form>
    </div>

  </div>
  </div>
</section>
