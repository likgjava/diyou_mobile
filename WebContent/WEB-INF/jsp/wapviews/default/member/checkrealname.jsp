<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">实名认证</span>
</nav>      
<div class="page page-user">
  <div class="page-content" ng-controller="checkrealnameCtrl"> 

    <!-- 实名认证 -->
    <div class="page-view">
      <div class="ui-form reset-form">
         <form role="form">                
          <div class="form-group">
            <input type="text" class="form-control" required="" name="realname" readonly="" ng-value="user.realname" />
            <span class="form-right c-blue" ng-bind="user.status"></span>
          </div>
          <div class="form-group">
            <input type="text" class="form-control" readonly=""  ng-value="user.card_id" />
          </div>
          <c:if test="${is_realname_open == '1'}">
          <div class="form-upload row">
            <div class="col col-xs-6">                
              <div class="upfile">                
                <div class="tit"><i class="iconfont">&#xe61c;</i>身份证正面</div>
                <div class="pic"><img ng-src="{{user.positive_card}}" /></div>
              </div>
              
            </div>
            <div class="col-xs-6">
              <div class="upfile">                
                <div class="tit"><i class="iconfont">&#xe61e;</i>身份证反面</div>
                <div class="pic"><img ng-src="{{user.back_card}}" /></div>
              </div>
            </div>
          </div>
          </c:if>
        </form>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
    </div>
  </div>

  
</div>