<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">实名认证</span>
</nav>      
<div class="page page-user" ng-controller="realnameCtrl">
  <div class="page-content"> 

    <!-- 实名认证 -->
    <div class="page-view">
      <div class="ui-form reset-form">
         <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm" id="realForm">                
          <div class="form-group c-red" ng-if="isNotice">绑定银行卡需先实名认证</div>
          <div class="form-group">
            <input type="text" class="form-control" placeholder="请输入姓名" required="" name="realname" ng-model="formData.realname" />
          </div>
          <div class="form-group">
            <input type="text" class="form-control" placeholder="请输入身份证号"  required="" name="card_id" ng-model="formData.card_id"/>
          </div>
          <c:if test="${is_realname_open == '1'}">
          <div class="form-upload row">
            <div class="col col-xs-6">                
              <div class="upfile">                
                <div class="tit"><i class="iconfont">&#xe61c;</i>身份证正面</div>
                <div class="pic"></div>
                <input class="file" type="file" name="front">
              </div>
              
            </div>
            <div class="col-xs-6">
              <div class="upfile">                
                <div class="tit"><i class="iconfont">&#xe61e;</i>身份证反面</div>
                <div class="pic"></div>
                <input class="file" type="file" name="verso">
              </div>
            </div>
          </div>
          </c:if>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.realForm()">开始认证</a>
          </div>
        </form>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
    </div>
  </div>
</div>