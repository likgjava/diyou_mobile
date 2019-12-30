<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>购买详情</span>
    </div>
  </nav>
  <section class="page page-transfer" ng-controller="mytransferInfoCtrl">
    <div class="page-content">
      <div class="transfer-buy">
        <div class="loan">
          <div class="hd"><!-- ng-bind="loansimpleinfo.name" -->
            <div class="tit" ng-bind="transferInfo.loan_name"></div>
            <a class="rig" ng-bind="transferInfo.repay_status|repayStatus"></a>
          </div>
          <ul class="bd">
            <li><span class="name">债权价值：</span><span ng-bind="transferInfo.amount_money|number:2"></span>元</li>
            <li><span class="name">到期时间：</span><span ng-bind="transferInfo.expire_time|timestamp|date: 'yyyy-MM-dd'"></span></li>
            <li><span class="name">待还/总期数：</span><span ng-bind="transferInfo.period"></span>期/共<span ng-bind="transferInfo.total_period"></span>期</li>            
          </ul>
        </div> 
        <div class="look">查看<a href="/wap/transfer/transferAgreement#?id={{transferInfo.transfer_id}}">《债权转让协议》</a></div>

        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">转让价格(元)：</div>
              <div class="item-input col-xs-7"><input type="text" class="txt" name="money" readonly="readonly" ng-model="transferInfo.amount"/></div>
            </div>
          </li>
          </ul>
        </div>

        <dl class="item-text">
          <dd><span class="name">预计盈利：</span><span class="val" ng-bind="transferInfo.income|number:2"></span>元</dd>
        </dl>


        <div class="loan-repay">
          <div class="hd">还款信息</div>
          <div class="bd">
            <dl ng-repeat="list in recoverInfo">
              <dt>第<span ng-bind="list.period_no">0</span>/<span ng-bind="list.period">0</span>期<a class="fr fs10" ng-bind="list.status|repayStatus"></a></dt>
              <dd><span class="name">应收总额</span><span ng-bind="list.amount|number:2"></span>元</dd>
              <dd><span class="name">实收本金</span><span ng-bind="list.principal_yes|number:2"></span>元</dd>
              <dd><span class="name">实收利息</span><span ng-bind="list.interest_yes|number:2"></span>元</dd>
              <dd><span class="name">预还款日</span><span ng-bind="list.recover_time"></span></dd>
            </dl> 
          </div>
        </div>
      </div> 

      <!-- 支付密码 -->  
      <div class="overlay"></div>
      <div class="tender-pay" id="tenderPay">
        <div class="hd">
          <i class="iconfont back" ng-click="transferHid()">&#xe604;</i>
          <span class="title">输入支付密码</span>                        
        </div>
        <div class="bd">            
            <div class="form-group">
              <input type="hidden" ng-model="counter.transfer_id" value="{{transfer.loan_id}}">
              <input type="password" class="form-control" value="" placeholder="请输入支付密码" ng-model="counter.paypassword">
            </div>             
            <div class="form-group fn-clear">
              <a class="btn btn-lg btn-cancel" ng-click="transferHid()">取消</a>
              <a class="btn btn-lg" ng-click="tennderSubmit()" ng-disabled="tenderBtn.status" ng-bind="tenderBtn.text"></a>
            </div>
            <div class="form-group" style="height:20px;">
              <span class="err" ng-bind="errorPlace"></span>
            </div>
        </div>
      </div>
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>  
    </div>
  </section>