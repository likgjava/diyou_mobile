<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
	<div class="container">
	  <span class="title">债权转让</span>
	</div>
	<a class="iconfont counter">&#xe60d;</a>
</nav>
<div class="page page-scroll" ng-controller="listTransferCtrl">
    <div class="page-content">
      <div class="loan-nav">
        <a href="/wap/loan/loantender" class="col-xs-6 link">投资列表</a>
        <a href="/wap/transfer/transferList" class="col-xs-6 link active">债权转让</a>
      </div>

      <!-- 债权列表 -->
      <div class="loans">

        <div class="loan-list transfer-list">
          <a class="loan-item" href="/wap/transfer/transferInfo#?id={{borrow.id}}" ng-repeat="borrow in borrowList.items" >
            <div class="loan-content">
              
              <div class="col c1">
                <div class="val" ng-bind-html="borrow.amount_moeny|formatMoney"></div>
                <div class="name">债权价值</div>
              </div>              
              <div class="col c2"><!-- ng-bind="" -->
                <div class="val"><b ng-bind="borrow.period">3</b>/<span  ng-bind="borrow.total_period">12</span></div>
                <div class="name">转让期数</div>
              </div>
              <div class="col c3">
                <div class="val"><b class="red" ng-bind="borrow.apr | number:0"></b>.<span ng-bind="borrow.apr|numdec"></span>%</div>
                <div class="name">原标年化收率</div>
              </div>
              <div class="col c4">
                <div class="val c-blue" ng-bind-html="borrow.amount|formatMoney"></div>
                <div class="name">转让价格</div>
              </div>
              <!-- <span class="icon-style"><span> 推 荐 </span><em></em></span> -->
            </div>
            <div class="loan-foot">
              <span class="com"><i class="iconfont">&#xe60f;</i>担保方：厦门帝网信息科技有限公司</span>
            </div>
            </a>
        </div>
        <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus && !loader">点击加载更多</a>
        <div class="preloader" ng-class="{loading:loader}"></div>
        <div class="txt-cent" ng-if="!getMoreStatus">没有更多数据</div>
      </div>

      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
      <!--理财计算器-->
      <?php $this->partial("common/counter")?>

    </div>
</div>


<nav class="nav-bottom navbar-fixed-bottom">
  <div class="nav-item col-xs-4">
    <a href="/wap">
      <span class="iconfont">&#xe600;</span>
      <span>首页</span>
    </a>
  </div>
  <div class="nav-item col-xs-4 active">
    <a href="/wap/loan/loantender">
      <span class="iconfont">&#xe601;</span>
      <span>我要投资</span>
    </a>
  </div>
  <div class="nav-item col-xs-4">
    <a href="/wap/member/index">
      <span class="iconfont">&#xe603;</span>
      <span>个人中心</span>
    </a>
  </div>
</nav>