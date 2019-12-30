<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<head>
	<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="mobile-web-app-capable" content="yes">
    <meta name="viewport" content="width=device-width,initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">
  <link rel="stylesheet" type="text/css" href="${system.themeDir}/css/waprisk.css">
</head>
<body>
<div class="lay-bg">
    <div class="lay">
        <h2 class="tit"><span>根据您的测评结果为您智能推荐！</span><span class="close">X</span></h2>
        <ul>
         <c:forEach var="dealer" items="${loanMapList}"> 
          <li class="uli">
            <h3><span class="dian"></span>${dealer['name']}</h3>
            <div class="cont">
                <dl class="w1">
                    <p class="c-red"><span class="fs-38">${dealer['apr']}</span>%</p>
                    <p>预期年化收益率</p>
                </dl>
                <dl class="w2">
                    <p class="c-gray"><span class="fs-28">${dealer['period']}</span>
                        	<c:if test="${dealer['repay_type']=='5' }">天</c:if>
                    	    <c:if test="${dealer['repay_type']!='5' }">个月</c:if>
                    </p>
                    <p>投资期限</p>
                </dl>
                <dl class="fr">
                    <a href="/wap/loan/loaninfoview#?id=${dealer['id']}" class="btns">立即投资</a>
                </dl>
            </div>
          </li> 
          </c:forEach>
        </ul>
    </div>
</div>
</body>
<script type="text/javascript" src="${system.themeDir}/js/wapauto.js"></script>
<script src="${system.themeDir}/js/plugins/jquery-1.8.2.min.js"></script>
<script>
$(".close").click(function(){
	window.location.href='/wap/member/index'
})
</script>
</html>