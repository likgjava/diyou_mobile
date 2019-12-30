<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
	String type = request.getAttribute("type") != null ? request.getAttribute("type").toString() : "";
	String url = basePath + "wap/member/index?" + type;
%>
<div class="page txt-cent back-msg">
 <div class="tit">${message}<%-- <input type="hidden" ng-model="${message}>" ng-init="${message}='信息提醒';"> --%></div>
 <div class="txt mt30"><span id="second">5</span>秒后系统会自动跳转,如果系统没有响应请<a href="/wap/member/mytender" >点击此处</a></div>
 <div class="key-box">
  <a href="/wap">关闭页面</a>
  <a class="on" href="${url}">立即跳转</a>
 </div>
 <div class="remark">
  <p>操作失败？</p>
  <p>1. 请重新请求申请页面</p>
  <p>2. 如再次失败，请联系客服处理！ </p>
  <p>3. 第三方已受理,平台操作失败?请联系客服！</p>
 </div>
</div>
<span id="redirectUrl" hidden>${url}</span>
<script type="text/javascript">
function refresh() {
    var second=parseInt($('#second').html());
    second--;
    $('#second').html(second);
    if(second==0){
     clearInterval(t);
     location.href = $('#redirectUrl').html();
    }
}
var t=setInterval("refresh()", 1000);
</script>
