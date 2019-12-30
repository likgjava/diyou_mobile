<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>提现</span>
      <a class="link" ng-href="/wap/bank/withdrawLog">提现记录</a>
    </div>
</nav>
<section class="page page-cash" ng-controller="cashCtrl">
    <div class="page-content">
        <div class="list-block">
          <ul>
          <li>
            <div class="item-content item-bank">
              <div class="icon"><img ng-src="{{bankinfo.bank_img}}"></div>
              <div class="name" ng-bind="bankinfo.bank_name"></div>
              <div class="number" ng-bind="bankinfo.account"></div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">开户名</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt" readonly="true" ng-value="bankinfo.realname">           
              </div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">支行名称</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt" readonly="true" ng-value="bankinfo.name" >          
              </div>
            </div>
          </li>
          </ul>
        </div>

        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">提现金额</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt" placeholder="请输入提现金额" ng-model="formData.amount" ng-change="cashFee(formData.amount)">  
                <span class="unit">元</span>            
              </div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">提现手续费</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt" readonly="true" ng-model="formData.cost">  
                <span class="unit">元</span>            
              </div>
            </div>
          </li>
          <li>
            <div class="item-content">
              <div class="item-title col-xs-4">实际到账</div>
              <div class="item-input col-xs-8">
                <input type="text" class="txt" readonly="true" ng-model="formData.realAmount" >  
                <span class="unit">元</span>            
              </div>
            </div>
          </li>
          </ul>
        </div>

        <div class="page-view">  
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>   
          <!-- 立即转让/转让详情 -->
          <input type="button" class="btn btn-block btn-lg btn-blue" value="提现" ng-click="cashSubmit()" >
        </div>
        
        <!-- 输入支付 -->
        <div class="overlay"></div>
        <div class="tender-pay">
          <div class="hd">
            <i class="iconfont back" ng-click="playPwdHide()">&#xe604;</i>
            <span class="title">输入支付密码</span>                        
          </div>
          <div class="bd">            
              <div class="form-group">
                <input type="password" class="form-control" value="" placeholder="请输入支付密码" ng-model="formData.paypassword">
              </div>             
              <div class="form-group fn-clear">
                <a class="btn btn-lg" ng-click="playPwdHide()">取消</a>
                <a class="btn btn-lg" ng-click="playSubmit()" ng-disabled="tenderBtn.status" ng-bind="tenderBtn.text"></a>
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

