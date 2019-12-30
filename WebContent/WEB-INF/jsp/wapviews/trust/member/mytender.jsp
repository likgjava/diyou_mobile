<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="window.location.href='/wap/member/index'" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>我的项目</span>
    </div>
  </nav>
  <section class="page" ng-controller="mytenderCtrl">
    <div class="page-content page-user">
      <div class="loan-nav">
        <a href="/wap/member/mytender" class="col-xs-6 link active">我的投资</a>
        <a href="/wap/transfer/mytransfer" class="col-xs-6 link">我的债权</a>
      </div>

      <div class="loans" ng-if="mytenderList.total_items>0">
        <div class="loan-list">
          <a class="loan-item" href="/wap/loan/myloaninfo#?id={{list.id}}" ng-repeat="list in mytenderList.items">
            <div class="loan-head">
              <div class="title" ng-bind="list.loan_name"></div>
            </div>
            <div class="loan-content">
              <div class="col-xs-5">
                <div class="val" ng-bind="list.amount | number:2"></div>
                <div class="name">投资金额</div>
              </div>
              <div class="col-xs-4">
                <div class="val val-price" ng-bind="list.award_interest | number:2"></div>
                <div class="name">总收益</div>
              </div>
              <div class="col-xs-3">
                <div class="val val-state" ng-bind="list.status_name"></div>
                <div class="name" ng-bind="list.add_time |timestamp|date: 'yyyy-MM-dd'"></div>
              </div>
            </div>
          </a>
        </div>
        <div class="loan-foot">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus">点击加载更多</a>
        <p class="txt-cent" ng-if="!getMoreStatus">没有更多数据</p>
        </div>
      </div>
      <div class="noData" ng-if="mytenderList.total_items==0">
        暂无投资记录
      </div>
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
    </div>
  </section>
