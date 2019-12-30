<%@ page language="java" pageEncoding="UTF-8"%>
<div ng-controller="myspreadCtrl">
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>我的推广</span>
      <a class="link" ng-click="onSpread()">推广赚钱</a>
    </div>
</nav>
<section class="page page-spread">
    <div class="page-content">
      <div class="ui-form">
        <div class="info">
          <ul>
            <li>
              <span class="name">邀请人数：</span>
              <span ng-bind="countInfo.person_count|number"></span>人</li>
            <li>
              <span class="name">提成总额：</span>
              <span ng-bind="countInfo.income|currency:'￥'"></span>
            </li>
            <li>
              <span class="name">投资提成：</span>
              <span ng-bind="countInfo.tender_income|currency:'￥'"></span>
            </li>
            <li>
              <span class="name">借款还款提成：</span>
              <span ng-bind="countInfo.repay_income|currency:'￥'"></span>
            </li>
          </ul>
        </div>

        <div class="form-alert">
          查看<a href="/wap/spread/spreadLog">推广记录</a>
        </div>

        <ul class="list-block">
          <li class="item-content">
            <div class="item-title">当前剩余结算金额：</div>
            <div class="item-input col-xs-5">
              <input type="text" class="txt" ng-value="total.unAaccount|currency:'￥'" readonly="true" >                        
            </div>
          </li>
        </ul>
        <div class="form-alert">满<span class="c-orange" ng-bind="limit+'元'"></span>结算一次</div>    

        <div class="page-view">  
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>   
          <!-- 立即转让/转让详情 -->
          <input type="button" class="btn btn-block btn-lg btn-blue" value="立即结算" ng-disabled="total.unAaccount<limit" ng-click="doAccount()">
        </div>

        <ul class="list-block">
          <li class="item-content">
              <div class="item-title">结算中：</div>
              <div class="item-input col-xs-5">
                <input type="text" class="txt" ng-value="total.accounting|currency:'￥'" readonly="true" >                        
              </div>
          </li>
        </ul>

        <div class="form-alert">
          查看<a href="/wap/spread/settleLog">结算记录</a>
        </div>

        <div class="table">
          <div class="row row-head">
            <div class="col c1">用户名</div>
            <div class="col c2">投资总额（元）</div>
            <div class="col c3">还款总额（元）</div>
          </div>
          <div ng-if="mySpread.total_items>0">
          <div class="row" ng-repeat="list in mySpread.items">
          <div class="col c1"><a ng-href="/wap/spread/spreadLog?name={{list.spreaded_member_name}}" ><span ng-bind="list.spreaded_member_name"></span></a></div>
            <div class="col c2"><span ng-bind="list.tender_success_amount|number:2"></span></div>
            <div class="col c3"><span ng-bind="list.repay_amount_yes|number:2"></span></div>
          </div>
          </div>
          <div class="row noData" ng-if="mySpread.total_items==0">
            暂无记录
          </div>
        </div>
      </div>

      <!-- 输入支付 -->
      <div class="overlay"></div>
      <div class="ui-spread">
        <div class="bd">
           <input type="text" class="form-control" ng-value="share_url" >
        </div>
        <div class="fd">
        <a class="btn btn-block btn-lg btn-blue" ng-click="onCopy()">复制</a>
          <a class="btn btn-block btn-lg btn-blue" ng-click="onSpreadHide()">取消</a>
        </div>
      </div>

      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
  </div>
</section>
</div>