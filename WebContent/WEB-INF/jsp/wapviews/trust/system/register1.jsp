<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a class="go-back" ng-click="history.go(-1)"><span class="iconfont">&#xe604;</span></a>
    <span>注册</span>
  </div>
</nav>
<section class="page">
  <div class="page-content">
    <div class="page-view">
      <div class="ui-form reset-form">
        <form role="form">
          <div class="form-group">
            <input type="tel" class="form-control" name="account"  value="" placeholder="请输入推荐人账号/手机号/邮箱">
          </div>
          <div class="form-group form-alert">
            请输入推荐人信息，如无可直接注册
          </div>
          <div class="form-group">
            <a href="/wap/system/register2" class="btn btn-block btn-lg btn-blue">直接注册</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</section>
