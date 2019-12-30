<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
 <div class="container">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span>债权转让协议书</span>
 </div>
</nav>
<section class="page page-transfer" ng-controller="mytransferAgreement">
 <div class="page-content">
   <div class="transfer-buy">
     <div class="loan transfer-one">
      <ul class="bd">
        <li><span class="name">债权转让协议号</span><span ng-bind="transferInfo.ind"></span></li>
        <li><span class="name">转让人</span><span ng-bind="transferInfo.memberName"></span></li> 
        <li><span class="name">受让人</span><span ng-bind="transferInfo.buyMemberName"></span></li> 
        <li><span class="name">还款方式</span><span ng-bind="loan.repay_type"></span></li> 
        <li><span class="name">签订日期</span><span ng-bind="transferInfo.successTime|timestamp|date:'yyyy-MM-dd HH:mm:ss'"></span></li>           
      </ul>
    </div> 
    <div class="loan transfer-two">
      <div class="hd">
        <div class="tit" ng-bind="loan.name"></div>
      </div>
      <ul class="bd">
        <li><span class="name">利率：</span><span ng-bind="transferInfo.apr"></span>%</li>
        <li><span class="name">转让本息：</span><span ng-bind="transferInfo.amountMoney"></span>元</li> 
        <li><span class="name">转让期数/总期数：</span><span ng-bind="transferInfo.totalPeriod-transferInfo.period+1+'/'+transferInfo.totalPeriod" ></span></li> 
        <li><span class="name">转让价格：</span><span ng-bind="transferInfo.amount"></span>元</li>
        <li><span class="name">转让时间：</span><span ng-bind="transferInfo.successTime|timestamp|date:'yyyy-MM-dd HH:mm:ss'"></span></li>
      </ul>
    </div>
    <div class="transfer-agree">
     <div class="hd">
      <div class="tit"></div>
     </div>
     <div class="bd" ng_bind-html="agreement.contents">
     </div>
    </div>
   </div>
 </div>
</section>
