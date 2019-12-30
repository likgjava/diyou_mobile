<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
        <a onclick="location.href='/wap/member/myaccountdata'" class="go-back"><span class="iconfont"></span></a>
        <span>我的支付账号</span>
    </div>
</nav>
<div class="page-content my-trust" ng-controller="mytrustCtrl" style="padding-top:50px;background: #fff;">
    <div class="trust-info row">
        <div class="list">
            <span class="tit col-xs-3">托管类型</span>
            <span class="col-xs-9"><img ng-src="/wapassets/images/payment/pay/{{trust_info.paymentLog}}" height="25"></span>
        </div>
        <div class="list">
            <span class="tit col-xs-3">托管号</span>
            <span class="col-xs-9" ng-if="trust_info.trustAccount" ng-bind="trust_info.trustAccount"></span>
            <span class="col-xs-9" ng-if="!trust_info.trustAccount" style="color:red">查询失败/未开通</span>
        </div>
        <div class="list">
            <span class="tit col-xs-3">总金额</span>
            <span class="col-xs-9" ng-bind="(trust_info.balance|judge|number:2)+'元'"></span>
        </div>
        <div class="list">
            <span class="tit col-xs-3">可用金额</span>
            <span class="col-xs-9" ng-bind="(trust_info.availableamount|judge|number:2)+'元'"></span>
        </div>
        <div class="list">
            <span class="tit col-xs-3">冻结金额</span>
            <span class="col-xs-9" ng-bind="(trust_info.freezeamount|judge|number:2)+'元'"></span>
        </div>
        <!-- <div class="list" ng-if="isYeepay">
            <label class="tit col-xs-3">重置密码:</label>
            <span class="col-xs-9"><a class="a-link1" href="javascript:;" ng-click="mytrustReset()">重置密码</a></span>
        </div> -->
        <div class="list" ng-if="isYeepay">
            <label class="tit col-xs-3">修改手机号:</label>
            <span class="col-xs-9"><a class="a-link1" href="javascript:;" ng-click="resetMobile()">修改手机号</a></span>
        </div>
    </div>
    <div class="btn-box">
    	<a class="btn btn-lg btn-block btn-blue" ng-if="trust_info.isAuto!=="true" ng-click="authorizeAutoTransfer();" >二次授权</a>
<!--  <a class="btn btn-lg btn-block btn-blue" ng-if="trust_info.isAuto" ng-click="cancelauthorizeAutoTransfer();" >取消授权</a> -->
    </div>
    <!-- 加载状态 -->
    <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
    </div>
</div>
<script type="text/javascript">
var trust_info = ${trust_info};
var payment_log = '${payment_log}';
</script>
