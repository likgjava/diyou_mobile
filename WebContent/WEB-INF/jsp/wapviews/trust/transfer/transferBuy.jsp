<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>购买债权</span>
    </div>
  </nav>
  <section class="page" ng-controller="transferbuyCtrl">
    <div class="page-content">
      <div class="tender-bid">
        <div class="loan">
          <div class="hd"><!-- ng-bind="loansimpleinfo.name" -->
            <div class="tit" >老家买房借款001</div>
          </div>
          <ul class="bd">
            <li><span class="name">起投金额：</span><span ng-bind="loansimpleinfo.amount_low"></span>元</li>
            <li><span class="name">还款方式：</span><span ng-bind="loansimpleinfo.repay_type"></span></li>
            <li><span class="name">转让期数：</span><span>10期/共12期</span></li>
            <li><span class="name">借款期限：</span><span ng-bind="loansimpleinfo.period"></span>个月</li>
            <li><span class="name">还款期限：</span><span>2015年07月22日</span></li>
          </ul>
        </div> 


        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">转让价格(元)：</div>
              <div class="item-input col-xs-7"><input type="text" class="txt" name="money" value="70,000.00" ng-model="counter.amount" readonly="readonly"/></div>
            </div>
          </li>
          </ul>
        </div>

        <dl class="item-text">
          <dd><span class="name">手续费：</span><span class="val c-blue"><span ng-bind="loansimpleinfo.can_amount|number:2"></span>元</span></dd>
          <dd><span class="name">账户余额：</span><span class="val c-red"><span ng-bind="loansimpleinfo.balance_amount|number:2"></span>元</dd>
          <a class="link" ng-click="pcNotice()">充值</a>
        </dl>

        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">预期收益(元)：</div>
              <div class="item-input col-xs-7"><input type="text" class="txt" name="money" value="{{getaccount.interest_total||0}}" readonly="true" /></div>
            </div>
          </li>
          </ul>
        </div>

        <div class="err"></div>
        <div class="item-foot">
          <a class="btn btn-lg btn-block btn-blue" id="btn-loan" ng-click="tenderNow()">立即购买</a>
        </div>
      </div> 

      <!-- 支付密码 -->  
      <div class="overlay"></div>
      <div class="tender-pay" id="tenderPay">
        <div class="hd">
          <i class="iconfont back">&#xe604;</i>
          <span class="title">输入支付密码</span>                        
        </div>
        <div class="bd">            
            <div class="form-group">
              <input type="password" class="form-control" value="" placeholder="请输入支付密码" ng-model="counter.paypassword">
            </div>             
            <div class="form-group fn-clear">
              <a class="btn btn-lg btn-cancel">取消</a>
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