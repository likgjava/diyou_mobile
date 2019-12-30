<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>填写验证码</span>
  </div>
</nav>
<section class="page" ng-controller="register2Ctrl">
  <div class="page-content">
    <div class="page-view">
      <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">

          <div class="form-group">
            <input type="password" class="form-control" value="" placeholder="请输入支付密码" ng-model="register2.password" required="" name="password" ng-minlength="6" ng-maxlength="12">
          </div>
          <div class="form-group">                  
            <div class="form-group-code">
              <input type="text" class="form-control" value="" placeholder="请输入您的验证码" ng-model="register2.phone_code" required="" name="phoneCode"/>
            </div>
            <a class="btn btn-blue btn-code" ng-bind="getCodeVal" ng-click="getPhoneCode()" ng-disabled="isSend"></a>
          </div>
          
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.register2Form()">立即绑定</a>
          </div>
          <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
        </form>
      </div>
      
    </div>
  </div>
</section>