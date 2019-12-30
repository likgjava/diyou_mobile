<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>购买债权</span>
    </div>
  </nav>
  <section class="page" ng-controller="transferinvestCtrl">
    <div class="page-content">
      <div class="tender-bid">
        <div class="loan">
          <div class="hd"><!-- ng-bind="loansimpleinfo.name" -->
            <div class="tit" ng-bind="transfer.loan_name"></div>
          </div>
          <ul class="bd">
            <!-- <li><span class="name">起投金额：</span><span ng-bind="10"></span>元</li> -->
            <li><span class="name">还款方式：</span><span ng-bind="transfer.repay_type"></span></li>
            <li><span class="name">转让期数：</span><span ng-bind="transfer.period"></span>期/共<span ng-bind="transfer.total_period"></span>期</li>
            <li><span class="name">借款期限：</span><span ng-bind="transfer.loan_period"></span></li>
            <li><span class="name">还款期限：</span><span ng-bind="transfer.next_repay_time"></span></li>
          </ul>
        </div> 


        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">转让价格(元)：</div>
              <div class="item-input col-xs-7"><input type="text" class="txt" name="money" ng-model="transfer.amount" readonly="readonly"/></div>
            </div>
          </li>
          </ul>
        </div>

        <dl class="item-text">
          <!-- <dd><span class="name">　手续费：</span><span class="val c-blue"><span ng-bind="transfer.transfer_fee|number:2"></span>元</span></dd> -->
          <dd><span class="name">账户余额：</span><span class="val c-red"><span ng-bind="transfer.balance_amount|number:2"></span>元</dd>
          <a class="link" href="/wap/member/recharge">充值</a>
        </dl>

        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">预期收益(元)：</div>
              <div class="item-input col-xs-7"><input type="text" class="txt" name="money" value="{{transfer.income|number:2||0}}" readonly="true" /></div>
            </div>
          </li>
          </ul>
        </div>

        <div class="err"></div>
        <div class="item-foot">
          <a class="btn btn-lg btn-block btn-blue" id="btn-loan" ng-click="transferNow()">立即购买</a>
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