<%@ page language="java" pageEncoding="UTF-8"%>
<div ng-controller="editPhoneCtrl">
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span ng-bind="editPhoneTit"></span>
  </div>
</nav>
<section class="page">
  <div class="page-content">
      <div class="page-view" ng-if="updatePhoneStep==1">
          <div class="ui-form reset-form" >
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                <div class="form-group">
                  <input type="text" class="form-control" ng-model="phone" readonly="" />
                </div>
                <div class="form-group">
                  <a class="btn btn-block btn-lg btn-blue" ng-click="editPhone()">修改手机</a>
                </div> 
              </form>
            </div>
        </div> 
        <!-- 修改手机号第2步 -->
        <div class="page-view" ng-if="updatePhoneStep==2">
          <div class="ui-form reset-form">
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                <div class="form-group">
                  <!-- <span class="g9">原手机号</span> -->
                  <span ng-bind="phone"></span>
                </div>
                <div class="form-group m0">                  
                    <div class="form-group-code">
                      <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="phoneForm2.code" required="" name="phoneCode"/>
                    </div>
                    <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode({'type':'reset'})" ng-disabled="isSend"></a>
                  </div>
                  <div class="form-group">
                    <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
                  </div>
                  <div class="form-group">
                    <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.editPhone2()" ng-disabled="btnStatus">下一步</a>
                  </div>
                  <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
                </form>
            </div>
        </div> 
        <!-- 修改手机号第3步 -->
        <div class="page-view" ng-if="updatePhoneStep==3">
          <div class="ui-form reset-form">
              <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">  
                  <div class="form-group ">    
                      <input type="text" class="form-control" value="" placeholder="请输入您要绑定的手机号码" ng-model="phoneForm3.phone" required="" name="phone"/>
                  </div>
                  <div class="form-group m0">                  
                    <div class="form-group-code">
                      <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="phoneForm3.phone_code" required="" name="phoneCode"/>
                    </div>
                    <a class="btn btn-blue btn-code" ng-bind="getCodeVal2" ng-click="getPhoneCode2({'type':'approve'})" ng-disabled="isSend2"></a>
                  </div>
                  <div class="form-group">
                    <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
                  </div>
                  <div class="form-group">
                    <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.editPhone3()" ng-disabled="btnStatus">完成修改</a>
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