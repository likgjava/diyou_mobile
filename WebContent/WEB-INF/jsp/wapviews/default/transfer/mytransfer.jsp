<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a href="wap/member/index" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>我的项目</span>
    </div>
  </nav>
  <section class="page" ng-controller="mytransferCtrl">

    <div class="page-content page-user page-transfer">
      <div class="loan-nav">
        <a href="/wap/member/mytender" class="col-xs-6 link">我的投资</a>
        <a href="/wap/transfer/mytransfer" class="col-xs-6 link active">我的债权</a>
      </div>

      <div class="transfer-info">
        <dl>
          <!-- <dt ng-bind="loan_info.name"></dd> -->
          <dt>债权盈亏</dt>
          <dd><span class="name">成功转让金额</span><span ng-bind="transferinfo.transfer_total|number:2" class="ng-binding"></span>元</dd>
          <dd><span class="name">债权转出盈亏</span><span ng-bind="transferinfo.transfer_interest_total|number:2" class="ng-binding"></span>元</dd>
          <dd><span class="name">成功购入金额</span><span ng-bind="transferinfo.transfer_buy_total|number:2" class="ng-binding"></span>元</dd>
          <dd><span class="name">债权购入盈亏</span><span ng-bind="transferinfo.transfer_buy_interest_total|number:2" class="ng-binding"></span>元</dd>
        </dl>
      </div>


      <div class="loan-tabs" id="loanTabs">
        <div class="hd">
          <ul>
            <li class="on">转让记录</li>
            <li ng-click="getRecord()">购买记录</li>
          </ul>
        </div>
        <div class="bd">

          <!--转让记录 ng-if="transfer_list.length>0"-->
          <div class="bd-item on">
            <div class="bd-list" ng-if="transfer_list.total_items>0">
              <div class="row row-head">
                <div class="col-xs-4 c1">待收本息(元)</div>
                <div class="col-xs-4 c2">待还/总期数</div>
                <div class="col-xs-4 c3">状态</div>
              </div>
              <div class="row-body">
                <a class="row" ng-repeat="item in transfer_list.items" ng-href="/wap/transfer/myTransferDetail#?id={{item.id}}">
                  <div class="col-xs-4 c1"><span ng-bind="item.wait_recover_principal+item.wait_recover_interest||0|number:2"></span></div>
                  <div class="col-xs-4 c2"><span ng-bind="item.wait_period||0"></span>/<span ng-bind="item.period||0"></span></div>
                  <div class="col-xs-4 c3"><span class="s{{item.transfer_status}}" ng-bind="item.transfer_status_name"></span></div>
                </a>
              </div>
              <div class="row-foot">
                <a class="btn btn-default btn-block" ng-click="getMoreList()" ng-if="getMoreStatus && !loader">点击加载更多</a>
                <div class="preloader" ng-class="{loading:loader}"></div>
                <div class="txt-cent" ng-if="!getMoreStatus">没有更多数据</div>
              </div>
            </div>
            <div class="noData" ng-if="transfer_list.total_items==0">暂无转让记录</div>
          </div>

          <div class="bd-item">
            <div class="bd-list" ng-if="record_list.total_items>0">
              <div class="row row-head">
                <div class="col-xs-4 c1">债权价值(元)</div>
                <div class="col-xs-4 c2">待还/总期数</div>
                <div class="col-xs-4 c3">状态</div>
              </div>
              <div class="row-body">
                
                <a class="row" ng-repeat="item in record_list.items" ng-href="/wap/transfer/myTransferInfo#?id={{item.transfer_id}}">
                  <div class="col-xs-4 c1"><span ng-bind="item.amount_moeny||0|number:2"></span></div>
                  <div class="col-xs-4 c2"><span ng-bind="item.wait_period||0"></span>/<span ng-bind="item.period||0"></span></div>
                  <div class="col-xs-4 c3"><span class="s{{item.transfer_status}}" ng-bind="item.buy_repay_status"></span></div>
                </a>
              </div>
              <div class="row-foot">
                <a class="btn btn-default btn-block" ng-click="getMoreList2()" ng-if="getMoreStatus2 && !loader">点击加载更多</a>
                <div class="preloader" ng-class="{loading:loader}"></div>
                <div class="txt-cent" ng-if="!getMoreStatus2">没有更多数据</div>
              </div>
            </div>
            <div class="noData" ng-if="record_list.total_items==0">暂无购买记录</div>
          </div>
        </div>
      </div>




      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
    </div>
  </section>