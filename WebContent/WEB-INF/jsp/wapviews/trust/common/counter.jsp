     <%@ page language="java" pageEncoding="UTF-8"%>  
    
      <!--理财计算器-->
      <div class="counter-overlay"></div>
      <div class="ui-counter" ng-controller="counterCtrl">
      <form role="form" w5c-form-validate="vm.validateOptions" novalidate name="validateForm">
        <div class="hd">
          <span class="title">收益预估</span>
          <i class="iconfont exit"></i>          
        </div>
        <ul class="bd fn-clear">
          <li>
            <div class="item-input">
              <input type="number" name="accountTwoDecimal" class="txt" id="amount" placeholder="投资金额" ng-model="counter.account" required ng-pattern="/^\d+(\.\d{1,2})?$/" />
              <span class="unit">元</span>
            </div>
          </li>
          <li>
            <div class="item-input">
              <input type="number" name="aprTwoDecimal" class="txt" id="rate" placeholder="借款利率" ng-model="counter.lilv" required ng-pattern="/^\d+(\.\d{1,2})?$/" />
              <span class="unit">%</span>
            </div>
          </li>
          <li>
            <div class="item-input">
              <input type="number" name="periodTwoDecimal" class="txt" id="time" placeholder="投资期限" ng-model="counter.period" required ng-pattern="/^\d+$/"/>
              <span class="unit">月</span>
            </div>
          </li>
          <li>
            <div class="item-input item-select">
              <select id="mode" class="select" ng-model="counter.repay_type" name="mode" ng-options="list.id as list.name for list in loanSel">
              </select>

              <span class="arrow"></span>
            </div>
          </li>
          <li class="w100">
            <span class="name">产品预估收益：</span>
            <span class="val" ng-bind="accountInterest"></span>元
          </li>
         <!--  <li>
            <span class="name">银行预估收益：</span>
            <span class="val" ng-bind="bankAccountInterest"></span>
          </li> -->
        </ul>
        <div>
          <a class="btn btn-blue btn-block ng-scope" w5c-form-submit="vm.countBtn()">开始计算</a>
        </div>
        <div class="fd">预估收益计算仅供参考，以实际收益为准</div>
        <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
        </form>
      </div>