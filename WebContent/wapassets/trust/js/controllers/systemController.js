define(function(require, exports, module) {
    function isEmpty(obj) {
        for (var name in obj) {
            return true;
        }
        return false;
    };
    var dySystem = angular.module('dySystem', ['w5c.validator','dylayer.m']);
    module.exports = dySystem;
    dySystem.config(["w5cValidatorProvider", function (w5cValidatorProvider) {

        // 全局配置
        //全局验证
        w5cValidatorProvider.config({
            blurTrig: false,
            showError: true,
            removeError: true

        });

        w5cValidatorProvider.setRules({
            account:{
                required: "请输入手机号/账号/邮箱"
            },
            email: {
                required: "输入的邮箱地址不能为空",
                email: "输入邮箱地址格式不正确"
            },
            phone: {
                required: "请输入您的手机号码",
                pattern: "手机号码不正确"
            },
            username: {
                required: "请输入用户名",
                pattern: "用户名4-16位字母数字组合,以字母开头"
            },
            password: {
                required: "请输入密码",
                minlength: "密码长度不符合6-16位限制",
                maxlength: "密码长度不符合6-16位限制"
            },
            cPassword: {
                required: "请再输入密码",
                repeat: "两次输入的密码不一致"
            },
            phoneCode: {
                required: "请输入验证码"
            },
            emailCode: {
                required: "请输入验证码"
            },
            keywords: {
                required: "请输入用户名/手机号码"
            },
            valicode: {
                required: "请输入验证码"
            },
            loginPwd:{
                required: "请输入登录密码"
            },
            Paypwd:{
                required: "请输入支付密码"
            },
            cPaypwd:{
                required: "请输入确认支付密码"
            },
            oldPaypwd:{
                required: "请输入原支付密码"
            },
            newPaypwd:{
                required: "请输入新支付密码"
            },
            cnewPaypwd:{
                required: "请输入确认新支付密码"
            },
            oldPwd:{
                required: "请输入原密码"
            },
            newPwd:{
                required: "请输入新密码",
                minlength: "密码长度不符合6-16位限制",
                maxlength: "密码长度不符合6-16位限制"
            },
            cnewPwd:{
                required: "请输入确认新密码",
                repeat: "两次输入的密码不一致"
            },
            realname:{
                required:"请输入您的姓名"
            },
            card_id:{
                required:"请输入您的身份证号码"
            },
            bankid:{
                required:"请输入银行卡卡号",
                pattern: "请输入正确的银行卡卡号"
            },
            bank:{
                required:"请选择开户行"
            },
            province:{
                required:"请选择省份"
            },
            city:{
                required:"请选择城市"
            },
            bankname:{
                required:"请输入开户行的名称"
            },
            oldBankid:{
                // required:"请输入原卡号",
                required:"请输入原卡号",
                pattern: "请输入正确的银行原卡号"
            },
            newBankid:{
                // required:"请输入新卡号",
                required:"请输入正确的银行新卡号",
                pattern: "请输入正确的银行新卡号"
            },
            checkNewBankid:{
                required:"请确认新卡号",
                repeat: "两次输入的银行卡号不一致"
            },
            twoDecimal: {
            	required: "充值金额不能为空",
                pattern: "充值金额必须为整数或保留两位小数"
            },
            accountTwoDecimal: {
            	required: "请输入投资金额",
                pattern: "投资金额必须为整数或保留两位小数"
            },
            aprTwoDecimal: {
            	required: "请输入借款利率",
                pattern: "借款利率必须为整数或保留两位小数"
            },
            periodTwoDecimal: {
                required: "请输入投资期限",
                pattern: "投资期限必须为整数"
            },
            rechargeType:{
                required:"请选择充值类型"
            }
        });
    }]);

    dySystem.controller('MobileMain', function($scope, $http, ngDialog, $timeout,$injector) {
        $.cookie('rebackUrl',"");
        //弹窗提示到pc端操作
        $scope.pcNotice = function(){
            //注入弹窗服务
            var $layer = $injector.get('$dylayer');
            $layer.alert("请到PC端进行此操作",800);
        }

        //联系客服 - 显示
        $scope.contactsShow = function(){
            $('body').addClass('hotline-cover');
        }
        //联系客服 - 隐藏
        $scope.contactsHide = function(){
            $('body').removeClass('hotline-cover');
        }

        //支付密码 - 显示
        $scope.playPwdShow = function(){
            $('body').addClass('cover');
        }
        //支付密码 - 隐藏
        $scope.playPwdHide = function(){
            $('body').removeClass('cover');
        }

        //退出登录
        $scope.loginOut = function(){
            //注入弹窗服务
            var $layer = $injector.get('$dylayer');
            $layer.confirm({
                msg:"确认是否退出登录？",
                yes:function(){
                    window.location.href="wap/member/exit";
                }
            });
        }
        //倒计时
        $scope.countDown = function(options){
            var defaults = {
                time: 59
            };
            var opt = angular.extend({},defaults,options);
            var i = opt.time;

            $scope = this;
            $scope.isSend = true;
            $scope.getCodeVal = "重新获取("+i+")";

            var timer = setInterval(function(){
                i--;
                if(i<0){
                    $scope.isSend = false;
                    $scope.getCodeVal = "重新获取";
                    clearInterval(timer);
                }else{
                    $scope.getCodeVal = "重新获取("+i+")";
                }
                $scope.$digest();
            },1000);
        }

        //新的倒计时方法
        //倒计时
        $scope.newCountdown = function(options){
            var defaults = {
                time: 59,
                disVar:"isSend", //禁止按钮重复提交的变量,ng-disabled
                getBtn:"getCodeVal" //获取验证码的变量
            };
            var opt = angular.extend({},defaults,options);
            var i = opt.time;

            $scope = this;
            $scope[opt.disVar] = true;
            $scope[opt.getBtn] = "重新获取("+i+")";

            var timer = setInterval(function(){
                i--;
                if(i<0){
                    $scope[opt.disVar] = false;
                    $scope[opt.getBtn] = "重新获取";
                    clearInterval(timer);
                }else{
                    $scope[opt.getBtn] = "重新获取("+i+")";
                }
                $scope.$digest();
            },1000);
        }
        //普通表单提交处理方式
        $scope._submitFn = function(url,data,callback){
            $http({
                method: 'post',
                url: url,
                data: $.param(data),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).success(function(_data) {
                callback(_data);
            });

        }

        //错误提示
        $scope.showError = function(message,options){
            // var defaults = {
            //     time: 2000
            // };
            // var opt = angular.extend({},defaults,options);
            // var time = opt.time;

            // $scope = this;
            // $scope.error = true;
            $scope.errorWord = message;

            // setTimeout(function(){
            //     $scope.error = false;
            //     $scope.errorWord = "";
            //     $scope.$digest();
            // }, time);
        }

        //识别银行卡类型
        $scope.bankDetection = function(){
            console.log('bankDetection');
        };
    });


    //新的控制器开始
    //注册或登录
    dySystem.controller('regloginCtrl',['$scope','$http','$injector','$location',function($scope,$http,$injector,$location){
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.register = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.regloginForm = function () {

            var url = "/wap/system/reglogin";
            var data = angular.extend($scope.register,$scope.typeId);
            var callback = function(_data){
                if(_data.code == "100"){
                    $scope.showError(_data.description);
                }else{
                    $.cookie('account', $scope.register.account);
                    if(_data.code == "1"){
                        window.location = "/wap/system/register2";
                    }else if(_data.code == "2"){
                        window.location = "/wap/system/login";
                    }else if(_data.code == "3"){
                        window.location = "/wap/system/login2";
                    }
                }
            }
            $http({
                method: 'post',
                url: url,
                data: $.param(data),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).success(function(_data) {
                callback(_data);
            });
        };

    }]);


    //注册
    dySystem.controller('wechatRegCtrl',['$scope','$http','$injector','$location',function($scope,$http,$injector,$location){
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.register = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };
        vm.regloginForm = function () {
            var url = "/wap/system/wechatRegisterSub";
            var data = angular.extend($scope.register,$scope.typeId);
            var callback = function(_data){
                if(_data.code == "100"){
                    $scope.showError(_data.description);
                }else{
                    $.cookie('account', $scope.register.account);
                    if(_data.code == "1"){
                        window.location = "/wap/system/register2";
                    }
                }
            }
            $http({
                method: 'post',
                url: url,
                data: $.param(data),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).success(function(_data) {
                callback(_data);
            });
        };
    }]);

     /**
     * 微信账号绑定
     */
    dySystem.controller('regloginWechatCtrl',['$scope','$http','$injector','$location',function($scope,$http,$injector,$location){
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.register = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        $scope.showError = function(message,options){
            $scope.errorWord = message;
        }

        vm.regloginForm = function () {
            var url = "/wechat/system/bindopenid";
            var data = angular.extend($scope.register,$scope.typeId);
            var callback = function(_data){
                if(_data.status == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.description,800,function(){
                        window.location = "/wechat/member/index";
                    });
                }else{
                    $scope.showError(_data.description);
                }
            }
            $http({
                method: 'post',
                url: url,
                data: $.param(data),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).success(function(_data) {
                callback(_data);
            });
        };

    }]);

   /**
     * 微信账号解绑
     */
    dySystem.controller('unbindopenidCtrl',['$scope','$http','$injector','$location','postUrl',function($scope,$http,$injector,$location,postUrl){
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.register = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        $scope.showError = function(message,options){
            $scope.errorWord = message;
        }

        vm.unbindForm = function () {
            var url = "/wechat/system/unbindopenid";
            var data = angular.extend($scope.register,$scope.typeId);
            var callback = function(_data){
                if(_data.status == "200"){
                    var $layer = $injector.get('$dylayer'),
                    wx = require('http://res.wx.qq.com/open/js/jweixin-1.0.0.js');
                    $layer.alert(_data.description,800,function(){
         //           	window.location = "/wechat/system/bindLogin";
                    	var val=location.href.split('#')[0];
                        	$scope.jsapi={
                        		url:val
                        	},
                         postUrl.events('/wechat/jsapi',$scope.jsapi).success(function(_jsdata) {
                        	 wx.config({
    							debug: false,
    							appId: _jsdata.appId,
    							timestamp: _jsdata.timestamp,
   								nonceStr: _jsdata.nonceStr,
    							signature: _jsdata.signature,
    							jsApiList: ['closeWindow']
							});
	                         wx.ready(function(){
								wx.closeWindow();
                     		});
		                });
                   });
                }else{
                     $scope.showError(_data.description);
                }
            }
            $http({
                method: 'post',
                url: url,
                data: $.param(data),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).success(function(_data) {
                callback(_data);
            });
        };

    }]);

    //注册(先不做推荐人)
    dySystem.controller("register2Ctrl",['$scope','$http','$injector',function($scope,$http,$injector){
        //注册账号
        $scope.account = $.cookie('account');

        $scope.register2 = {referrer:invite};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.register2Form = function () {
        	$scope.register2.account = $.cookie('account');
            var url = "/wap/system/register2";
            var data = $scope.register2;
            var callback = function(_data){
                if(_data.code == "200"){
                    window.location = "/wap/member/index";
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };

        $scope.look = function(type){
        	var $layer = $injector.get('$dylayer');
            var url = "/wap/loan/loanProtocol?type=" + type ;
            $layer.page({
                type:2,
                url: url
            });
        };


        //获取手机验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getPhoneCode = function(phone){
            var data = {"type":"reg", "phone":phone};
            var url = "/wap/system/sendsms";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向手机<span>"+_data.data+"</span>发送短信，请输入短信验证码完成注册。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }
    }])

    //登录
    dySystem.controller('loginCtrl',['$scope','$http','$injector',function($scope,$http,$injector){

        //登录账号
        $scope.account = $.cookie('account');
        //登录方式，默认验证码登录
        $scope.currentDiv = "type2.html";
        $scope.login = {"logintype":2};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.loginForm = function () {

            var url = "/wap/system/login";
            var data = $scope.login;
            var callback = function(_data){
                if(_data.code == "200"){
                    window.location = "/wap/member/index";
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);

        };

        //登录方式切换
        $scope.loginType = function(typeid){
            $scope.login = {"logintype":typeid};
            $scope.currentDiv = "type"+typeid+".html";
        }

        //获取手机验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getPhoneCode = function(){
            var data = {"type":"login","name":$scope.account};
            var url = "/wap/system/sendsms";

            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";

            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向手机<span>"+_data.data+"</span>发送短信，请输入短信验证码完成验证。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }
    }]);

    //登录2
    dySystem.controller('login2Ctrl',['$scope','$http','$injector',function($scope,$http,$injector){
        $scope.account = $.cookie('account');
        $scope.login = {"logintype":2};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.loginForm = function () {

            var url = "/wap/system/login";
            var data = $scope.login;
            var callback = function(_data){
                if(_data.code == "200"){
                    window.location = "/wap/member/index";
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };

    }]);


    //找回密码
    dySystem.controller('searchPwdCtrl',['$scope','$http','$injector',function($scope,$http,$injector){

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        $scope.search = {};
        $scope.search.type = "phone";
        vm.searchNext = function () {
            var url = "/wap/system/searchPwd";
            var data = $scope.search;
            var callback = function(_data){
                if(_data.code == "200"){
                    if($scope.search.type == "phone"){
                        window.location = "/wap/system/searchPwdphone";
                    }else{
                        window.location = "/wap/system/searchPwdemail";
                    }
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };

        //找回类型
        $scope.searchType = function(type){
            if(type == 'phone'){
                $scope.search.type = "phone";
                $scope.search.phone ='';
                $scope.showError('');
                $scope.validateForm.$errors[0]='';
            }else{
                $scope.search.type = "email";
                $scope.search.email ='';
                $scope.showError('');
                $scope.validateForm.$errors[0]='';
            }
        }
    }]);

    //手机找回密码
    dySystem.controller('searchPwdphoneCtrl',['$scope','$http','$injector',function($scope,$http,$injector){

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.searchForm = function () {

            var url = "/wap/system/searchPwdphone";
            var data = $scope.search;
            var callback = function(_data){
                if(_data.code == "200"){
                    window.location = "/wap/system/searchPwdreset";
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };


        //获取手机验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getPhoneCode = function(){
            var data = {"type":"pwd"};
            var url = "/wap/system/sendsms";

            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向手机<span>"+_data.data+"</span>发送短信，请输入短信验证码完成验证。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }
    }]);

    //邮箱找回密码
    dySystem.controller('searchPwdemailCtrl',['$scope','$http','$injector',function($scope,$http,$injector){

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.searchForm = function () {

            var url = "/wap/system/searchPwdemail";
            var data = $scope.search;
            var callback = function(_data){
                if(_data.code == "200"){
                    window.location = "/wap/system/searchPwdreset";
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };


        //获取邮箱验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getEmailCode = function(){
            var data = {"type":"password"};
            var url = "/wap/member/sendemail";

            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向邮箱<span>"+_data.data+"</span>发送验证码，请输入验证码完成验证。"
                    $scope.showError("");
                }else{
                    $scope.showError(_data.description);
                    $scope.isSend = false;
                    $scope.smsNotice = "" ;
                    $scope.getCodeVal = "重新获取";
                }
            }
            $scope._submitFn(url,data,callback);
        }
    }]);

    //重置密码
    dySystem.controller('searchPwdresetCtrl',['$scope','$http','$injector',function($scope,$http,$injector){

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.setPwdForm = function () {
            var url = "/wap/system/searchPwdreset";
            var data = $scope.search;
            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.description,800,function(){
                        window.location = "/wap/system/login2";
                    });
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback)
        };
    }]);
})
