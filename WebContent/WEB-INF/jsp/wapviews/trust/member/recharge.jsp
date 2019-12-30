<%@ page language="java" pageEncoding="UTF-8"%>
<div ng-controller="rechargRecordCtrl">
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="location.href='/wap/member/index'" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>充值</span>
    <a class="link" href="/wap/recharge/rechargelog">充值记录</a>
  </div>
</nav>
<div class="page" ng-controller="rechargeCtrl">
  <div class="page-content">
    <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
    <div class="page-view">

      <div class="list-block">
        <ul>
          <li>
            <div class="item-content" ng-click="rechargeType()">
              <div class="item-title col-xs-4">充值类型</div>
              <div class="item-input col-xs-8">
                <input type="text" name="rechargeType" class="txt open-panel" name="pay-type" placeholder="请选择充值类型" readonly="true" ng-model="bankText" required>
                <input type="hidden" ng-model="rechargeTp.paymentType">
              </div>
              <div class="item-after iconfont">&#xe604;</div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">充值金额</div>
              <div class="item-input col-xs-8">
                <input type="number" name="twoDecimal" class="txt" name="pay-type" placeholder="请输入充值金额"  ng-model="rechargeTp.amount" required   ng-pattern="/^\d+(\.\d{1,2})?$/"  >
                <span class="unit">元</span>
              </div>
            </div>
          </li>

        <%--   <li class="mt10">
            <div class="item-content">
              <div class="item-title col-xs-4">手续费</div>
              <div class="item-input col-xs-8">
                <input type="tel" class="txt" name="pay-type" value="0" readonly="true" ng-model="getfee">
                <span class="unit">元</span>
              </div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">实际到账</div>
              <div class="item-input col-xs-8">
                <input type="tel" class="txt" name="pay-type" value="0" readonly="true" ng-model="getaccount">
                <span class="unit">元</span>
              </div>
            </div>
          </li> --%>
        </ul>
      </div>
      <div class="form-group">
        <a w5c-form-submit="vm.rechargeSubmit()" class="btn btn-block btn-lg btn-blue">充值</a>
      </div>
      <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
    </div>
    </form>
  </div>
  <!-- 侧栏版面-银行类型 -->
  <div class="panel-overlay"></div>
  <div class="panel panel-default panel-bank">
    <div class="panel-heading" ng-click="bankListHide()">选择银行类型</div>
    <ul class="list-group">
      <li class="list-group-item" ng-click="selectBank(list.name,list.nid)" ng-repeat="list in payment_list">
        <span class="item-media">
          <img src=""  ng-src="{{list.thumbs}}">
        </span>
        <span class="item-title" data-key="list.nid" ng-bind="list.name"></span>
      </li>
    </ul>
  </div>
</div>
</div>
