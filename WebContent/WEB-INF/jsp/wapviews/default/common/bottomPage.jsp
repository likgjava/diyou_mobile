<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
  String bright_loan=request.getParameter("bright_loan");
  String bright_index=request.getParameter("bright_index");
  String bright_personal=request.getParameter("bright_personal");
 %>
<c:if test="${isBottom ne false}">
<nav class="nav-bottom navbar-fixed-bottom">
    <div class="nav-item col-xs-4 <%=bright_index%>">
        <a href="/">
            <span class="iconfont">&#xe600;</span>
            <span>首页</span>
        </a>
    </div>
    <div class="nav-item col-xs-4 <%=bright_loan%>">
        <a href="/wap/loan/loantender">
            <span class="iconfont">&#xe601;</span>
            <span>我要投资</span>
        </a>
    </div>
    <div class="nav-item col-xs-4 <%=bright_personal%>">
        <a href="/wap/member/index">
            <span class="iconfont">&#xe603;</span>
            <span>个人中心</span>
        </a>
    </div>
</nav>
</c:if>