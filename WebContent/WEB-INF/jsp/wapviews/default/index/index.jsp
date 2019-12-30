<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <span>首页</span>
    <a href="/wap/index/settings" class="iconfont setting">&#xe61f;</a>
</nav>
<div class="page" ng-controller="indexCtrl">
    <div class="page-content">
        <!-- 焦点图 -->
        <div id="banner" class="slider">
            <div class="hd">
                <ul></ul>
            </div>
            <div class="bd">
                <ul>
					<li ng-repeat="bannerlist in banner">
						<a href="{{bannerlist.jumpurl}}"><img src="{{bannerlist.image}}" /></a>
					</li> 
                </ul>
            </div>
        </div>
        <div class="new-trailer">
            <div class="bd">
                <dl>
                    <dt>
                        <b ng-bind="newloan.apr | number:2"></b>%
                        <br/><span class="year">预期年化收益</span>
                    </dt>
                    <dt class="w42">
                        <b ng-bind-html="newloan.loan | number:2"></b>元
                        <br/><span class="year">借款金额</span>
                    </dt>
                    <dt>
 						<b ng-bind="newloan.period | number:0"></b><span ng-if="newloan.repay_type_id != 5">个月</span><span ng-if="newloan.repay_type_id == 5">天</span>                       
 						<br/><span class="year">借款期限</span>
                    </dt>
                </dl>
            </div>
            <div class="bt">
                <p class="fl"><i class="icon"></i>开始时间：<span ng-bind="newloan.begin_time | timestamp|date:'yyyy-MM-dd' ">
      </span></p>
                <p class="fr"><i class="icon pos-icon"></i>还款方式：<span ng-bind="newloan.repay_type_name"></span></p>
            </div>
            <div class="lbox-tit">新标预告</div>
        </div>
        <div class="loan-plan"  ng-if="loanone">
            <div class="loan-title" ng-bind="loanone.name"></div>
            <ul class="loan-opts">
                <li>
                    <a class="btn">本金保障</a>
                </li>
                <li>
                    <a class="btn btn2">稳定增值</a>
                </li>
                <li>
                    <a class="btn btn3">灵活投资</a>
                </li>
            </ul>
            <div class="loan-rate">
                <span class="val"><b ng-bind="loanone.apr"></b>%</span>
                <span class="name">预期年化收益</span>
            </div>
        </div>
        <div class="page-view mb10"  ng-if="loanone">
            <a class="btn btn-lg btn-block btn-blue" href="/wap/loan/loaninfoview#?id={{loanone.id}}">开始赚钱</a>
        </div>
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

