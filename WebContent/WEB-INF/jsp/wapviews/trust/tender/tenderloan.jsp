<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>投标</span>
    </div>
  </nav>
  <section class="page" ng-controller="tenderloanCtrl">
    <div class="page-content">
      <div class="tender-bid">
        <div class="loan">
          <div class="hd">
            <div class="name" ng-bind="loansimpleinfo.name"></div>
            <div class="rate" ng-class="{'w100-apr':loansimpleinfo.award_proportion>0&&loansimpleinfo.additional_apr>0}"><i class="iconfont">&#xe60e;</i><span ng-bind="loansimpleinfo.apr"></span>% <span class="r" ng-if="loansimpleinfo.award_proportion>0">+<span ng-bind="loansimpleinfo.award_proportion|number:2"></span>%<i class="ico">奖</i></span>
            <span ng-if="loansimpleinfo.additional_apr>0" class="pos">+<span ng-bind="loansimpleinfo.additional_apr|number:2"></span>%<i class="ico">新</i></span></div>
          </div>
           <ul class="bd">
            <li ng-if="loansimpleinfo.category_type!=3"><span class="name">起投金额：</span><span ng-bind="loansimpleinfo.tender_amount_min"></span>元</li>
            <li ng-if="loansimpleinfo.category_type==3"><span class="name">最小流转：</span><span ng-bind="loansimpleinfo.roam_info.amount"></span>元</li>
            <li ng-if="loansimpleinfo.category_type==3"><span class="name">限投份数：</span><span ng-bind="loansimpleinfo.roam_info.portion_total-loansimpleinfo.roam_info.portion_yes"></span>份</li>
            <li><span class="name">还款方式：</span><span ng-bind="loansimpleinfo.repay_type_name"></span></li>
            <li ng-if="loansimpleinfo.category_type!=3"><span class="name">投资上限：</span>
            <span ng-if="loansimpleinfo.tender_amount_max!=0"><span ng-bind="loansimpleinfo.tender_amount_max"></span>元</span>
            <span ng-if="loansimpleinfo.tender_amount_max==0">不限</span>
            </li>
            <li><span class="name" ng-if="loansimpleinfo.category_type!=3">借款</span><span class="name" ng-if="loansimpleinfo.category_type==3">回购</span><span class="name">期限：</span><span ng-bind="loansimpleinfo.period_name"></span></li>
          </ul>
        </div>



       <div class="list-block" ng-if="loansimpleinfo.category_type!=3">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">投资金额(元)：</div>
              <div class="item-input col-xs-7">
              	<input type="text" class="txt" name="money" placeholder="需10的倍数" ng-model="counter.amount" ng-if='loansimpleinfo.tender_amount_min <= loansimpleinfo.wait_amount' />
              	<input type="text" class="txt" name="money" disabled="disabled" ng-model="loansimpleinfo.wait_amount" ng-if='loansimpleinfo.tender_amount_min > loansimpleinfo.wait_amount' />
              </div>
            </div>
          </li>
          </ul>
        </div>

        <div class="lz-loan" ng-if="loansimpleinfo.category_type==3">
        <div class="loan-cal">
          投资份数：<label class="item-opera" ng-click="subtract(countval)">-</label><input type="num" class="txt-val" readonly="true" ng-model="countval"/><label class="item-opera" ng-click="add(countval)">+</label>
        </div>
       </div>

         <div class="list-block" ng-if="loansimpleinfo.is_password=='yes'">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">投资密码：</div>
              <div class="item-input col-xs-7"><input type="password" class="txt" placeholder="请输入投资密码" ng-model="counter.password" /></div>
            </div>
          </li>
          </ul>
        </div>

        <dl class="item-text">
          <dd><span class="name">最大可投：</span><span class="val c-blue"><span ng-bind="loansimpleinfo.wait_amount|number:2"></span>元</span></dd>
          <dd><span class="name">账户余额：</span><span class="val c-red"><span ng-bind="loansimpleinfo.member.balance_amount|number:2"></span>元</dd>
          <!-- <a class="link" ng-click="pcNotice()">充值</a> -->
          <a class="link" href="/wap/member/recharge">充值</a>
        </dl>
        <div class="list-block">
           <ul>
          <li>
            <div class="item-content">
              <div class="item-title">红包</div>
              <div class="item-input"  style="float: right;margin-right:35px;">
                  <span ng-if="bountyData.Amount"><span ng-bind="bountyData.Amount"></span>元红包</span>
                  <a href="javascript:;" ng-click="contactsShow()" ng-if="!bountyData.Amount">请选择红包</a>
              </div>
                <a href="javascript:;" ng-click="contactsShow()"><i style="float: right;" class="item-arrow iconfont">&#xe614;</i></a>
              </div>
            </li>
          </ul>

        </div>
        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">预期收益(元)：</div>
              <div class="item-input col-xs-7">
              	<span ng-bind="getaccount.interest_total|number:2||0"></span>
              	<span ng-if="getaccount.additional_amount>0">+<span ng-bind="getaccount.additional_amount|number:2||0"></span></span>
              </div>
            </div>
          </li>
          </ul>
        </div>

        <dl class="item-text">
          <dd><span class="name">收益包含利息：</span><span class="val" ng-bind="getaccount.interest_award|number:2||0"></span>元<span ng-if="getaccount.additional_amount>0">+<span class="val" ng-bind="getaccount.additional_amount|number:2||0"></span>元</span></dd>
          <!-- <dd><span class="name">收益包含奖励：</span><span class="val" ng-bind="getaccount.award_amount|number:2||0"></span>元</dd> -->
        </dl>
        <div class="err"></div>
        <div class="item-foot">
          <a class="btn btn-lg btn-block btn-blue" id="btn-loan" ng-if="loansimpleinfo.category_type !=3" ng-click="tenderNow()">马上投标</a>
          <a class="btn btn-lg btn-block btn-blue" id="btn-loan"  ng-if="loansimpleinfo.category_type==3" ng-click="tenderNow()">马上认购</a>
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
    <div class="page-settings">
  <!-- 联系客服 -->
    <div class="hotline-overlay" ng-if="loanbounty!=''" ng-click="contactsHide()"></div>
    <div class="hotline" >
       <div class="page-content page-bounty" ng-if="loanbounty!=''" >
    <ul>
       <li ng-repeat="bounty in loanbounty" class="undefalut"  ng-class="{on:counter.redbag == bounty.red.id}" ng-click="SelectBounty(bounty.red.id,bounty.red.amount,bounty.bountyType)">
          <div class="lileft">
            <div class="letop">
              <span class="dw">￥</span>
              <span class="rednumber" ng-bind="bounty.red.amount|number:2"></span>
              <p>投资抵用劵</p>
            </div>
            <!--  <div class="lebot" ng-bind="bounty.bountyType"></div> -->
          </div>
          <div class="liright">
            <div class="toptit">单笔投资年化金额满<span ng-bind="bounty.red.amountMin|number:0"></span>元使用</div>
            <div class="toptime"><span ng-bind="bounty.red.endTime|timestamp|date:'yyyy-MM-dd HH:mm:ss'"></span>到期</div>
          </div>
       </li>
    </ul>
  </div>
  <div class="page-content page-bounty" ng-if="loanbounty==''">
      <p style="text-align: center;margin:15px 0">暂无红包！</p>
  </div>
      <div class="fd1">
        <span class="bountytips">使用<span ng-bind="bountyData.Amount||0"></span>元<span ng-bind="bountyData.bountyType"></span></span>
        <a class="btn-lg btn-blue btn-check" ng-click="cBounty()">确定</a>
      </div>
    </div>
    <div class="hotline" ng-if="loanbounty!=''">
    </div>
  </section>
