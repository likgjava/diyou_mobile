<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>提现记录</span>
    </div>
</nav>
<section class="page page-user page-cash" ng-controller="cashRecordCtrl">
    <div class="page-content">
      <div class="loans" ng-if="cashRecord.total_items>0">
        <div class="loan-list loan-log">
          <div class="loan-item" ng-repeat="list in cashRecord.items">
            <div class="loan-content">
              <div class="col-xs-12">
                <span class="time" ng-bind="list.add_time |timestamp|date: 'yyyy-MM-dd HH:mm'"></span>
                <span class="val" ng-bind="list.amount | number:2"></span>元
              </div>
              <div class="col-xs-12">
                <span class=" fr">
                  <span class="val v{{list.status}}" ng-bind="list.status_name"></span>
                </span>
                <span class="name">提现金额</span>
              </div>            
            </div>
          </div>        
        </div>
        <div class="loan-foot">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus">点击加载更多</a>
        <p class="txt-cent" ng-if="!getMoreStatus">没有更多数据</p>
        </div>
      </div>
      <div class="noData" ng-if="cashRecord.total_items==0">
        暂无交易记录
      </div>


      <!-- 加载状态  -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
	</div>
</section>

