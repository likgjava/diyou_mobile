<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
    <div class="container">
      <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span>债权详情</span>
    </div>
</nav>
<section class="page" ng-controller="mytransferViewCtrl">
    <div class="page-content">
      <div class="transfer-view">
        <div class="cont">
          <dl><!-- ng-bind="loan_info.left_amount|number:2" -->
            <dt ng-bind="transferDetail.loan_name"></dt>
            <dd><span class="name">债权价值：</span><span ng-bind="transferDetail.amount_money|number:2"></span>元</dd>
            <dd><span class="name" ng-if="transferDetail.transfer_status!=2">还款期限：</span><span class="name" ng-if="transferDetail.transfer_status==2">转让时间：</span><span ng-bind="transferDetail.recover_time">0000年00月00日</span></dd>
            <dd><span class="name" ng-if="transferDetail.transfer_status!=2">转让期数/总期数：</span><span class="name" ng-if="transferDetail.transfer_status==2">转让期数/总期数：</span><span ><span ng-bind="transferDetail.period"></span>期/共<span ng-bind="transferDetail.total_period"></span>期</span></dd>
          </dl>          
        </div>

        <div class="list-block">
          <ul>
          <li>
            <div class="item-content">
              <div class="item-title">转让系数：</div>
              <div class="item-input col-xs-6">
                <input type="text" class="txt" maxLength="3" placeholder="{{transferDetail.transfer_coefficient_min}}%~{{transferDetail.transfer_coefficient_max}}%" ng-model="formData.coefficient" ng-change="getAmount()" ng-if="transferDetail.transfer_status==-2||transferDetail.transfer_status==-1">

                <input type="text" class="txt" ng-value="transferDetail.coefficient" readonly="true" ng-if="transferDetail.transfer_status!=-2||transferDetail.transfer_status!=-1">              
              </div>
              <div class="item-right">%</div>
            </div>
          </li>
          </ul>
        </div>
        <div class="item-text">
          <span class="name">转让价格：</span><span class="val c-blue">￥<span ng-bind="transferDetail.amount||0|number:2"></span></span>
        </div>
        <div class="item-text">
          <span class="name">转让手续费：</span><span class="val c-blue">￥<span ng-bind="transferfee||0|number:2"></span></span>
        </div>

        <div class="page-view">  
          <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>   
          <!-- 立即转让/转让详情 -->
          <input type="button" class="btn btn-block btn-lg btn-blue" value="立即转让" ng-if="transferDetail.transfer_status==-2||transferDetail.transfer_status==-1" ng-click="transferChange()" >

          <a class="btn btn-block btn-lg btn-orange" ng-if="transferDetail.transfer_status==1" ng-click="transferCancel(transferDetail.transfer_id)">撤销转让</a>
          
        <!--   <a class="btn btn-block btn-lg btn-gray" ng-if="transferDetail.transfer_status==2" ng-href="/wap/transfer/transferInfo#?id={{transferDetail.transfer_id}}">债权详情</a> -->
        <a class="btn btn-block btn-lg btn-gray" ng-if="transferDetail.transfer_status==2">已转让</a>
          <div class="loan-alert">
            <p>温馨提示：</p>
            <p>1、正在转让的债权若有还款，则转让中的数据会自动撤销，进入到可以转让和已撤销的债权列表中</p>
            <%-- <p>2、债权转让给网站，网站购买债权标准为：（待还本金+待还利息）*0.8。</p>--%>
          </div>
        </div>

        
        <!-- 输入支付 -->
        <div class="overlay"></div>
        <div class="tender-pay">
          <div class="hd">
            <i class="iconfont back" ng-click="playPwdHide()">&#xe604;</i>
            <span class="title">输入支付密码</span>                        
          </div>
          <div class="bd">            
              <div class="form-group">
                <input type="password" class="form-control" value="" placeholder="请输入支付密码" ng-model="formData.paypassword">
              </div>             
              <div class="form-group fn-clear">
                <a class="btn btn-lg" ng-click="playPwdHide()">取消</a>
                <a class="btn btn-lg" ng-click="transferSubmit()" ng-disabled="tenderBtn.status" ng-bind="tenderBtn.text"></a>
              </div>
              <div class="form-group" style="height:20px;">
                <span class="err" ng-bind="errorPlace"></span>
              </div>
          </div>
        </div> 

      <!-- 加载状态 -->
      <div class="loader" ng-class="{hid:loading}">
        <div class="loader-inner"></div>
      </div>	  
    </div>
	</div>
</section>

