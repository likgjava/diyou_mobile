<%@ page language="java" pageEncoding="UTF-8"%>
<script type="text/javascript">
	var name='${name}';
</script>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>推广记录</span>
    </div>
  </nav>
  <section class="page page-user" ng-controller="spreadLogCtrl">
    <div class="page-content">
      <div class="loans" ng-if="spreadLog.total_items>0">
        <div class="loan-list loan-log">
          <div class="loan-item" ng-repeat="list in spreadLog.items">
            <div class="loan-content">
              <div class="col-xs-12">                
                <span class="col-xs-6">
                  <span class="name">用 户 名：</span>
                  <span class="val" ng-bind="list.spreaded_member_name"></span>
                </span> 
                <span class="time col-xs-6" ng-bind="list.add_time"></span>
              </div>
              <div class="col-xs-12 col-center">
                <span class="col-xs-6">
                  <span class="name">提成类型：</span>
                  <span class="val" ng-bind="list.spread_type"></span>
                 <!--  <span class="val" ng-if="list.spread_type=='tender'">投资提成</span>
                  <span class="val" ng-if="list.spread_type=='loan'">借款提成</span> -->
                </span> 
                <span class="col-xs-6">
                  <span class="name">资金类型：</span>
                  <span class="val" ng-bind="list.amount_type"></span>
                  <!-- <span class="val" ng-if="list.amount_type=='amount'">本息</span>
                  <span class="val" ng-if="list.amount_type=='interest'">利息</span>
                  <span class="val" ng-if="list.amount_type=='principal'">本金</span> -->
                </span> 
              </div>
              <div class="col-xs-12">
                <span class="col-xs-6">
                  <span class="name">提成金额：</span>
                  <span class="val c-orange" ng-bind="list.award|currency:'￥'"></span>
                </span> 
                <span class="col-xs-6">
                  <span class="name">提成比例：</span>
                  <span class="val c-orange" ng-bind="list.proportion"></span>
                </span> 
              </div>      
            </div>
          </div>        
        </div>
        <div class="loan-foot">
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus">点击加载更多</a>
        <p class="txt-cent" ng-if="!getMoreStatus">没有更多数据</p>
        </div>
      </div>
      <div class="noData" ng-if="spreadLog.total_items==0">
        暂无推广记录
      </div>
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
    </div>
  </section>