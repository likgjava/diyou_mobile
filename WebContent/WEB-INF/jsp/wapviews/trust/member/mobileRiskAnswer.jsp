<%@ page language="java" pageEncoding="UTF-8"%>
<link rel="stylesheet" type="text/css" href="${system.themeDir}/css/wenjuan/layout.css">
<link rel="stylesheet" type="text/css" href="${system.themeDir}/css/wenjuan/questionnaire.css">
<div ng-controller="mobileRiskAnswerCtrl">
<div class="questionnaireWrap">
	<div class="desc-box">
		<p>以下10个问题将根据您的财务状况、投资经验、投资风格、风险偏好和风险承受能力等对您进行风险评估，我们将根据评估结果为您更好的配置资产。请您认真作答，感谢您的配合！</p>
		<p class="orange">(每个问题请选择唯一选项，不可多选)</p>
	</div>
	<input type="hidden" value="${id}" id="id">
	<form class="Jvalidation" action="" method="">
		<input type="hidden" ng-model="formData.id" ng-init="formData.id=${id}">
		<section class="ques-item" ng-repeat="list in answers">		
			<div class="title orange" ng-bind="list.title"></div>
			<ul>
				<li ng-repeat="ans in list.question">
					<div class="radio-box">
						<input type="radio" name="answers_{{list.id}}" ng-value="ans.num" ng-model="formData['answers_'+list.id]">
						<i class="check"></i>
					</div>
					<span class="txt" ng-bind="ans.num+'. '+ans.description"></span>
				</li>
				
			</ul>
		</section>
		<input type="hidden" name="type" id="type" value="${type}" >
		<input type="submit" class="button" ng-disabled="btnStatus" value="提&nbsp;&nbsp;交" ng-click="submitFn()">
	</form>
</div>
	<div class="mask hid" id="j-mask"></div>
	<div class="prompt-wrap hid" id="j-prompt-wrap">
		<h4>温馨提示</h4>
		<h5>RISK WARNING</h5>
		<P class="green">您提供的信息应当真实、准确、完整，我们的风险评估将基于您提供的有效信息做评估，如因您提供虚假、无效或者不完整地信息，导致的评价结果出现错误，平台将不承担相应责任。</P>
		<P class="black">本测试结果的有效期为24个月，如您的财务状况发生较大变化或发生可能影响您风险承受能力的其他情况，请您及时通知我们并重新进行测试。</P>
		<P class="orange">风险提示：市场有风险，投资需谨慎。购买投资产品前，请认真阅读相关产品合同、协议书、说明书、招募说明书等法律文件，充分了解投资的风险。</P>
		<a href="javascript:;" class="close-btn" ng-click="closeDialog()"></a>
	</div>
