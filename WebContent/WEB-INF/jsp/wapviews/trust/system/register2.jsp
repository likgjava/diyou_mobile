<%@ page language="java" pageEncoding="UTF-8"%>
<style>
  .reset-form {max-width: 6.4rem;overflow-x:hidden;}
  .reset-form .form-group{width: 94%;margin:0.2rem 3%;position: relative;overflow-x:hidden;}
  .reset-form .form-group label{width: 0.71rem;height:0.71rem;border:1px solid #eee;float: left;display: inline-block;text-align:center;line-height: 0.71rem;}
  .reset-form .form-group .form-control {padding: 0px 0rem 0 0.35rem;-moz-box-sizing: content-box;-webkit-box-sizing: content-box;box-sizing: content-box;-moz-box-shadow: none;-webkit-box-shadow: none;box-shadow: none;border: 1px solid  #eee;border-left:none; width: 80%;background: #fff; height: 0.68rem;}
  .reset-form .form-group a.eyes{position: absolute;right: 0.35rem;z-index: 2;top:0.2rem;color:#999;}
  .reset-form .form-group .btn-code1{background: #dddddd;color:#777;top: 0.08rem;right: 0.35rem;line-height: 0.5rem!important;padding:0 0.1rem!important;position: absolute;}
</style>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>注册</span>
  </div>
</nav>
<script type="text/javascript">
	var invite = '${invite}' ;
</script>
<section class="page" ng-controller="register2Ctrl" style="background: #fff;">
  <div class="page-content" style="padding-top:50px;">
    <div class="page-view">
      <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <!-- <span class="g9">注册账号</span>
            <span ng-bind="account"></span> -->
             <label><i class="newIconfont">&#xe673;</i></label>
             <input type="phone" name="phone" placeholder="手机号码" class="form-control" ng-model="register2.phone" required="">
          </div>
          <div class="form-group mb1">
            <label><i class="newIconfont">&#xe6bb;</i></label>
            <input type="password" class="form-control" id="form-control" value="" placeholder="登录密码" ng-model="register2.password" required="" name="password" ng-minlength="6" ng-maxlength="16">
             <a class="eyes" id="eyes" onclick="hideShowPsw()"><i class="newIconfont" id="eye-pic">&#xe60a;</i></a>
          </div>

          <div class="form-group mb1">
              <label><i class="newIconfont">&#xe621;</i></label>
              <input type="text" class="form-control" value="" placeholder="请输入验证码" ng-model="register2.phone_code" required="" name="phoneCode"/>
            <a class="btn btn-code1" ng-bind="getCodeVal" ng-click="getPhoneCode(register2.phone)" ng-disabled="isSend"></a>
          </div>
          <div class="form-group">
            <label><i class="newIconfont">&#xe625;</i></label>
            <input type="text" class="form-control" placeholder="邀请人用户名(选填)" name="referrer" ng-model="register2.referrer">
          </div>
          <div class="form-group form-agree">
            <span class="agree">注册即视为同意<a ng-click="look('reg')" style="color:#3388ff;">《网站服务协议》</a></span>
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.register2Form()">免费注册</a>
          </div>
           <div class="form-group newbot" style="text-align: center;">
            <span stlye="width:14px;color:#999;">已有账号？去<a href="/wap/system/login2" class="forgetp" >登录</a></span>
          </div>
          <div class="form-group form-alert" ng-bind-html="smsNotice"></div>
        </form>
      </div>

    </div>
  </div>
    <script type="text/javascript">
      var demoImg = document.getElementById("eyes");
      var demoInput = document.getElementById("form-control");
      function hideShowPsw() {
          if (demoInput.type == "password") {
              demoInput.type = "text";
              $("#eye-pic").html('&#xe620;')
          } else {
              demoInput.type = "password";
              $("#eye-pic").html('&#xe60a;')
          }
      }
</script>
</section>
