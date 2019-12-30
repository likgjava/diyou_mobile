<%@ page language="java" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="wapassets/trust/css/index.css">
<div ng-controller="indexCtrl" style="overflow-x: hidden;">
<nav class="nav-top navbar-fixed-top new-nav">
    <a href="/wap/index/settings" class="iconfont setting">&#xe61f;</a>
</nav>
<div class="banner" id="banner">
    <div class="hd">
        <ul>
            <li ng-repeat="list in banner"></li>
        </ul>
    </div>
    <div class="bd">
        <ul>
            <li ng-repeat="list in banner track by $index"><a ng-href="{{list.jumpurl}}"><img ng-src="{{list.image}}" src="wapassets/trust/images/index/banner.png" alt=""></a></li>
        </ul>
    </div>
</div>
<div class="count-box">
    <ul>
        <li>
            <p class="amount" ng-bind="loantotal.tenderTotal|judge|number:0">5,011,231,050</p>
            <p class="txt">投资金额(元)</p>
        </li>
        <li>
            <p class="amount" ng-bind="loantotal.web_reg_num">246,718</p>
            <p class="txt">注册用户(个)</p>
        </li>
        <li>
            <p class="amount" ng-bind="loantotal.tenderAward|judge|number:0">8,866,847.68</p>
            <p class="txt">赚取收益(元)</p>
        </li>
        <li>
            <p class="amount" ng-bind="loantotal.safeTotal">5,011,231,050</p>
            <p class="txt">风险缓释金(元)</p>
        </li>
    </ul>
</div>
<div class="advantage">
    <ul>
        <li>
            <p><img src="wapassets/trust/images/index/aimg1.png"></p>
            <p class="title">国资控股</p>
            <p>首都城投战略投资</p>
            <p>国资背景实力雄厚</p>
        </li>
        <li>
            <p><img src="wapassets/trust/images/index/aimg2.png"></p>
            <p class="title">安全保障</p>
            <p>银行资金托管</p>
            <p>360°全程监控</p>
        </li>
        <li>
            <p><img src="wapassets/trust/images/index/aimg3.png"></p>
            <p class="title">消费金融</p>
            <p>专注小额消费金融</p>
            <p>50+城市 美钱支持</p>
        </li>
    </ul>
</div>
<div class="newhand">
    <a ng-if="loanTopThree.new_loan" class="newhand-list" ng-href="/wap/loan/loaninfoview#?id={{loanTopThree.new_loan.id}}">
        <div class="title">
            <span class="tname" ng-bind="loanTopThree.new_loan.name">新手专享省心宝[20170703-6]</span>
            <span class="mark orange" ng-bind="loanTopThree.new_loan.marker_type"></span>
        </div>
        <div class="content">
            <ul>
                <li>
                    <p class="c-orange"><span class="c-apr" ng-bind="loanTopThree.new_loan.apr">9.5</span>%</p>
                    <p>年化收益</p>
                </li>
                <li>
                    <p><span class="c-perid" ng-bind="loanTopThree.new_loan.toDayAndMonth"></span></p>
                    <p>投资期限</p>
                </li>
                <li>
                    <span class="pro-bar">
                         <i class="pro-bar-bg" ng-style="{width: loanTopThree.new_loan.progress+'%'}">
                         </i>
                    </span>
                    <span class="pro-nums"><em ng-bind="loanTopThree.new_loan.progress||0">80</em>%</span>
                </li>
            </ul>
        </div>
    </a>
    <a ng-if="loanTopThree.loan_one" class="newhand-list" ng-href="/wap/loan/loaninfoview#?id={{loanTopThree.loan_one.id}}">
        <div class="title">
            <span class="tname" ng-bind="loanTopThree.loan_one.name">新手专享省心宝[20170703-6]</span>
            <span class="mark blue" ng-bind="loanTopThree.loan_one.marker_type"></span>
        </div>
        <div class="content">
            <ul>
                <li>
                    <p class="c-orange"><span class="c-apr" ng-bind="loanTopThree.loan_one.apr">9.5</span>%</p>
                    <p>年化收益</p>
                </li>
                <li>
                    <p><span class="c-perid" ng-bind="loanTopThree.loan_one.toDayAndMonth"></span></p>
                    <p>投资期限</p>
                </li>
                <li>
                    <span class="pro-bar">
                         <i class="pro-bar-bg" ng-style="{width: loanTopThree.loan_one.progress+'%'}">
                         </i>
                    </span>
                    <span class="pro-nums"><em ng-bind="loanTopThree.loan_one.progress||0">80</em>%</span>
                </li>
            </ul>
        </div>
    </a>
    <a ng-if="loanTopThree.loan_two" class="newhand-list" ng-href="/wap/loan/loaninfoview#?id={{loanTopThree.loan_two.id}}">
        <div class="title">
            <span class="tname" ng-bind="loanTopThree.loan_two.name">新手专享省心宝[20170703-6]</span>
            <span class="mark lorange" ng-bind="loanTopThree.loan_two.marker_type"></span>
        </div>
        <div class="content">
            <ul>
                <li>
                    <p class="c-orange"><span class="c-apr" ng-bind="loanTopThree.loan_two.apr">9.5</span>%</p>
                    <p>年化收益</p>
                </li>
                <li>
                    <p><span class="c-perid" ng-bind="loanTopThree.loan_two.toDayAndMonth"></span></p>
                    <p>投资期限</p>
                </li>
                <li>
                    <span class="pro-bar">
                         <i class="pro-bar-bg" ng-style="{width: loanTopThree.loan_two.progress+'%'}">
                         </i>
                    </span>
                    <span class="pro-nums"><em ng-bind="loanTopThree.loan_two.progress||0">80</em>%</span>
                </li>
            </ul>
        </div>
    </div>
