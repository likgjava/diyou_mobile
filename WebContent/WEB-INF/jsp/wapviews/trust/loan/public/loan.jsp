<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
	<div class="container">
	  <span class="title">投资列表</span>
	</div>
	<a class="iconfont counter">&#xe60d;</a>
</nav>
<div class="page page-scroll" ng-controller="listTenderCtrl">
    <div class="page-content">
      <div class="loan-nav">
        <a href="/wap/loan/loantender" class="col-xs-6 link active">投资列表</a>
        <a href="/wap/transfer/transfer" class="col-xs-6 link">债权转让</a>
      </div>

      <!-- 投资列表 -->
      <div class="loans">
        <div class="loan-list">
          <a class="loan-item" href="/wap/loan/loaninfoview#?id={{borrow.id}}" ng-repeat="borrow in borrowList.items">
            <div class="loan-content">
              <div class="loan-tit" ng-if="borrow.additional_status!=-1"></div>
              <div class="load-title">
              	<img ng-if="borrow.pic!=null" ng-src="{{borrow.pic}}" width="24" height="24"/>
              	<img ng-if="borrow.pic==null" ng-src="/wapassets/default/images/loantype/style3/{{borrow.category_id}}.png" width="24" height="24"/>
              	<span ng-bind="borrow.name|truncate:10"></span>
              </div>
              <div class="col-xs-4">
                <div class="val"><b class="red" ng-bind="borrow.apr"></b><span>%</span><div class="ln-pos" ng-if="borrow.additional_apr>0" ng-bind="'+'+(borrow.additional_apr|number:2)+'%'"></div></div>
                <div class="name">年化收益</div>
              </div>
              <div class="col-xs-4">
                <div class="val" ng-bind-html="borrow.amount|formatMoney"></div>
                <div class="name">投资金额</div>
              </div>
              <div class="col-xs-4">
                <div class="val"><b ng-bind="borrow.period"></b><span ng-if="borrow.repay_type=='5'">天</span><span ng-if="borrow.repay_type!='5'">个月</span></div>
                <div class="name">投资期限</div>
              </div>

              <div class="progbar" >
                <div class="prog prog-bg"></div>
                <div class="prog prog-bg2"></div>
                <div class="prog prog-rount" style="-webkit-transform:rotate({{borrow.progress*360/100}}deg);" ng-if="borrow.progress<=50"></div>
                <div class="prog prog-rount" style="-webkit-transform:rotate(180deg);" ng-if="borrow.progress>50"></div>
                <div class="prog prog-rount2" style="-webkit-transform:rotate({{(borrow.progress-50)*360/100}}deg);" ng-if="borrow.progress>50"></div>
                <div class="prog-text t{{borrow.status}}" ng-if="borrow.progress<100 && borrow.status<4">{{borrow.progress|number:0}}%</div>
                <div class="prog-text t{{borrow.status}}" ng-if="borrow.progress==100 && borrow.category_type == 3 && borrow.status==3">{{borrow.status_name}}</div>
                <div class="prog-text t{{borrow.status}}" ng-if="borrow.status>=4">{{borrow.status_name}}</div>
              </div>

              <span class="icon-award" ng-if="borrow.award_status!=-1"></span>
            </div>

            <div class="loan-foot">
              <span class="com col-xs-8" ng-if="borrow.vouch_company_id == 0"><i class="iconfont">&#xe60f;</i>担保方：南昌金融投资有限公司</span>
              <span class="com col-xs-8" ng-if="borrow.vouch_company_id > 0"><i class="iconfont">&#xe60f;</i>担保方：<span ng-bind="borrow.vouch_company_name"></span></span>
              <span class="num col-xs-4"><i class="iconfont">&#xe611;</i>投资人数：<span ng-bind="borrow.tender_count"></span>
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
       <jsp:include   page="../../common/counter.jsp" />
    </div>
</div>

<!-- 网页底部 -->
<jsp:include page="../../common/bottomPage.jsp">
  <jsp:param name="bright_loan" value="active" />
  <jsp:param name="light" value="listimg2" />
</jsp:include>
