<%@ page language="java" pageEncoding="UTF-8"%>
<div ng-controller="resultAnswerCtrl">
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a href="/wap/member/index" class="go-back" ><span class="iconfont">&#xe604;</span></a>
      <span>风险测评</span>
    </div>
</nav>
<section class="page">
  	<div class="page-content">
	    <div ng-controller="resultAnswerCtrl">

	    	<p style="font-size:12px;color:#666; line-height:30px;margin-top:50px;text-align: center;">风险测评等级为</p>
	    	<p ng-bind="level" style="font-size:18px;color:#333; line-height:30px;margin-bottom:50px;text-align: center;"></p>
	    </div>
	    <div style="text-align: center;">
	    	<a href="/wap/answer/record?type=echo" style="width:90%;font-size:14px;color:#333;border:1px solid #ddd;line-height:30px;display:inline-block;text-algin:center;background:#fff;border-radius:5px;">重新测评</a>
	    </div>
	</div>
</section>
</div>