</div>
<script type="text/javascript">
	(function(e, t) {
		    function n() {
		        for (var e = navigator.userAgent,
		        t = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod"], n = !0, i = 0; t.length > i; i++) if (e.indexOf(t[i]) > 0) {
		            n = !1;
		            break
		        }
		        return n
		    }
		    function i() {
		        var t = a.getBoundingClientRect().width;
		        n() && 2047 > t && (t = 640);
		        var i = t * 100 / w;
		        a.style.fontSize = i + "px",
		        d.rem = e.rem = i
		    }
		    var r, o = e.document,
		    a = o.documentElement,
		    s = o.querySelector('meta[name="viewport"]'),
		    c = o.querySelector('meta[name="flexible"]'),
		    l = 0,
		    u = 0,
		    d = t.flexible || (t.flexible = {});
		    var w = o.querySelector('meta[name="W_design"]') ? o.querySelector('meta[name="W_design"]').getAttribute('content') : 640;
		    if (s) {
		        // console.warn("将根据已有的meta标签来设置缩放比例");
		        var p = s.getAttribute("content").match(/initial\-scale=([\d\.]+)/);
		        p && (u = parseFloat(p[1]), l = parseInt(1 / u))
		    } else if (c) {
		        var f = c.getAttribute("content");
		        if (f) {
		            var h = f.match(/initial\-dpr=([\d\.]+)/),
		            m = f.match(/maximum\-dpr=([\d\.]+)/);
		            h && (l = parseFloat(h[1]), u = parseFloat((1 / l).toFixed(2))),
		            m && (l = parseFloat(m[1]), u = parseFloat((1 / l).toFixed(2)))
		        }
		    }
		    if (!l && !u) {
		        e.navigator.appVersion.match(/android/gi);
		        var g = e.navigator.appVersion.match(/iphone/gi);
		        e.navigator.appVersion.match(/ipad/gi);
		        var v = e.devicePixelRatio;
		        l = g ? v >= 3 && (!l || l >= 3) ? 3 : v >= 2 && (!l || l >= 2) ? 2 : 1 : 1,
		        u = 1 / l
		    }
		    if (a.setAttribute("data-dpr", l), !s) if (s = o.createElement("meta"), s.setAttribute("name", "viewport"), s.setAttribute("content", "initial-scale=" + u + ", maximum-scale=" + u + ", minimum-scale=" + u + ", user-scalable=no"), a.firstElementChild) a.firstElementChild.appendChild(s);
		    else {
		        var y = o.createElement("div");
		        y.appendChild(s),
		        o.write(y.innerHTML)
		    }
		    e.addEventListener("resize",
		    function() {
		        clearTimeout(r),
		        r = setTimeout(i, 300)
		    },!1),
		    e.addEventListener("pageshow",
		    function(e) {
		        e.persisted && (clearTimeout(r), r = setTimeout(i, 300))
		    },!1),
		    "complete" === o.readyState ? o.body.style.fontSize = 12 * l + "px": o.addEventListener("DOMContentLoaded",
		    function() {
		        o.body.style.fontSize = 12 * l + "px"
		    },!1),
		    i(),
		    d.dpr = e.dpr = l,
		    d.refreshRem = i,
		    d.rem2px = function(e) {
		        var t = parseFloat(e) * this.rem;
		        return "string" == typeof e && e.match(/rem$/) && (t += "px"),
		        t
		    },
		    d.px2rem = function(e) {
		        var t = parseFloat(e) / this.rem;
		        return "string" == typeof e && e.match(/px$/) && (t += "rem"),
		        t
		    }
		})(window, window.lib || (window.lib = {}));
		
</script>
<%--<div ng-controller="riskAnswerCtrl">
 <form action="" method="" id="risk_form" name="risk_form">
    <div>
        <div>
            <div>
                <h2>风险测评</h2>
                <a href="wap/risk/getRiskRecordList">风险测评信息</a>
            </div>
            
             <table>
                        <tbody>
                            <tr ng-repeat="list in answers">
                                <td width="5%">
                                	<input hidden="true" type="text" value="{{list.id}}" name="questionId"/>
                                	{{$index+1}}.&nbsp;&nbsp;<input type="text" value="{{list.title}}" name="title"/><br>
                                	<div ng-repeat="ans in list.question">
                                		<input type="checkbox" name="answers_{{list.id}}" id="ans_{{ans.num}}" value="{{ans.num}}" ng-checked="{{ans.isChose}}"  />{{ans.num}}. {{ans.description}}
                                	</div>
                                </td>
                                
                            </tr>
                        </tbody>
                    </table>
			          <div>
	                    <input type="submit" value="提交">
	                </div>
            </div>
        </div>
    </form>
</div>

<script src="wapassets/trust/js/plugins/jquery-1.8.2.min.js" type="text/javascript"></script>
<script src="wapassets/trust/js/plugins/jquery.form.js" type="text/javascript"></script>

<script>

 $('#risk_form').bind("submit", function() {
       $('#risk_form').find(":submit").addClass("disBtn").attr("disabled", true).attr("value", "提交中...");
       $('#risk_form').ajaxSubmit({
           url: "/wap/answer/submit",
           type: "POST",
           dataType: 'json',
           success: function(results) {
           alert(results.description);
               if (results.status == '200') {
                   layer.msg(results.description, 1, 1, function() {
                       location.reload();
                   });
               } else {
                   layer.msg(results.description, 1, 2, function() {
                       $('#risk_form').find(":submit").removeClass("disBtn").attr("disabled", false).attr("value", "确认提交");
                   });
               }
           }
       });
       return false;
   });

</script>
--%>