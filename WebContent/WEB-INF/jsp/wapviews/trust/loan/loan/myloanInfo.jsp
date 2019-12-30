<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>我的投资详情</span>
    </div>
  </nav>
  <section class="page page-transfer" ng-controller="myloanInfoCtrl">
    <div class="page-content">
      <div class="transfer-buy">
        <div class="loan">
          <div class="hd"><!-- ng-bind="loansimpleinfo.name" -->
            <div class="tit" ng-bind="loan_info.name|truncate:15"></div>
            <a class="rig" ng-bind="tender_info.status_name"></a>
          </div>
          <ul class="bd">
            <li><span class="name">借款编号：</span><span  ng-bind="loan_info.serialno"></span></li>
            <li><span class="name">年利率：</span><span ng-bind="loan_info.apr+'%'"></span></li>
            <li><span class="name">还款方式：</span><span ng-bind="loan_info.repay_type_name"></span></li>
            <li><span class="name">借款期限 {{loan_info.category_type}}：</span><font ng-bind="loan_info.period_name" ></font></li>
          </ul>
        </div>
        <div class="look" ng-if="loan_info.status!='-2'&&loan_info.status!='-1'&&loan_info.status!='-6'&&loan_info.status!='-7'&&loan_info.status!='-3'&&loan_info.status!='-4'&&loan_info.status!='-5'&&loan_info.status!='-6'&&loan_info.status!='3'&&loan_info.status!='4'">
        	查看<a ng-click="look('loan',tender_info.loan_id)">《借款协议》</a>
        </div>
         <div class="look" ng-if="loan_info.status=='3'&&loan_info.category_type=='3'">
        	查看<a ng-click="look('loan',tender_info.loan_id)">《借款协议》</a>
        </div>

        <div class="loan-repay loan-tabs" id="loanTabs">
          <div class="hd">
           <ul>
            <li class="on">借款详情</li>
            <li >还款详情</li>
           </ul>
          </div>
          <div class="bd">
           <div class="bd-item on">
            <dl>
              <dd><span class="name">投资金额</span><span ng-bind="tender_info.amount|number:2"></span>元</dd>
              <dd><span class="name">总收益</span><span ng-bind="tender_info.recover_income_all|number:2"></span>元</dd>
              <dd><span class="name">已收收益</span><span ng-bind="tender_info.recover_income|number:2"></span>元</dd>
              <dd><span class="name">待收本金</span><span ng-bind="tender_info.wait_principal|number:2"></span>元</dd>
              <%-- <dd><span class="name">奖励金额</span><span ng-bind="tender_info.award_amount|number:2"></span>元</dd> --%>
            </dl>
          </div>

          <div class="bd-item">
            <dl ng-repeat="list in recover_info">
              <dt>第<font ng-bind="list.period_no"></font>/<font ng-bind="list.period"></font>期<a class="staues" ng-bind="list.status_name"></a></dt>
              <dd><span class="name">应收总额</span><font ng-bind="list.amount|number:2" ></font>元</dd>
              <dd><span class="name">实收本金</span><font ng-bind="list.principal_yes|number:2"></font>元</dd>
              <dd><span class="name">实收利息</span><font ng-bind="list.interest_yes|number:2"></font>元</dd>
              <dd><span class="name">预还款日</span><font ng-bind="list.recover_time"></font></dd>
            </dl>
          </div>
         </div>
        </div>
      </div>
  </section>
