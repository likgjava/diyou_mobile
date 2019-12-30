<nav class="nav-top navbar-fixed-top">
  <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
  <span class="title">添加银行卡</span>
</nav>      
<div class="page page-user" ng-controller="bankaddCtrl">
  <div class="page-content user-bank">
    <form w5c-form-validate="vm.validateOptions" novalidate name="validateForm" id="bankForm">
      <!--添加银行卡--> 
      <div class="list-block">
        <div class="list-title">请绑定通过实名认证的本人银行卡</div>
        <ul>
        <li>
          <div class="item-content">
            <div class="item-title " ng-bind="bankInfo.realname"></div>
            <div class="item-right c-blue">实名认证通过</div>
          </div>
        </li>
        </ul>
      </div>
      <div class="list-block">
        <ul>
        <li>
          <!-- 6223 0015 5251 1089 305 -->
          <div class="item-content">
            <div class="col-xs-4 item-title ">银行卡卡号</div>
            <div class="col-xs-8 item-input">
              <input class="txt" type="text" name="bankid" placeholder="请输入银行卡卡号" required="" ng-pattern="/^\d{16}|\d{19}$/" ng-model="formData.account">
            </div>
          </div>
        </li>
        <li>
          <div class="item-content" ng-click="bankList()">
            <div class="col-xs-5 item-title">选择开户行</div>
            <div class="col-xs-7  item-input">
              <input class="txt" type="text" name="bank" placeholder="请选择开户行" readonly="true" required="" ng-model="formData.bankText">
              <input type="hidden" ng-model="formData.bank">
            </div>
            <i class="item-arrow iconfont">&#xe614;</i>
          </div>
        </li>
        <li>
          <div class="item-content" ng-click="cityList()">
            <div class="col-xs-5 item-title">开户行所在省份</div>
            <div class="col-xs-7  item-input">
              <input class="txt" type="text" name="province" placeholder="请选择省份" readonly="true" required="" ng-model="formData.provinceText">
              <input type="hidden" ng-model="formData.province">
            </div>
            <i class="item-arrow iconfont">&#xe614;</i>
          </div>
        </li>
        <li>
          <div class="item-content" ng-click="cityList()">
            <div class="col-xs-5 item-title">开户行所在城市</div>
            <div class="col-xs-7  item-input">
              <input class="txt" type="text" name="city" placeholder="请选择城市" readonly="true" required="" ng-model="formData.cityText">
              <input type="hidden" ng-model="formData.city">
            </div>
            <i class="item-arrow iconfont">&#xe614;</i>
          </div>
        </li>        
        <li>
          <div class="item-content">
            <div class="col-xs-4 item-title ">开户行名称</div>
            <div class="col-xs-8 item-input">
              <input class="txt" type="text" name="bankname" placeholder="银行卡所在开户行的名称" required="" ng-model="formData.branch">
            </div>
          </div>
        </li>
        </ul>          
      </div>
      <div class="page-view">
        <div class="err" ng-class="{shake:validateForm.$errors.length > 0 || error} " ng-bind="validateForm.$errors[0] || errorWord"></div>
        <a class="btn btn-block btn-lg btn-blue"  w5c-form-submit="vm.bankaddForm()">绑定</a>
      </div>
    </form>
  </div>

  <!-- 选择城市列表 -->
  <div class="panel-overlay"></div>
  <div class="panel panel-city">
    <nav class="nav-top navbar-fixed-top">
      <a ng-click="cityListHide()" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span class="title">选择省市</span>
    </nav>
    <div class="page-content">
      <div class="city-tit">请选择开户行所在省市</div>
      <div class="city-con">
      <ul>
        <li ng-repeat="list in datalist">
          <span ng-bind="list.name" ng-click="selectProvince(list.id,list.name, $index)"></span>
          <dl ng-class="{slide:isSetid(list.id)}">
            <dd ng-repeat="clist in list.citylist" ng-bind="clist.name" ng-click="selectCity(clist.id, clist.name)"></dd>
          </dl>
        </li>
      </ul>
      </div>
    </div>  
  </div>

  <!-- 选择银行卡 -->
  <div class="panel panel-bank">
    <nav class="nav-top navbar-fixed-top">
      <a ng-click="bankListHide()" class="go-back"><span class="iconfont">&#xe604;</span></a>
      <span class="title">选择银行</span>
    </nav>
    <div class="page-content">
      <div class="bank-list">
        <ul>
          <li class="item-content" ng-repeat="bank in banklist" ng-click="selectBank(bank.bank_nid,bank.name)">
            <div class="item-media"><img ng-src="{{bank.url}}"></div>
            <div class="item-title" ng-bind="bank.name">中国银行</div>
          </li>
        </ul>
      </div>
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
          <a class="btn btn-lg" ng-click="bankSubmit()" ng-disabled="tenderBtn.status" ng-bind="tenderBtn.text"></a>
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