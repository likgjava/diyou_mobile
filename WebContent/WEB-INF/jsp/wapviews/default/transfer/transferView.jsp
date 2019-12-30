<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>债权详情</span>
    </div>
</nav>
<section class="page" ng-controller="transferViewCtrl">
    <div class="page-content">
      <div class="tender-view">
        <div class="loan">
            <div class="loan-content loan-details">
              <div class="tit"><i class="iconfont">&#xe60e;</i>预期年化收益</div>
              <div class="rate">
                <span class="r1"><b ng-bind="loan_info.apr"></b>%</span>
                <span ng-if="loan_info.award_proportion>0" class="r2" ng-bind="'+'+loan_info.award_proportion+'%'"></span>
                <span ng-if="loan_info.award_amount>0" class="r2" ng-bind="'+'+loan_info.award_amount+'%'"></span>
                <i class="ico" ng-if="loan_info.award_status != -1">奖</i>
              </div>
            </div>
            <div class="loan-foot">
              <span class="com col-xs-8" ><i class="iconfont">&#xe60f;</i>担保方：<span ng-bind="loan_info.vouch_company_info || '无'"></span></span>
              <span class="num col-xs-4"><i class="iconfont icon-num">&#xe611;</i>投资人数：<span ng-bind="loan_info.tender_count"></span></span>
            </div>
        </div>

        <div class="loan-info">
          <div class="hd">债权信息</div>
          <div class="bd">
            <dl>
              <dd><span class="name">待收本金</span><span ng-bind="transfer_ret.wait_principal|number:2"></span>元</dd>
              <dd><span class="name">待收利息</span><span ng-bind="transfer_ret.wait_interest|number:2"></span>元</dd>
              <dd><span class="name">债权价值</span><span ng-bind="transfer_ret.amount_money|number:2"></span>元</dd>
              <dd class="tip">（待收本金+当期利息*距离上一期还款的天数/30）</dd>
              <dd><span class="name">转让价格</span><span ng-bind="transfer_ret.amount|number:2"></span>元</dd>
              <dd><span class="name">转让收益</span>待收本息-转让价格=<span ng-bind="transfer_ret.income|number:2"></span>元</dd>
            </dl>  
          </div>
        </div>

        <div class="loan-main">
          <div class="cont">
            <dl>
              <dt ng-bind="loan_info.name"></dt>
              <dd><span class="name">融资金额</span><span ng-bind="loan_info.amount|number:2"></span>元</dd>
              <dd><span class="name">借款期限</span><span ng-bind="loan_info.period_name"></span></dd>
              <dd><span class="name">还款方式</span><span ng-bind="repay_type.name"></span></dd>
              <dd><span class="name">还款进度</span>已还<span ng-bind="loan_info.period_yes||0">2</span>期/共<span ng-bind="transfer_ret.total_period">12</span>期</dd>
              <dd ng-if="loan_info.next_repay_time!=null"><span class="name">还款期限</span><span ng-bind="loan_info.next_repay_time|timestamp|date:'yyyy-MM-dd'"></span></dd>
            </dl>

            <div class="progbar" >
              <div class="prog prog-bg"></div>
              <div class="prog prog-bg2"></div> 
              <div class="prog prog-rount" style="-webkit-transform:rotate({{loan_info.progress*360/100}}deg);" ng-if="loan_info.progress<=50"></div>
              <div class="prog prog-rount" style="-webkit-transform:rotate(180deg);" ng-if="loan_info.progress>50"></div>
              <div class="prog prog-rount2" style="-webkit-transform:rotate({{(loan_info.progress-50)*360/100}}deg);" ng-if="loan_info.progress>50"></div>
              <div class="prog-text t{{loan_info.status}}" ng-if="loan_info.progress<100">{{loan_info.progress|number:0}}%</div>
              <div class="prog-text t{{loan_info.status}}" ng-if="loan_info.progress==100">{{loan_info.status_name}}</div>
            </div>

            <div class="safe">
              <span class="col-xs-4"><a ng-click="look('')"><i class="iconfont"></i>{{loan_info.agreementTitle}}</a></span>
              <span class="col-xs-4"><a ng-click="look('contract')"><i class="iconfont"></i>投资合同范本</a></span>
              <span class="col-xs-4"><i class="icon"></i><span ng-bind="loan_info.category_name"></span></span>
            </div>
          </div>

          <div class="loan-tabs" id="loanTabs">
              <div class="hd">
                <ul>
                  <li class="on">项目详情</li>
                  <li ng-if="loan_info.is_company!=1" >借款人信息</li>
                  <li ng-if="loan_info.is_company!=-1" >企业信息</li>
                  <li>还款计划</li>
                  <li>投资列表</li>
                </ul>
              </div>
              <div class="bd">
                <!--项目详情-->
                <div class="bd-item on" ng-bind-html="loan_info.contents">

                </div>

                <!--借款人信息-->
                 <div class="bd-item bd-info" ng-if="loan_info.is_company!=1">
                    <div class="ibox">
                      <div class="tit"><i class="iconfont">&#xe619;</i>借款人信息</div>
                      <dl class="con">      
                        <dd><span class="name">借款人</span><span class="val" ng-bind="loan_info.member_name ||'无'"></span></dd>
                        <dd><span class="name">性别</span><span class="val" ng-bind="member_info.gender ||'无'"></span></dd>
                        <dd><span class="name">月收入</span><span class="val" ng-bind="member_info.monthly_income_name ||'无'"></span></dd>
                        <dd><span class="name">结婚状况</span><span class="val" ng-bind="member_info.marry_name ||'无'"></span></dd>
                        <dd><span class="name">学历</span><span class="val" ng-bind="member_info.edu_name ||'无'"></span></dd>                        
                      </dl>
                    </div>

                    <div class="ibox">
                      <div class="tit"><i class="iconfont">&#xe618;</i>工作信息</div>
                      <dl class="con">
                        <dd><span class="name">工作城市</span><span class="val" ng-bind="member_info.areas ||'无'"></span></dd>
                        <dd><span class="name">工作性质</span><span class="val" ng-bind="member_info.industry_name ||'无'"></span></dd>
                        <dd><span class="name">公司规模</span><span class="val" ng-bind="member_info.company_scale_name ||'无'"></span></dd>
                        <dd><span class="name">岗位职位</span><span class="val" ng-bind="member_info.company_office_name ||'无'"></span></dd>                
                      </dl>
                    </div>
                </div>
