<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>结算记录</span>
    </div>
  </nav>
  <section class="page page-spread" ng-controller="settleLogCtrl">
    <div class="page-content">
      <div  ng-if="settleLog.total_items>0">
        <div class="table table-settle">
          <div class="row row-head">
            <div class="col c1">结算金额（元）</div>
            <div class="col c2">时间</div>
            <div class="col c3">状态</div>
          </div>
          <div class="row" ng-repeat="list in settleLog.items">
            <div class="col c1"><span class="g3" ng-bind="list.money|number:2">10.00</span></div>
            <div class="col c2"><span class="g9" ng-bind="list.add_time"></span></div>
            <div class="col c3"><span class="c-orange" ng-bind="list.status"></span></div>
          </div>
        </div>
        <div class="loan-foot mt10 border0">
            <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus">点击加载更多</a>
            <p class="txt-cent" ng-if="!getMoreStatus">没有更多数据</p>
        </div>
      </div>
      <div class="noData" ng-if="settleLog.total_items==0">
        暂无结算记录
      </div>
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
    </div>
  </section>