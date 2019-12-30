<%@ page language="java" pageEncoding="UTF-8"%>
<style>
    .reset-form {max-width: 6.4rem;overflow-x:hidden;}
  .reset-form .form-group{width: 94%;margin:0.2rem auto;position: relative;overflow-x:hidden;}
  .reset-form .form-group label{width: 0.71rem;height:0.71rem;border:1px solid #eee;float: left;display: inline-block;text-align:center;line-height: 0.71rem;}
  .reset-form .form-group .form-control {padding: 0px 0rem 0 0.35rem;-moz-box-sizing: content-box;-webkit-box-sizing: content-box;box-sizing: content-box;-moz-box-shadow: none;-webkit-box-shadow: none;box-shadow: none;border: 1px solid  #eee;border-left:none; width: 80%;background: #fff; height: 0.68rem;}
  .reset-form .form-group a.eyes{position: absolute;right: 0.35rem;z-index: 2;top:0.2rem;color:#999;}
  .reset-form .form-group a.forgetp{font-size: 0.14rem;line-height: 0.25rem;color:#666;}
</style>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>登录</span>
  </div>
</nav>
<section ng-cloak class="page" ng-controller="login2Ctrl" style="background: #fff;" >
  <div class="page-content">
  <div class="page-view">
    <div class="ui-form reset-form">
        <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
          <div class="form-group">
            <label><i class="newIconfont">&#xe640;</i></label>
            <input type="text" name="account" placeholder="用户名/邮箱/手机号码" class="form-control" ng-model="login.account" required="">
          </div>
          <div class="form-group">
            <label><i class="newIconfont">&#xe6bb;</i></label>
            <input type="password" class="form-control" id="form-control" ng-model="login.password" placeholder="密码" required="" name="password" />
            <a class="eyes" id="eyes" onclick="hideShowPsw()"><i class="newIconfont" id="eye-pic">&#xe60a;</i></a>
          </div>
          <div class="form-group">
            <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
          </div>
          <div class="form-group mt15">
            <a class="btn btn-block btn-lg btn-blue" w5c-form-submit="vm.loginForm()">立即登录</a>
          </div>
          <div class="form-group newbot" style="text-align: center;">
            <a href="/wap/system/register2" class="forgetp" >注册账号<em style="margin-left:5px;font-style: normal;">|</em></a>
            <a href="/wap/system/searchPwd" class="forgetp">忘记密码</a>
          </div>
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

