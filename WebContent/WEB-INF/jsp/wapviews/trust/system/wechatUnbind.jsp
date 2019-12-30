<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <%-- <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a> --%>
    <span>解绑账号</span>
  </div>
</nav>
<section class="page">
  <div class="page-content">
    <div class="page-view" ng-controller="unbindopenidCtrl">
      <div class="ui-form reset-form">

        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group"><span>用户名：${user }</span></div>
          <div class="form-group"><a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.unbindForm()">解绑</a></div>
          <div class="form-group">  
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
        </form>
      </div>
    </div>
  </div>
</section>