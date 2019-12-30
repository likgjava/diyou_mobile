<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>充值</span>
  </div>
</nav>
<div class="page" ng-controller="rechargeCtrl">
  <div class="page-content">
    <div class="page-view">
      
      <div class="list-block">
        <ul>
          <li>
            <div class="item-content" ng-click="rechargeType()">
              <div class="item-title col-xs-4">充值类型</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt open-panel" name="pay-type" placeholder="请选择充值类型" readonly="true" ng-model="bankText">
                <input type="hidden" ng-model="rechargeTp.payment_nid">
              </div>
              <div class="item-after iconfont">&#xe604;</div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">充值金额</div>
              <div class="item-input col-xs-8">
                <input type="number" class="txt" name="pay-type" placeholder="请输入充值金额"  ng-model="rechargeTp.amount">
                <span class="unit">元</span>
              </div>
            </div>
          </li>

          <li class="mt10">
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
          </li>
        </ul>          
      </div>
      <div class="form-group">
        <a ng-click="rechargeSubmit()" class="btn btn-block btn-lg btn-blue">充值</a>
      </div>

    </div>
  </div>
  <!-- 侧栏版面-银行类型 -->
  <div class="panel-overlay"></div>
  <div class="panel panel-default panel-bank">
    <div class="panel-heading" ng-click="bankListHide()">选择银行类型</div>
    <ul class="list-group">
      <li class="list-group-item" ng-click="selectBank(list.name,list.nid)" ng-repeat="list in payment_list">
        <span class="item-media">
          <img ng-src="/wapassets/images/payment/pay/{{list.nid}}.jpg" src="/wapassets/images/payment/pay/yeepay.gif">
        </span>
        <span class="item-title" data-key="list.nid" ng-bind="list.name"></span>
      </li>
    </ul>
  </div>
</div>

