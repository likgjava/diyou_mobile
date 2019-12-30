<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>交易记录</span>
    </div>
  </nav>
  <section class="page page-user" ng-controller="accountLogCtrl">
    <div class="page-content">
      <div class="loans" ng-if="account.total_items>0">
        <div class="loan-list loan-log">
          <div class="loan-item" ng-repeat="list in account.items">
            <div class="loan-content">
              <div class="col-xs-12">
                <span class="time" ng-bind="list.add_time |timestamp|date: 'yyyy-MM-dd HH:mm:ss'"></span>
                <span class="title" ng-bind="list.fee_name"></span>
              </div>
              <div class="col-xs-12 col-center">
                <span class="c-blue fr" ng-bind="list.loan_name"></span>
                <span class="name">关联项目：</span> 
              </div>
              <div class="col-xs-12">
                <span class="{{list.money_type}} fr">
                  ￥<span ng-if="list.money_type=='expend'">-</span><span ng-if="list.money_type=='income'">+</span><span ng-bind="list.money| number:2"></span>
                </span>
                <span class="name">余　　额：</span>
                <span class="val" ng-bind="list.balance|number:2"></span>元
              </div>            
            </div>
          </div>        
        </div>
        <div class="loan-foot">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus">点击加载更多</a>
        <p class="txt-cent" ng-if="!getMoreStatus">没有更多数据</p>
        </div>
      </div>
      <div class="noData" ng-if="account.total_items==0">
        暂无交易记录
      </div>
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
    </div>
  </section>