<!--企业信息-->
                <div class="bd-item bd-info" ng-if="loan_info.is_company==1">
                    <div class="ibox">
                      <div class="tit"><i class="iconfont">&#xe619;</i>企业信息</div>
                      <dl class="con">      
                        <dd><span class="name">企业名称</span><span class="val" ng-bind="company_info.name ||'无'"></span></dd>
                        <dd><span class="name">注册资金</span><span class="val" ng-bind="company_info.account ||'无'"></span></dd>
                        <dd><span class="name">成立日期</span><span class="val" ng-bind="company_info.establishment_date ||'无'"></span></dd>
                        <dd><span class="name">所在地</span><span class="val" ng-bind="company_info.place+' '+company_info.address ||'无'"></span></dd>
                        <dd><span class="name">营业地址</span><span class="val" ng-bind="company_info.address ||'无'"></span></dd> 
                        <dd><span class="name">企业简介</span><span class="val" ng-bind="company_info.company_intro ||'无'"></span></dd> 
                        <dd><span class="name">抵质押物</span><span class="val" ng-bind="company_info.collateral ||'无'"></span></dd>                        
                      </dl>
                    </div>
                </div>
                <!--还款计划-->
                <div class="bd-item bd-plan" ng-if="repay_plan.length>0">
                    <div class="row row-head">
                      <div class="col-xs-4">预期还款时间</div>
                      <div class="col-xs-3">类型</div>
                      <div class="col-xs-3">金额(元)</div>
                      <div class="col-xs-2">状态</div>
                    </div>
                    <div class="row" ng-repeat="item in repay_plan">
                      <div class="col-xs-4"><span class="g9" ng-bind="item.repay_date"></span></div>
                      <div class="col-xs-3"><span class="g9" ng-bind="item.type_name"></span></div>
                      <div class="col-xs-3" ng-bind="item.total|number:2"></div>
                      <div class="col-xs-2"><span class="c-blue" ng-bind="item.repay_type_name"></span></div>
                    </div>
                </div>
                <!--还款计划无数据-->
                <div class="bd-item bd-list txt-cent pb10" ng-if="repay_plan.length==0">
                  暂无数据
                </div>

                <!--还款计划-->
                <div class="bd-item bd-list" ng-if="tender_list.length>0">
                  <div class="row row-head">
                    <div class="col-xs-4 name">投标人</div>
                    <div class="col-xs-4 money">投资金额(元)</div>
                    <div class="col-xs-4 time">投资时间</div>
                  </div>

                  <div class="row" ng-repeat="item in tender_list">
                    <div class="col-xs-4 name" ng-bind="item.member_name"></div>
                    <div class="col-xs-4 money" ng-bind="item.amount"></div>
                    <div class="col-xs-4 time" ng-bind="item.add_time|timestamp|date: 'yyyy-MM-dd'"></div>
                  </div>

                  <div class="row-foot fn-clear">
                    <div class="fr">
                      <div class="t1"><i class="iconfont">&#xe601;</i>投资总额：<span ng-bind="loan_info.credited_amount"></span>元</div>
                      <div class="t2"><i class="iconfont">&#xe603;</i>投资人次： <span ng-bind="loan_info.tender_count"></span>人</div>
                    </div>
                  </div>
                </div>
                <!--投资列表无数据-->
                <div class="bd-item bd-list txt-cent pb10" ng-if="tender_list.length==0">
                  暂无数据
                </div>

              </div>
            </div>
		
        <div class="loan-bid">
          <i class="iconfont counter">&#xe60d;</i>

          <a class="btn btn-blue btn-block" disabled="disabled" ng-if="transfer_ret.status==2">转让成功</a>

          <a class="btn btn-blue btn-block" href="/wap/transfer/transferinvest#?id={{typeId.id}}" ng-if="transfer_ret.status==1">立即购买</a>

        </div>
      </div> 
      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>
      <!--理财计算器-->
      <jsp:include   page="../common/counter.jsp" /> 	 	  
    </div>
	</div>
</section>

