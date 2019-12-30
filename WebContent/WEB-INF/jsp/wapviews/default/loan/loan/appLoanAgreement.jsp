<%@ page language="java" pageEncoding="UTF-8"%>
<section class="page page-transfer" ng-controller="myloanAgreement">
 <div class="page-content">
   <div class="transfer-buy">
     <div class="loan transfer-one">
      <ul class="bd">
        <li><span class="name">贷款号</span><span ng_bind="LoanProtocol.loan_info.serialno"></span></li>
        <li><span class="name">借款人</span><span ng_bind="LoanProtocol.loan_info.member_name"></span></li> 
        <li><span class="name">出借人</span>详见本协议第一条</li> 
        <li><span class="name">还款方式</span><span ng_bind="LoanProtocol.loan_info.repay_type"></span></li> 
        <li><span class="name">签订日期</span><span ng_bind="LoanProtocol.loan_info.reverify_time|timestamp|date:'yyyy-MM-dd'"></span></li>
      </ul>
    </div>
    <div class="loan transfer-two">
      <div class="hd">
        <div class="tit">第一条：借款详情如下表所示：</div>
      </div>
      <div class="hd">
        <div class="tit">1、出借人名录：</div>
      </div>
       <ul class="bd" ng-repeat="items in LoanProtocol.tender">
        <li><span class="name">出借人：</span><span ng_bind="items.memberName"></span></li>
        <li><span class="name">借款金额：</span><span ng_bind="items.amount|number:2"></span>元</li> 
        <li><span class="name">借款期限：</span><span ng_bind="LoanProtocol.loan_info.borrow_period_name" ></span></li> 
        <li><span class="name">年利率：</span><span ng_bind="LoanProtocol.loan_info.apr|number:2"></span>%</li>
        <li><span class="name">借款开始：</span><span ng_bind="LoanProtocol.loan_info.reverify_time|timestamp|date:'yyyy-MM-dd'"></span></li>
        <li><span class="name">借款到期：</span><span ng_bind="LoanProtocol.loan_info.repay_last_time|timestamp|date:'yyyy-MM-dd'"></span></li>
        <li><span class="name">总还款本息：</span><span ng_bind="items.recoverAmount|number:2"></span>元</li>
      </ul>
    </div>
    <div class="transfer-agree">
     <div class="hd">
      <div class="tit" ng-bind="LoanProtocol.agreement.title"></div>
     </div>
     <div class="bd" ng-bind-html="LoanProtocol.agreement.contents">
     </div>
    </div>
    <div class="loan transfer-two">
      <div class="hd">
        <div class="tit">附：债权转让交易记录：</div>
      </div>
      <ul class="bd" ng-repeat="transfer_items in LoanProtocol.transfre">
        <li><span class="name">债权买入：</span><span ng_bind="transfer_items.buy_member_name"></span></li>
        <li><span class="name">债权卖出：</span><span ng_bind="transfer_items.member_name"></span></li> 
        <li><span class="name">交易金额：</span><span ng_bind="transfer_items.amount|number:2" ></span>元</li> 
        <li><span class="name">交易时间：</span><span ng_bind="transfer_items.success_time|timestamp|date:'yyyy-MM-dd'" ng_if="transfer_items.success_time!=0"></span></li>
      </ul>
    </div>
   </div>
 </div>
</section>
