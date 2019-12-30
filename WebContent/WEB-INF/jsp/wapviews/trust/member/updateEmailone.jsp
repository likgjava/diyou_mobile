<%@ page language="java" pageEncoding="UTF-8"%>
<div ng-controller="editEmailCtrl">
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span ng-bind="editEmailTit"></span>
  </div>
</nav>
<section class="page">
  <div class="page-content">
      <div class="page-view" ng-if="updateEmailStep==1">
          <div class="ui-form reset-form" >
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                <div class="form-group">
                  <input type="text" class="form-control" ng-value="email" readonly="" />
                </div>
                <div class="form-group">
                  <a class="btn btn-block btn-lg btn-blue" ng-click="editEmail()">修改邮箱</a>
                </div> 
              </form>
            </div>
        </div> 
        <div class="page-view" ng-if="updateEmailStep==2">
          <div class="ui-form reset-form">
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                <div class="form-group">
                  <span ng-bind="email"></span>
                </div>
                <div class="form-group m0">                  
                    <div class="form-group-code">
                      <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="emailForm2.code" required="" name="phoneCode"/>
                    </div>
                    <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getCode({'type':'reset'})" ng-disabled="isSend"></a>
                  </div>
                  <div class="form-group">
                    <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
                  </div>
                  <div class="form-group">
                    <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.editemail2()">确认绑定</a>
                  </div>
                  <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
                </form>
            </div>
        </div> 
        <!-- 修改手机号第3步 -->
        <div class="page-view" ng-if="updateEmailStep==3">
          <div class="ui-form reset-form">
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                  <div class="form-group ">    
                      <input type="text" class="form-control" value="" placeholder="请输入您要绑定的邮箱" ng-model="emailForm3.email" required="" name="email"/>
                  </div>
                  <div class="form-group m0">                  
                    <div class="form-group-code">
                      <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="emailForm3.code" required="" name="phoneCode"/>
                    </div>
                    <a class="btn btn-blue btn-code" ng-bind="getCodeVal2" ng-click="getCode2({'type':'again'})" ng-disabled="isSend2"></a>
                  </div>
                  <div class="form-group">
                    <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
                  </div>
                  <div class="form-group">
                    <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.editemail3()" ng-disabled="btnStatus">完成修改</a>
                  </div>
                  <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
                </form>
            </div>
        </div>
  </div>
  <!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
    <div class="loader-inner"></div>
  </div> 
</section>
</div>