</a>
<div class="new-invest">
    <div class="amount">
        <p class="tit">总资产额</p>
        <p class="money">￥<span ng-bind="getLoanSum.amountSum"></span></p>
    </div>
    <div class="invest-tit">
        <span class="topic">智能投顾 </span>
        <span class="notic">为您私人定制多元化的资产配置组合</span>
    </div>
    <div class="invest-con" id="main" style="width:4rem;margin-right:1rem;height:4rem;padding:0.2rem;"></div>
    <ul class="invest-color">
        <li ng-repeat="list in tenderFinance">
            <div class="cir" style="background: {{list.value}};"></div>
            <span class="text" ng-bind="list.name">车商贷</span>
        </li>
    </ul>
</div>
<div class="icome">
    <div class="icome-tit">您的<span>收益</span>偏好等级</div>
    <div class="slide-line">
        <span class="inner-style" ng-style="{width: (levelData.earnings-5)*10+'%'}"><i class="cir-btn"></i></span>
    </div>
    <div class="list-num">
        <div class='ruler'>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
            <div class='cm'></div>
        </div>
    </div>
</div>
<div class="risk">
    <div class="icome-tit">您的<span>风险</span>偏好等级</div>
    <div class="slide-line">
        <span class="inner-style" ng-style="{width: levelData.scale+'%'}"><i class="cir-btn"></i></span>
    </div>
    <div class="list-num2">
        <div class='cm'></div>
        <div class='cm'></div>
        <div class='cm'></div>
        <div class='cm'></div>
        <div class='cm'></div>
    </div>
</div>
<div class="copyright">
    <p ng-bind="siteConfig.site_copyright.value">深圳前海融金所互联网金融服务有限公司 版权所有</p>
    <p ng-bind="siteConfig.site_license_number.value">粤ICP备13026617号-1丨粤ICP证B2-20160354号</p>
    <p><span class="c-blue" ng-bind="siteConfig.service_tel.value">400-7779960</span> <span ng-bind="siteConfig.service_hours.name"></span><span ng-bind="siteConfig.service_hours.value">9:00-18:00</span></p>
</div>
<div class="new-footer">
    <ul>
        <li>
            <a href="#" class="active">
                <span class="listimg1"></span><br/>
                <span>首页</span>
            </a>
        </li>
         <li>
            <a href="#">
                <span class="listimg2"></span><br/>
                <span>理财</span>
            </a>
        </li>
         <li>
            <a href="#">
                <span class="listimg3"></span><br/>
                <span>发现</span>
            </a>
        </li>
         <li>
            <a href="#">
                <span class="listimg4"></span><br/>
                <span>我的</span>
            </a>
        </li>
    </ul>
</div>
<!-- 加载状态 -->
  <div class="loader" ng-class="{hid:loading}">
      <div class="loader-inner"></div>
  </div>
</div>
<!-- 网页底部 -->
<jsp:include page="../common/bottomPage.jsp">
  <jsp:param name="bright_index" value="active" />
</jsp:include>
