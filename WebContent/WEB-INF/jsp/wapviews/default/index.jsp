<%@ page language="java" pageEncoding="UTF-8"%>
<%
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <base href="/" />
        <title>${system.siteName}</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="viewport" content="user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimal-ui" />
        <meta name="apple-mobile-web-app-status-bar-style" content="yes" />
        <link href="${system.siteIco}" rel="shortcut icon" type="image/x-icon" />
        <link href="${system.themeDir}/css/bootstrap.min.css" type="text/css" rel="stylesheet"/>
        <link href="${system.themeDir}/css/iconfont.css" type="text/css" rel="stylesheet"/>
        <link href="${system.themeDir}/css/dyapp.css" type="text/css" rel="stylesheet"/>
        <link href="${system.themeDir}/css/extend.css" type="text/css" rel="stylesheet"/>
        <script src="${system.themeDir}/js/diyou.js"></script>
        <script src="${system.themeDir}/js/base.js"></script>
      	
    </head>
    <body ng-controller="MobileMain">
        <jsp:include page="${system.contentPage}" />
        <script type="text/javascript">
            diyou.use(['app'], function(app) {
                angular.bootstrap(document, ['app']);
            });
        </script>
    </body>
</html>