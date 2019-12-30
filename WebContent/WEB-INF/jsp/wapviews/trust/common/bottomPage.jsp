<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
  String bright_loan=request.getParameter("bright_loan");
  String bright_index=request.getParameter("bright_index");
  String bright_personal=request.getParameter("bright_personal");
  String light=request.getParameter("light");
 %>
<c:if test="${isBottom ne false}">
<div class="new-footer">
    <ul>
        <li>
            <a href="/" class="active">
                <span class="listimg1"></span><br/>
                <span>首页</span>
            </a>
        </li>
         <li>
            <a href="/wap/loan/loantender">
                <span class="listimg2"></span><br/>
                <span>理财</span>
            </a>
        </li>
        <!--  <li>
            <a href="/wap/index/find">
                <span class="listimg3"></span><br/>
                <span>发现</span>
            </a>
        </li> -->
         <li>
            <a href="/wap/member/index">
                <span class="listimg4"></span><br/>
                <span>我的</span>
            </a>
        </li>
    </ul>
</div>
</c:if>
<script>
var light = "<%=light%>";
if(light != "null"){
	document.getElementsByClassName(light)[0].parentNode.className="active";
}

// var light = "<%=light%>";
// document.getElementsByClassName(light)[0].parentNode.className="active";
// document.getElementsByClassName("listimg1")[0].parentNode.className="";
</script>
