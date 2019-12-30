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
        <a href="/wap/transfer/transfer" class="col-xs-6 link active">债权转让</a>
      </div>

      <!-- 债权列表 -->
      <div class="loans">

        <div class="loan-list transfer-list">
          <a class="loan-item" ng-href="/wap/transfer/transferView#?id={{borrow.id}}" ng-repeat="borrow in borrowList.items" >
            <div class="loan-content">
			  <div ng-if="borrow.status==2" class="bg"></div>
              <div class="load-title">
             	 <img ng-if="borrow.pic!=null" ng-src="{{borrow.pic}}" width="24" height="24"/>
              	 <img ng-if="borrow.pic==null" ng-src="/wapassets/default/images/loantype/style3/{{borrow.category_id}}.png" width="24" height="24"/>
              	 <span ng-bind="borrow.loan_name|truncate:10"></span>
              </div>
              <div class="col c1">
                <div class="val">
                	<b ng-bind-html="borrow.amount_money|units"></b>
                </div>
                <div class="name">债权价值</div>
              </div>
              <div class="col c2">
                <div class="val"><b ng-bind="borrow.period"></b>/<span  ng-bind="borrow.total_period"></span></div>
                <div class="name">转让期数</div>
              </div>
              <div class="col c3">
                <div class="val"><b ng-bind="borrow.apr"></b>%</div>
                <div class="name">原标年化收益</div>
              </div>
              <div class="col c4">
                <div class="val c-blue"><b ng-bind-html="borrow.amount|units"></b></div>
                <div class="name">转让价格</div>
              </div>
            </div>
            <div class="loan-foot">
              <span class="com"><i class="iconfont">&#xe60f;</i>担保方：<span ng-bind="borrow.vouch_company_name || '无'"></span></span>
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
      <!-- 理财计算器 -->
       <jsp:include   page="../../common/counter.jsp" />
    </div>
</div>

<!-- 网页底部 -->
<jsp:include page="../../common/bottomPage.jsp">
  <jsp:param name="light" value="listimg2" />
</jsp:include>
