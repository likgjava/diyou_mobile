<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>登录</span>
  </div>
</nav>
<script type="text/javascript">
	var account = '${accounts}'
</script>
<section ng-cloak class="page" ng-controller="login2Ctrl" >
  <div class="page-content">
  <div class="page-view">
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
  </div> 
  </div> 
</section>