define(function(require, exports, module) {
    require('dyDirective');
    require('getUrl');
    function isEmpty(obj) {
        for (var name in obj) {
            return true;
        }
        return false;
    };
    var dyUser = angular.module('dyUser', ['dyDir','get.services','dylayer.m','w5c.validator']);

    //借款详情页
    dyUser.controller("loanContentCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
    	$scope.alert = function(msg){
    		var $layer = $injector.get('$dylayer');
            $layer.alert(msg,800);
    	}
        $scope.loading = false;
        $scope.islogin = "";
        //利息计算器带值操作
        $scope.counter = {
            account : "", //投资总额
            lilv :"", //借款利率
            times :"",//投资期限
            borrow_style :"" //还款方式
        }

        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $http({
            method: 'POST',
            url: "/wap/loan/loanInfo",
            data: $.param($scope.typeId), // pass in data as strings
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).success(function(_data) {
            if (_data.code != undefined && _data.code != '200') {
                return false;
            }
            $scope.member = _data.member; //投资用户信息
            $scope.loan_info = _data.loan_info; //借款信息
            $scope.member_info = _data.member_info; //借款人基础信息
            $scope.member_approve = _data.member_approve; //借款人认证信息
            $scope.member_loan_info = _data.member_loan_info; //借款人--借款信息
            $scope.repay_type = _data.repay_type; //借款还款类型
            $scope.tender_count = _data.tender_count;
            $scope.repay_plan = _data.repay_plan;
            $scope.tender_list = _data.tender_list;
            $scope.islogin = _data.is_login; //用户登录状态
            $scope.company_info = _data.company_info; //企业信息
            //利息计算器所用到的值
            var borrow_styles = ["","month","","end","endmonth","","endmonths"]
            $scope.counter = {
                lilv :parseFloat($scope.loan_info.apr), //借款利率
                times :parseInt($scope.loan_info.period_name),//投资期限
                borrow_style :borrow_styles[$scope.loan_info.repay_type] //还款方式
            }
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        diyou.use('common', function(app) {
            app.tabs();
            app.counter();
        });


      //查看协议范本 给app用的h5页面
        $scope.look = function(_sty){
        	var agreementId = $scope.loan_info.agreementId ;
            var url = "/wap/loan/loanProtocolAgreement?agreementId=" + agreementId;
            //var url = "/wap/loan/loanProtocol?type=" + agreementType;
//            url = "/wap/loan/loanContract";
            var $layer = $injector.get('$dylayer');
            if(_sty == "contract"){
                url = "/wap/loan/loanContract?type=tender";
            }

            $layer.page({
                type:2,
                //url:"/wap/index/xieyifanben"
                url: url
            });
        }

        $scope.$watchCollection("counter", function(newValue, oldValue, scope) {
            $scope.calcTool();
        });

        $scope.calcTool = function() {
            var borrowStyle = $scope.counter.borrow_style;
            switch (borrowStyle) {
                case "month":
                    $scope.accountInterest = $scope.getsMonth($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getsMonth(3);
                    break;
                /*case "season":
                    $scope.getSeason();
                    break;*/
                case "end":
                    $scope.accountInterest = $scope.getEnd($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEnd(3);
                    break;
                case "endmonth":
                    /*$scope.getEndMonth();*/
                    $scope.accountInterest = $scope.getEndMonth($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEndMonth(3);
                    break;
                case "endmonths":
                    $scope.accountInterest = $scope.getEndMonths($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEndMonths(3);
                    break;
                /*case "endday":
                    $scope.getEndDay();
                    break;
                case "months":
                    $scope.getMonths();
                    break;*/
                default:
                    break;
            }
        }

        //等额本息法
        $scope.getsMonth = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr,
                accountInterest = 0; //利息收益
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount, accountAll, interest, capital;
            repayAccount = accountAll = interest = capital = 0;
            //每个月还款额,还款总额,利息,还款本金
            var jQli = Math.pow((1 + monthApr), times);
            if (jQli > 1) {
                repayAccount = account * (monthApr * jQli) / (jQli - 1);
            } else {
                repayAccount = account;
            }
            for (var i = 0; i < times; i++) {
                if (jQli <= 1) {
                    interest = 0;
                } else if (i == 0) {
                    interest = account * monthApr;
                } else {
                    var jQlu = Math.pow((1 + monthApr), i);
                    interest = (account * monthApr - repayAccount) * jQlu + repayAccount;
                }
                capital = repayAccount - interest;
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
            }
            repayAccount = repayAccount.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (repayAccount * times).toFixed(2);
            accountInterest = (repayAccount * times - account).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
        }

        //到期还本还息
        $scope.getEnd = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = 0; //每个月还款额
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            interest = monthApr * times * account;
            repayAccount = interest + account;
            capital = account;
            Month += times;
            if (Month > 12) {
                if (!(Month % 12)) {
                    Year += parseInt(Month / 12) - 1;
                    Month = "12";
                } else {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }

            }
            var _Year = Year,
                _Month = Month,
                _Day = Day;
            if (Year <= 9) {
                _Year = "0" + parseInt(Year)
            }
            if (Month <= 9) {
                _Month = "0" + parseInt(Month)
            }
            if (Day <= 9) {
                _Day = "0" + parseInt(Day)
            }
            dateStr = _Year + "-" + _Month + "-" + _Day;
            tableTr += "<tr><td>" + 1 + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥0.00</td><td>" + dateStr + "</td></tr>";
            tableTr += "</table>";
            repayAccount = repayAccount.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (interest + account).toFixed(2);
            /*accountInterest = (repayAccount * times - account).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }*/
            if(isNaN(interest)){
                interest = 0;
            }
            return interest.toFixed(2);
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }

        //到期还本，按月付息
        $scope.getEndMonth = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = capital = yesAccount = 0;
            //每个月还款额,还款总额,利息,还款本金
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            capital = 0;
            interest = account * monthApr; //利息等于应还金额乘月利率
            for (var i = 0; i < times; i++) {
                if (i == times - 1) {
                    capital = account;
                }
                repayAccount = interest + capital;
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
                dateStr = _Year + "-" + _Month + "-" + _Day;
                tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (account - capital).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
            }
            tableTr += "</table>";
            repayAccount = interest.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (account + interest * times).toFixed(2);
            accountInterest = (interest * times).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }

        //到期还本，按月付息,且当月还息
        $scope.getEndMonths = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = capital = repayNext = repayInt = 0;
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            capital = 0;
            interest = account * monthApr;
            repayAccount = interest + capital;
            repayInt = interest;
            repayNext = repayAccount;
            for (var i = 0; i < times + 1; i++) {
                if (i == times) {
                    repayAccount = capital = account;
                    interest = 0;
                }
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
                dateStr = _Year + "-" + _Month + "-" + _Day;
                tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (account - capital).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
            }
            tableTr += "</table>";
            repayAccount = repayNext.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (account + repayInt * times).toFixed(2);
            accountInterest = (repayInt * times).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }
        $scope.tender = function(){
        	postUrl.events('/wap/member/checkTrustAccount?riskFlag=1').success(function(_data) {
             	 _data = _data.data;
             	 var $layer = $injector.get('$dylayer');
             	 if(_data == undefined) {
             	 	$layer.alert("请先登录", 1000,function() {
						window.location.href = "/wap/system/reglogin";
					});
				 } else if (_data == 2) {
				 	$layer.alert("请先实名认证",1000,function(){window.location.href = "/wap/member/approverealname";});
				 } else if (_data == 3) {
				 	$layer.alert("请先进行邮箱认证",1000,function(){window.location.href = "/wap/member/checkEmail";});
				 } else if (_data == 4) {
				 	$layer.alert("请先绑定手机号",1000,function(){window.location.href = "/wap/member/checkPhone";});
             	 }else if(_data == 5){
                	$layer.confirm({
                        msg:"请先进行风险评测",
                        btn:["风险测评","取消"],
                        yes:function(){
                        	 window.location.href = "/wap/answer/record";
                        }
                    });
            	}
             	/* else if(_data == 6){
                	$layer.confirm({
                        msg:"已到期，请重新进行风险评测",
                        btn:["风险测评","取消"],
                        yes:function(){
                        	 window.location.href = "/wap/answer/record";
                        }
                    });
            	} */
				 else if (_data != 1) {
					$layer.confirm({
						msg : "请先开通资金托管",
						btn : ["马上开通", "暂不开通"],
						yes : function() {
							postUrl.events('/wap/system/isApprove').success(function(_data) {
								var ori_data = _data;
                             	_data = _data.data;
                             	 if(ori_data.description==-1){
                             	 	 $layer.alert("请先登录", 1000,function() {
										window.location.href = "/wap/system/reglogin";
									});
                             	 }else if(_data.is_realname!=1&&_data.realname_status!=-2){
        	                         $layer.alert("请先实名认证",1000,function(){
        	                             window.location.href = "/wap/member/approverealname";
        	                         });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else if(_data.is_trust!=1){
				                	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
				                         if(_data.status=="200"){
				                             $(_data.description.form).appendTo($("body"));
				                         }else{
				                         	$layer.alert(_data.description,1000);
				                         }
				                     });
				                 }
							});
						}
					});
				}else{
					window.location = "/wap/loan/tenderLoanview#?id="+$scope.typeId.id;
				}
             });
        }
    });

    //借款详情页
    dyUser.controller("transferViewCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        $scope.islogin = "";
        //利息计算器带值操作
        $scope.counter = {
            account : "", //投资总额
            lilv :"", //借款利率
            times :"",//投资期限
            borrow_style :"" //还款方式
        }

        postUrl.events('/wap/transfer/transferInfo',$scope.typeId).success(function(_data) {

            if (_data.status != undefined && _data.status != '200') {

                return false;
            }
            $scope.member = _data.member; //投资用户信息
            $scope.loan_info = _data.loan_info; //借款信息
            $scope.member_info = _data.member_info; //借款人基础信息
            $scope.member_approve = _data.member_approve; //借款人认证信息
            $scope.member_loan_info = _data.member_loan_info; //借款人--借款信息
            $scope.repay_type = _data.repay_type; //借款还款类型
            $scope.tender_count = _data.tender_count;
            $scope.repay_plan = _data.repay_plan;
            $scope.tender_list = _data.tender_list;
            $scope.islogin = _data.is_login; //用户登录状态
            $scope.transfer_ret = _data.transfer_ret; //债权信息
            $scope.isSelf=_data.is_self;
            $scope.company_info = _data.company_info; //企业信息
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        diyou.use('common', function(app) {
            app.tabs();
            app.counter();
        });


      //查看协议范本 给app用的h5页面
        $scope.look = function(_sty){
        	var agreementType = $scope.loan_info.agreementId;
            var url = "/wap/loan/loanProtocolAgreement?agreementId=" + agreementType;
//            url = "/wap/loan/loanContract";
            var $layer = $injector.get('$dylayer');
            if(_sty == "contract"){
                url = "/wap/loan/loanContract";
            }

            $layer.page({
                type:2,
                //url:"/wap/index/xieyifanben"
                url: url
            });
        }

        $scope.$watchCollection("counter", function(newValue, oldValue, scope) {
            //console.log($scope.counter.borrow_style);
            $scope.calcTool();
        });

        $scope.calcTool = function() {
            var borrowStyle = $scope.counter.borrow_style;
            switch (borrowStyle) {
                case "month":
                    $scope.accountInterest = $scope.getsMonth($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getsMonth(3);
                    break;
                /*case "season":
                    $scope.getSeason();
                    break;*/
                case "end":
                    $scope.accountInterest = $scope.getEnd($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEnd(3);
                    break;
                case "endmonth":
                    /*$scope.getEndMonth();*/
                    $scope.accountInterest = $scope.getEndMonth($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEndMonth(3);
                    break;
                case "endmonths":
                    $scope.accountInterest = $scope.getEndMonths($scope.counter.lilv);
                    $scope.bankAccountInterest = $scope.getEndMonths(3);
                    break;
                /*case "endday":
                    $scope.getEndDay();
                    break;
                case "months":
                    $scope.getMonths();
                    break;*/
                default:
                    break;
            }
        }

        //等额本息法
        $scope.getsMonth = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr,
                accountInterest = 0; //利息收益
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount, accountAll, interest, capital;
            repayAccount = accountAll = interest = capital = 0;
            //每个月还款额,还款总额,利息,还款本金
            var jQli = Math.pow((1 + monthApr), times);
            if (jQli > 1) {
                repayAccount = account * (monthApr * jQli) / (jQli - 1);
            } else {
                repayAccount = account;
            }
            for (var i = 0; i < times; i++) {
                if (jQli <= 1) {
                    interest = 0;
                } else if (i == 0) {
                    interest = account * monthApr;
                } else {
                    var jQlu = Math.pow((1 + monthApr), i);
                    interest = (account * monthApr - repayAccount) * jQlu + repayAccount;
                }
                capital = repayAccount - interest;
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
            }
            repayAccount = repayAccount.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (repayAccount * times).toFixed(2);
            accountInterest = (repayAccount * times - account).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
        }

        //到期还本还息
        $scope.getEnd = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = 0; //每个月还款额
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            interest = monthApr * times * account;
            repayAccount = interest + account;
            capital = account;
            Month += times;
            if (Month > 12) {
                if (!(Month % 12)) {
                    Year += parseInt(Month / 12) - 1;
                    Month = "12";
                } else {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }

            }
            var _Year = Year,
                _Month = Month,
                _Day = Day;
            if (Year <= 9) {
                _Year = "0" + parseInt(Year)
            }
            if (Month <= 9) {
                _Month = "0" + parseInt(Month)
            }
            if (Day <= 9) {
                _Day = "0" + parseInt(Day)
            }
            dateStr = _Year + "-" + _Month + "-" + _Day;
            tableTr += "<tr><td>" + 1 + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥0.00</td><td>" + dateStr + "</td></tr>";
            tableTr += "</table>";
            repayAccount = repayAccount.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (interest + account).toFixed(2);
            /*accountInterest = (repayAccount * times - account).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }*/
            if(isNaN(interest)){
                interest = 0;
            }
            return interest.toFixed(2);
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }

        //到期还本，按月付息
        $scope.getEndMonth = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = capital = yesAccount = 0;
            //每个月还款额,还款总额,利息,还款本金
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            capital = 0;
            interest = account * monthApr; //利息等于应还金额乘月利率
            for (var i = 0; i < times; i++) {
                if (i == times - 1) {
                    capital = account;
                }
                repayAccount = interest + capital;
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
                dateStr = _Year + "-" + _Month + "-" + _Day;
                tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (account - capital).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
            }
            tableTr += "</table>";
            repayAccount = interest.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (account + interest * times).toFixed(2);
            accountInterest = (interest * times).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }

        //到期还本，按月付息,且当月还息
        $scope.getEndMonths = function(lilv) {
            var accountVal = $scope.counter.account;
            var lilvVal = lilv; //网站利率
            var timesVal = $scope.counter.times;
            var account = parseFloat(accountVal); //借款金额
            var yearApr = parseFloat(lilvVal);
            var monthAprShow = yearApr / 12;
            var monthApr = monthAprShow / 100; //月利率
            var times = parseFloat(timesVal); //借款期限
            var todayDate = new Date(),
                dateStr;
            var Year = todayDate.getFullYear(),
                Month = todayDate.getMonth() + 1,
                Day = todayDate.getDate(); //还款时间
            var repayAccount = accountAll = interest = capital = repayNext = repayInt = 0;
            var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
            capital = 0;
            interest = account * monthApr;
            repayAccount = interest + capital;
            repayInt = interest;
            repayNext = repayAccount;
            for (var i = 0; i < times + 1; i++) {
                if (i == times) {
                    repayAccount = capital = account;
                    interest = 0;
                }
                Month++;
                if (Month > 12) {
                    Year += parseInt(Month / 12);
                    Month = parseInt(Month % 12);
                }
                var _Year = Year,
                    _Month = Month,
                    _Day = Day;
                if (Year <= 9) {
                    _Year = "0" + parseInt(Year)
                }
                if (Month <= 9) {
                    _Month = "0" + parseInt(Month)
                }
                if (Day <= 9) {
                    _Day = "0" + parseInt(Day)
                }
                dateStr = _Year + "-" + _Month + "-" + _Day;
                tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (account - capital).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
            }
            tableTr += "</table>";
            repayAccount = repayNext.toFixed(2);
            monthAprShow = monthAprShow.toFixed(2);
            accountAll = (account + repayInt * times).toFixed(2);
            accountInterest = (repayInt * times).toFixed(2)
            if(isNaN(accountInterest)){
                accountInterest = 0;
            }
            return accountInterest;
            /*$('#tableTr').html(tableTr);
            $("#monthAcc").html(repayAccount);
            $("#monthAp").html(monthAprShow);
            $("#monthIn").html(accountAll);*/
        }

        $scope.buyTransfer=function(typeId){
        	postUrl.events('/wap/system/isApprove').success(function(_data) {
            	var $layer = $injector.get('$dylayer');
            	if(_data.description == -1){
      			  $layer.alert("请先登录",1000,function(){
                        window.location.href = "/wap/system/reglogin";
                    });
            	}
            	if(_data.description == 1){
      			  $layer.alert("托管账号未授权，请先进行授权操作",1000,function(){
                        window.location.href = "/wap/trust/myTrust";
                    });
            	}
           	 	_data = _data.data;
               if (_data.is_trust == 1&&_data.is_phone != 1) {
                    $layer.alert("请先绑定手机号", 1000, function() {
                        window.location.href = "/wap/member/checkPhone";
                    });
                } else if (_data.is_trust != 1) {
                    $layer.confirm({
                        msg: "请先开通资金托管",
                        btn: ["马上开通", "暂不开通"],
                        yes: function() {
                            if (_data.is_realname != 1 && _data.realname_status != -2) {
                                $layer.alert("请先实名认证", 1000, function() {
                                    window.location.href = "/wap/member/approverealname";
                                });
                            } else if (_data.is_phone != 1) {
                                $layer.alert("请先绑定手机号", 1000, function() {
                                    window.location.href = "/wap/member/checkPhone";
                                });
                            } else {
                                postUrl.events('/wap/trust/register', {
                                    "type": "data"
                                }).success(function(_data) {
                                    if (_data.status == "200") {
                                        $(_data.description.form).appendTo($("body"));
                                    } else {
                                        $layer.alert(_data.description, 1000);
                                    }
                                });
                            }
                        }
                    });
                }else{
              	  var $layer = $injector.get('$dylayer');
                  if ($scope.isSelf == 1) {
                      $layer.alert("不能购买自己的债务！", 800);
                      return false;
                  }
                  if ($scope.isSelf == 2) {
                      $layer.alert("不能购买自己的债权！", 800);
                      return false;
                  }
                  window.location.href = "/wap/transfer/transferinvest#?id=" + typeId;
             }
        });
        }
    });

    //我的投资
    dyUser.controller("mytenderCtrl", function($scope, $http, $location,postUrl,$timeout) {
        //$scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/member/myTenderData').success(function(_data) {
        	_data = _data.data ;
            $scope.mytenderList = _data;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/member/myTenderData',$scope.listData).success(function(_data) {
            	_data = _data.data ;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.mytenderList.items = $scope.mytenderList.items.concat(_data.items);
                }
            });
        }
    })

    //我的债权 - 列表
    dyUser.controller("mytransferCtrl", function($scope, $http, $location,postUrl,$timeout) {
        //$scope.typeId = isEmpty($location.search()) ? $location.search() : '';

        //债权盈亏
        postUrl.events('/wap/transfer/mytransfer').success(function(_data) {
        	_data = _data.data;
            $scope.transferinfo = _data;
        });

        //转让记录
        $scope.loading = false;
        postUrl.events('/wap/transfer/mytransferList',{status:'transfer'}).success(function(_data) {
        	_data = _data.data;
            $scope.transfer_list = _data;
            if(_data.total_items==_data.items.length){
            	$scope.getMoreStatus = false;
            }
            $timeout(function() {
                $scope.loading = true;
            },300);

        });

        //点击加载更多
        $scope.listData = {page:1,status:'transfer'};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/transfer/mytransferList',$scope.listData).success(function(_data) {
            	_data = _data.data;
            	if(_data.total_items==_data.items.length){
                	$scope.getMoreStatus = false;
                }
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.transfer_list.items = $scope.transfer_list.items.concat(_data.items);
                }
            });
        }



        //购买记录
        $scope.getRecord = function(){
            postUrl.events('/wap/transfer/mytransferList',{status:'buy'}).success(function(_data) {
            	_data = _data.data;
                $scope.record_list = _data;
            });
        }
        //点击加载更多
        $scope.listData2 = {page:1,status:'buy'};
        $scope.getMoreStatus2 = true;
        $scope.getMoreList2 = function(){
            $scope.listData2.page++;
            postUrl.events('/wap/transfer/mytransferList',$scope.listData2).success(function(_data) {
            	_data = _data.data;
                if(_data == null || _data.items.length==0){
                    $scope.getMoreStatus2 = false;
                }else{
                    $scope.record_list.items = $scope.record_list.items.concat(_data.items);
                }
            });
        }


        diyou.use('common', function(app) {
            app.tabs();
        });
    })

    //我的债权 - 详情
    dyUser.controller("mytransferViewCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

        $scope.loading = false;
        $scope.formData = {};
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        //注入弹窗服务
        var $layer = $injector.get('$dylayer');
        var timer;
        var transfer_fee = 0;
        postUrl.events('/wap/transfer/myTransferDetail',$scope.typeId).success(function(_data) {
        	_data = _data.data ;
            $scope.transferDetail = _data;
            $scope.formData.amount = _data.coefficient;
            $scope.transferfee=parseFloat(_data.amount)*parseFloat(_data.transfer_fee);
            transfer_fee = _data.transfer_fee ;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });


        //更改系数
        $scope.getAmount = function(){
            $scope.errorWord = '';
            var val = parseInt($scope.formData.coefficient);

            if(isNaN(val)){
                $scope.formData.coefficient = "";
                return false;
            }
            $scope.typeId['coefficient'] = val;
            if(timer){
                $timeout.cancel(timer);
            }
            timer = $timeout(function(){
            	var coefficient = val;
            	$scope.transferDetail.amount = $scope.transferDetail.amount_money * coefficient * 0.01;
            	$scope.transferfee=parseFloat($scope.transferDetail.amount)*parseFloat(transfer_fee);
            },300)



        }
        //立即转让
        $scope.tenderBtn = {};
        $scope.tenderBtn.text = "确定";
        $scope.transferChange = function(){
            $scope.errorWord = '';
            var val = parseInt($scope.formData.coefficient);
            if(isNaN(val)){
                $scope.formData.coefficient = "";
                //$scope.errorWord = '转让系数为80%~99%之间';
                $layer.alert("转让系数为"+$scope.transferDetail.transfer_coefficient_min+"%~"+$scope.transferDetail.transfer_coefficient_max+"%之间", 1000);
                return false;
            }
            if(val>$scope.transferDetail.transfer_coefficient_max || val <$scope.transferDetail.transfer_coefficient_min ){
            	 $layer.alert("转让系数为"+$scope.transferDetail.transfer_coefficient_min+"%~"+$scope.transferDetail.transfer_coefficient_max+"%之间", 1000);
                return false;
            }
            $scope.typeId['coefficient'] = val;
            $scope.transferSubmit();
        }
        $scope.transferSubmit = function(){
            $scope.tenderBtn.text = "转让中...";
            $scope.tenderBtn.status = true;
            $scope.formData['tender_id'] = $scope.typeId['id'];

            //错误提示位置
            $scope.errorPlace = "";
            postUrl.events('/wap/transfer/transfersub',$scope.formData).success(function(_data) {
               if(_data.code == "200"){

                  $layer.alert("转让成功",800,function(){
                      window.location.href = "/wap/transfer/mytransfer";
                  });
               }else{
                    //$scope.errorPlace = _data.description;
                    $layer.alert(_data.description,800);
               }
               $scope.tenderBtn.text = "确定";
               $scope.tenderBtn.status = false;
            });

        }

        //撤销转让
        $scope.transferCancel = function(id){
        //注入弹窗服务
            postUrl.events('/wap/transfer/cancelTransfer',{id:id}).success(function(_data) {
                    var trans_num=parseInt(_data.data.cancelCount);
                    if(isNaN(trans_num)){
                       trans_num=0;
                    }
                    $layer.confirm({
                    	msg:"是否确认撤销债权转让,债权转让撤销3次后将不可再进行转让,当前已撤销"+trans_num+"次",
                        yes:function(){

                              postUrl.events('/wap/transfer/cancel',{id:id}).success(function(_data) {
                                if(_data.code==200){
                                    window.location.href = "/wap/transfer/mytransfer";
                                }else{
                                	$layer.alert(_data.description+"债权转让撤销3次后将不可再进行转让，当前已撤销3次",800);
                                    return false;
                                }
                            });
                        }
                    });
            });

         }

    })
    //我的债权 - 购买详情
    dyUser.controller("mytransferInfoCtrl", function($scope, $http, $location,postUrl,$timeout) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/transfer/mytransferInfo',$scope.typeId).success(function(_data) {
        	_data = _data.data ;
            $scope.transferInfo = _data;
            $scope.recoverInfo = _data.recover.items;

//            $scope.transferInfo['expire_time'] = _data.repay_info.expire_time;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });


    })
    //债权转让协议
    dyUser.controller("mytransferAgreement", function($scope, $http,$injector, $location,postUrl,$timeout) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/transfer/transferAgreement',$scope.typeId).success(function(_data) {
            $scope.transferInfo = _data.data.transfer_info;
            $scope.agreement = _data.data.agreement;
            $scope.loan = _data.data.loan;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });
    })
    //交易记录
    dyUser.controller("accountLogCtrl", function($scope, $http, $location,postUrl,$timeout) {
        //$scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/account/accountLog').success(function(_data) {
        	_data = _data.data ;
            $scope.account = _data;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/account/accountLog',$scope.listData).success(function(_data) {
            	_data = _data.data ;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
               }else{
                    $scope.account.items = $scope.account.items.concat(_data.items);
                }
            });
        }
    })


    //充值记录
    dyUser.controller("rechargRecordCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        postUrl.events("/wap/recharge/rechargelog",{type:'data'}).success(function(_data) {
            $scope.cashRecord = _data.data;
            console.log($scope.cashRecord);
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/recharge/rechargelog',$scope.listData).success(function(_data) {
                if(_data.data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.cashRecord.items = $scope.cashRecord.items.concat(_data.items);
                }
            });
        }

    })


    //投标
  dyUser.controller("tenderloanCtrl", function($scope, $http, $location,postUrl,$injector,$timeout) {
        //计算收益需要提交的参数
        $scope.getAccount = {
            "amount":"",  //投资金额
            "period" : "", //借款期限
            "apr" : "",  //利率
            "repay_type" : "", //还款方式
            "award_scale":"", //奖金率
            "loan_id":"" //标id
        }

        //判断手机号是否绑定，支付密码是否设置
        $scope.judgeTender = {"isphone":"","ispaypassword":""};
        //loading加载
        $scope.loading = false;
        //注入弹窗服务
        var $layer = $injector.get('$dylayer');
        var amount_low = "", //起投金额
            tender_amount_max = "" ,//投资上限
            can_amount = "", //可投金额
            balance_amount = "";//账户余额

        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        postUrl.events('/wap/loan/loansimpleinfo',$scope.typeId).success(function(_data) {
    	    _data = _data.data;
            $scope.loansimpleinfo = _data;
            if($scope.loansimpleinfo.tender_amount_min > $scope.loansimpleinfo.wait_amount){
            	 $scope.$watch("loansimpleinfo.wait_amount",function(newValue, oldValue, scope){
     	            $scope.getAccount.amount = $scope.loansimpleinfo.wait_amount;
     	            if(($scope.loansimpleinfo.wait_amount!="") && !isNaN($scope.loansimpleinfo.wait_amount) ){
     	                postUrl.events('/wap/loan/investinterest',$scope.getAccount).success(function(_data) {
     	                    $scope.getaccount = _data.data;
     	                });
     	            }else{
     	                $scope.getaccount.interest_total = 0;
     	                $scope.getaccount.award_amount = 0;
     	                $scope.getaccount.interest_award = 0;
     	                $scope.getaccount.additional_amount = 0 ;
     	            }
     	        });
            }
             //定义红包选择后的值
            $scope.bountyData = {};
            $scope.SelectBounty = function(cid, value, type) {
                $scope.counter.redbag = cid;
                $scope.bountyData.Amount = value;
                $scope.bountyData.bountyType = type;
            }

            //请求红包列表需要传递的参数
            $scope.getBounty = {
                    amount: '',
                    loanId: $scope.typeId.id
                }
            //请求红包列表
            $scope.contactsShow = function() {
                if ($scope.counter.amount != ''&& $scope.loansimpleinfo.category_type != 3) {
                    $scope.getBounty.amount = $scope.counter.amount;

                    postUrl.events('/wap/bounty/useableList', $scope.getBounty).success(function(_data) {
                        $scope.loanbounty = _data.data.redbag;
                    });
                    $('body').addClass('hotline-cover');
                } else if ($scope.countval != ''&& $scope.loansimpleinfo.category_type == 3) {
                    $scope.getBounty.amount = $scope.loansimpleinfo.roam_info.amount * parseInt($scope.countval);
                    postUrl.events('/wap/bounty/useableList', $scope.getBounty).success(function(_data) {
                        $scope.loanbounty = _data.data.redbag;
                    });
                    $('body').addClass('hotline-cover');

                } else {
                    $layer.alert("请输投资金额不能为空！", 1000);
                }

            }

            //点击确定后，隐藏红包列表，并将参数传递
            $scope.cBounty = function() {
               /* if($scope.loanbounty!=""){
                    $scope.counter.cid = $scope.bountyData.cid;
                    $scope.counter.redbag = $scope.bountyData.cid;
                    $scope.counter.bounty = $scope.bountyData.onAmount;
                }*/
                 $('body').removeClass('hotline-cover');
            }
            amount_low = _data.tender_amount_min;
            tender_amount_max = parseInt(_data.tender_amount_max);
            can_amount = _data.can_amount;
            low_amount = _data.low_amount;
            balance_amount = parseFloat(_data.balance_amount);
            $scope.getAccount.period = _data.period; //借款期限
            $scope.getAccount.apr = _data.apr; //利率
            $scope.getAccount.repay_type = _data.repay_type; //还款方式
            $scope.getAccount.award_scale = _data.award_proportion; //奖金率
            $scope.getAccount.additional_status = _data.additional_status ;
            $scope.getAccount.additional_apr = _data.additional_apr ;
            $scope.getAccount.loan_id = _data.loan_id ;
            $scope.judgeTender.isphone = _data.is_phone;
            $scope.judgeTender.ispaypassword = _data.is_paypassword;

            //初始化流转标
            $timeout(function() {
              $scope.loading = true;
            },300);
	        var category_type=$scope.loansimpleinfo.category_type;

	        if(category_type==3){
	            var t_num=$scope.loansimpleinfo.roam_info.portion_total-$scope.loansimpleinfo.roam_info.portion_yes
		        if(t_num==0){
		           $scope.countval="0份";
		        }
	         	$scope.countval="1份";
		        $scope.$watch("countval",function(newValue, oldValue, scope){
                    $scope.bountyData.Amount = 0;
                    $scope.bountyData.bountyType = '';
                    $scope.counter.redbag = '';
		        	var val=parseInt($scope.countval);
		            var loanAmount=parseInt($scope.loansimpleinfo.roam_info.amount);
		            $scope.getAccount.amount=val*loanAmount;
		            if((val!="") && !isNaN(val)){
		                postUrl.events('/wap/loan/investinterest',$scope.getAccount).success(function(_data) {
		                    $scope.getaccount = _data.data;
		                });
		            }else{
		                $scope.getaccount.interest_total = 0;
		                $scope.getaccount.award_amount = 0;
		                $scope.getaccount.interest_award = 0;
		                $scope.getaccount.additional_amount = 0 ;
		            }
		        });
	        }

	        $scope.counter = angular.extend({"amount":""},$scope.typeId);
	        diyou.use('common', function(fn){
	            fn.tenderLoan();
	        });

        	$scope.tenderNow = function(){
        		$scope.counter.paypassword="";
	            if(category_type==3){
	            	var val=parseInt($scope.countval);
	                var loanAmount=parseInt($scope.loansimpleinfo.roam_info.amount);
	                $scope.counter.amount=val*loanAmount;
	                $scope.counter.number = val;
	            }
	            postUrl.events('/wap/system/isApprove').success(function(_data) {
	            	 _data = _data.data;
	            	 if(_data.is_email!=1){
	            		 $layer.alert("请先进行邮箱认证",1000,function(){
 	                        window.location.href = "/wap/member/checkEmail";
 	                    });
	            	 }else if(_data.is_phone!=1){
	            		 $layer.alert("请先绑定手机号",1000,function(){
 	                        window.location.href = "/wap/member/checkPhone";
 	                    });
	            	 }else if(_data.is_trust!=1){
	                    $layer.confirm({
	                        msg:"请先开通资金托管",
	                        btn:["马上开通","暂不开通"],
	                        yes:function(){
	                        	if(_data.is_realname!=1&&_data.realname_status!=-2){
	        		                $layer.alert("请先实名认证",1000,function(){
	        		                    window.location.href = "/wap/member/approverealname";
	        		                });
	        	                 }else if(_data.is_email!=1){
	        	                    $layer.alert("请先进行邮箱认证",1000,function(){
	        	                        window.location.href = "/wap/member/checkEmail";
	        	                    });
	        	                 }else if(_data.is_phone!=1){
	        	                    $layer.alert("请先绑定手机号",1000,function(){
	        	                        window.location.href = "/wap/member/checkPhone";
	        	                    });
	        	                }else{
		                        	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
	                                     if(_data.status=="200"){
	                                         $(_data.description.form).appendTo($("body"));
	                                     }else{
	                                     	$layer.alert(_data.description,1000);
	                                     }
	                                 });
	        	                }
	                        }
	                    });
	                }else{
                	 if($scope.loansimpleinfo.tender_amount_min > $scope.loansimpleinfo.wait_amount){
                     	$scope.counter.amount = $scope.loansimpleinfo.wait_amount;
                     }
	               	 if($scope.counter.amount==""){
	                        $layer.alert("请输入投资金额",1000);
	                    }else if(isNaN($scope.counter.amount)){
	                        $layer.alert("投资金额格式有误",1000);
	                    }else if($scope.counter.amount % 10!=0){
	                        $layer.alert("投资金额必须为10的倍数",1000);
	                    }else if($scope.loansimpleinfo.tender_amount_min < $scope.loansimpleinfo.wait_amount && $scope.counter.amount < amount_low){
	                        $layer.alert("投资金额必须大于等于起投金额",1000);
	                    }else if($scope.counter.amount > tender_amount_max &&tender_amount_max!=0){
	                        $layer.alert("投资金额不能大于投资上限",1000);
	                    }else if($scope.counter.amount > low_amount){
	                        $layer.alert("投资金额不能大于最大可投金额",1000);
	                    }else if($scope.counter.amount > balance_amount){
	                        $layer.alert("账户余额不足，请到PC端进行充值",1000);
	                    }
	                    else{
	                    	var tenderUrl = "/wap/trust/tender";
	                        if(category_type==3){
	                        	tenderUrl = "/wap/trust/tenderRoam";
	          	            }
	                    	 postUrl.events(tenderUrl,$scope.counter).success(function(_data) {
	  			               if(_data.status == "200"){
	  			                    $(_data.description.form).appendTo($("body"));
	  			               }else{
	  			              	    $layer.alert(_data.description,1000);
	  			               }
	  			       		});
	                    }
	                }
	            });
        	}

	        $scope.tenderBtn = {
	            "text":"确定",
	            "status":false
	        };
	        $scope.submitNow = false;
	        $scope.tennderSubmit = function(){
	            $scope.tenderBtn.text = "投资中...";
	            $scope.tenderBtn.status = true;
	            //错误提示位置
	            $scope.errorPlace = "";

	        }


	        $scope.getaccount = {
	            "interest_total":0,
	            "award_amount":0,
	            "interest_award":0,
	            "additional_amount":0
	        };

	        //计算收益
	        $scope.$watch("counter.amount",function(newValue, oldValue, scope){
                $scope.bountyData.Amount = 0;
                $scope.bountyData.bountyType = '';
                $scope.counter.redbag = '';
	            $scope.getAccount.amount = $scope.counter.amount;
	            if(($scope.counter.amount!="") && !isNaN($scope.counter.amount) && ($scope.counter.amount>=10)){
	                postUrl.events('/wap/loan/investinterest',$scope.getAccount).success(function(_data) {
	                    $scope.getaccount = _data.data;
	                });
	            }else{
	                $scope.getaccount.interest_total = 0;
	                $scope.getaccount.award_amount = 0;
	                $scope.getaccount.interest_award = 0;
	                $scope.getaccount.additional_amount = 0 ;
	            }
	        });

       });
	   $scope.subtract=function(val){
		      var val=parseInt(val);
		      var t_num=$scope.loansimpleinfo.roam_info.portion_total-$scope.loansimpleinfo.roam_info.portion_yes
		      if(t_num>=1){
		      if(val>1){
		        val--;
		      }else{
		        $scope.countval="1份";
		      }
		      $scope.countval=val+"份";
		    }else{
		      $scope.countval="0份";
		      return false;
		    }
	   }
		$scope.add=function(val){
		      var val=parseInt(val);
		      var t_num=$scope.loansimpleinfo.roam_info.portion_total-$scope.loansimpleinfo.roam_info.portion_yes;
		      val++;
		      if(val>t_num){
		          $scope.countval=t_num+"份";
		      }else{
		          $scope.countval=val+"份";
		      }
		}
    });
    //债权购买
    dyUser.controller("transferinvestCtrl", function($scope, $http, $location,postUrl,$injector,$timeout) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.counter = angular.extend($scope.typeId);
        $scope.loading = false;
        var $layer = $injector.get('$dylayer');
        postUrl.events('/wap/transfer/transferinvest',$scope.typeId).success(function(_data) {
        	_data = _data.data ;
        	if(_data.description.is_trust != 1){
        		$layer.confirm({
                    msg:"请先开通资金托管",
                    btn:["马上开通","暂不开通"],
                    yes:function(){
                    	if(_data.description.is_realname!=1&&_data.description.realname_status!=-2){
    		                $layer.alert("请先实名认证",1000,function(){
    		                    window.location.href = "/wap/member/approverealname";
    		                });
    	                 }else if(_data.description.is_phone != "1"){
                            $layer.alert("请先进行手机认证",1000,function(){
                                window.location.href = "/wap/member/checkPhone";
                            });
                        }else if(_data.description.is_email!="1"){
                            $layer.alert("请先进行邮箱认证",1000,function(){
                                window.location.href = "/wap/member/checkEmail";
                            });
                        }else{
                        	postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                if(_data.status=="200"){
                                    $(_data.description.form).appendTo($("body"));
                                }else{
                                	$layer.alert(_data.description,1000);
                                }
                            });
                        }
                    }
                });
        	}else{
        		if(_data.description.is_realname!=1&&_data.description.realname_status!=-2){
                	$layer.alert("请先实名认证",1000,function(){window.location.href = "/wap/member/approverealname";});
                }else if(_data.description.is_phone != "1"){
                	$layer.alert("请先进行手机认证",1000,function(){window.location.href = "/wap/member/checkPhone";});
                }else if(_data.description.is_email!="1"){
                	$layer.alert("请先进行邮箱认证",1000,function(){window.location.href = "/wap/member/checkEmail";});
                }else {
	        		$scope.transfer=_data;
	                $timeout(function() {
	                    $scope.loading = true;
	                },300);
                }
        	}
        });

        $scope.transferHid = function(){
            $('body').removeClass('cover');
        }

        //立即购买 - 提交
        $scope.tenderBtn = {
            "text":"确定",
            "status":false
        };
        $scope.submitNow = false;
        $scope.transferNow = function(){
            //错误提示位置
            $scope.errorPlace = "";
             postUrl.events('/wap/trust/buyTransfer',$scope.counter).success(function(_data) {
              if(_data.status == "200"){
                   $(_data.description.form).appendTo($("body"));
               }else{
	               	$layer.alert(_data.description,1000);
               }
       		});
        };
    })

      //add by panxh 风险测评结果
     dyUser.controller('resultAnswerCtrl', function($scope, $http, $location,postUrl, $injector, $timeout) {
        //注入弹窗服务
        var $layer = $injector.get('$dylayer');
        $scope.url = '/wap/answer/resultData';
        postUrl.events($scope.url).success(function(_data) {
        	if(_data.status == '230'){
        		$layer.confirm({
                    msg: _data.description,
                    btn:["确定","取消"],
                    yes:function(){
                    	 window.location.href = "/wap/answer/record";
                    }
                });
        	} else if (_data.status == '200'){
        		$scope.level = _data.data.level;
        		if( _data.data.isExpire == '1'){
        			$layer.confirm({
                        msg: "已到期，请重新进行风险评测",
                        btn:["确定","取消"],
                        yes:function(){
                        	 window.location.href = "/wap/answer/record";
                        }, no: function(){
                        	dyClose: true;
         		        }
                    });
        		}
        	}
        });
    });
   //end by panxh 风险测评结果

    //用户中心-首页
    dyUser.controller("userindexCtrl", function($scope, $http, $location,postUrl, $injector) {
        $scope.loading = false;
        var $layer = $injector.get('$dylayer');
        postUrl.events('/wap/account/messageCount').success(function(_data) {
            $scope.message=_data.msgNum;
    });
        postUrl.events('/wap/member/accountData').success(function(_data) {
        	_data = _data.data ;
                $scope.interest_award=_data.interest_award; //预估累计收益
                $scope.total_amount=_data.total_amount; //总资产
                $scope.balance_amount=_data.balance_amount;//可用余额
                $scope.accountData = _data;
                $scope.loading = true;
        });
        $scope.OpenTrust = function(){
          postUrl.events('/wap/member/checkTrustAccount').success(function(_data) {
                _data = _data.data;
                if(_data == 0){
                    var $layer = $injector.get('$dylayer');
                    $layer.confirm({
                        msg:"请先开通资金托管",
                        btn:["马上开通","暂不开通"],
                        yes:function(){
                             postUrl.events('/wap/system/isApprove').success(function(_data) {
                                _data = _data.data;
                                 if(_data.is_realname!=1&&_data.realname_status!=-2){
                                     $layer.alert("请先实名认证",1000,function(){
                                         window.location.href = "/wap/member/approverealname";
                                     });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else{
                                     postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                         if(_data.status=="200"){
                                             $(_data.description.form).appendTo($("body"));
                                         }else{
                                            $layer.alert(_data.description,1000);
                                         }
                                     });
                                 }
                             });
                        }
                    });
                }else{
                    window.location = "/wap/member/index";
                }
            });
        }

        $scope.risk = function(){
        	postUrl.events('/wap/answer/isExpire').success(function(_data) {
        		if (_data.status == '230'){
         			$layer.confirm({
                         msg: "已到期，请重新进行风险评测",
                         btn:["确定","取消"],
                         yes:function(){
                         	 window.location.href = "/wap/answer/record";
                         }, no: function(){
                         	dyClose: true;
          		         }
                     });
             	}else if(_data.status == '220'){
             		$layer.confirm({
                        msg:"请先进行风险评测",
                        btn:["风险测评","取消"],
                        yes:function(){
                        	 window.location.href = "/wap/answer/record";
                        }
                    });
             	}else{
             		window.location.href = "/wap/answer/result";
             	}

        	});
        };

        $scope.recharge = function(){
        	postUrl.events('/wap/member/checkTrustAccount').success(function(_data) {
        		_data = _data.data;
        		if(_data == 0){
        			var $layer = $injector.get('$dylayer');
                    $layer.confirm({
                        msg:"请先开通资金托管",
                        btn:["马上开通","暂不开通"],
                        yes:function(){
                        	 postUrl.events('/wap/system/isApprove').success(function(_data) {
                             	_data = _data.data;
                                 if(_data.is_realname!=1&&_data.realname_status!=-2){
        	                         $layer.alert("请先实名认证",1000,function(){
        	                             window.location.href = "/wap/member/approverealname";
        	                         });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else{
                                	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                         if(_data.status=="200"){
                                             $(_data.description.form).appendTo($("body"));
                                         }else{
                                         	$layer.alert(_data.description,1000);
                                         }
                                     });
                                 }
                             });
                        }
                    });
        		}else{
        			window.location = "/wap/member/recharge";
        		}
        	});

        };
    })

    //用户中心资金详情页
    dyUser.controller("accountCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        postUrl.events('/wap/member/account',{"type":1}).success(function(_data) {
        	_data = _data.data ;
                $scope.account = _data;
                $scope.loading = true;
            });
        $scope.recharge = function(){
        	postUrl.events('/wap/member/checkTrustAccount').success(function(_data) {
        		_data = _data.data;
        		if(_data==0){
        			var $layer = $injector.get('$dylayer');
                    $layer.confirm({
                        msg:"请先开通资金托管",
                        btn:["马上开通","暂不开通"],
                        yes:function(){
                        	 postUrl.events('/wap/system/isApprove').success(function(_data) {
                             	_data = _data.data;
                                 if(_data.is_realname!=1&&_data.realname_status!=-2){
        	                         $layer.alert("请先实名认证",1000,function(){
        	                             window.location.href = "/wap/member/approverealname";
        	                         });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else{
                                	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                         if(_data.status=="200"){
                                             $(_data.description.form).appendTo($("body"));
                                         }else{
                                         	$layer.alert(_data.description,1000);
                                         }
                                     });
                                 }
                             });
                        }
                    });
        		}else{
        			window.location = "/wap/member/recharge";
        		}
        	});

        };
    });

    //用户中心设置支付密码
    dyUser.controller("setPaypwdCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

        $scope.loading = false;

        $scope.PaypwdForm = {};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };
        $timeout(function(){
            $scope.loading = true;
        },500);
        vm.setPaypwdForm = function () {
            $scope.setBtn = true;
            var url = "/wap/member/editpaypwd";
            var data = $scope.PaypwdForm;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.data,800,function(){
                       window.location = "/wap/member/myaccountdata";
                   })
                }else{
                    $scope.showError(_data.description);
                }
                $scope.setBtn = false;
            }
            $scope._submitFn(url,data,callback)
        };
    });

    //绑定手机号
    dyUser.controller("bindPhoneCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.phoneForm = {};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.bindPhone = function () {
            $scope.btnStatus = true;
            var url = "/wap/public/checkPhone";
            var data = $scope.phoneForm;

            var callback = function(_data){
                if(_data.code=="200"){
                    $.cookie('phone', $scope.phoneForm.phone);
                    window.location = "/wap/member/approvePhone";
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback)
        };

        $scope.phone = $.cookie('phone');
        $scope.phoneForm2 = {"type":"approve"};
        //手机绑定2
        vm.bindPhone2 = function () {
            $scope.btnStatus = true;
            var url = "/wap/member/approvephone";
            var data = $scope.phoneForm2;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert("绑定成功",800,function(){
                       window.location = "/wap/member/myaccountdata";
                   })
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback)
        };

        //获取手机验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getPhoneCode = function(){
            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var data = {"type":"approve"};
            var url = "/wap/system/sendsms";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向手机<span>"+$scope.phone+"</span>发送短信，请输入短信验证码完成绑定。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }
    });

    //修改手机号
    dyUser.controller("editPhoneCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

        $scope.editPhoneTit = "修改手机"; //导航栏标题
        $scope.btnStatus = false; //按钮状态
        $scope.getCodeVal = $scope.getCodeVal2 = "获取验证码"; //获取验证码按钮的value值
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };


        $scope.loading = false;
        $scope.updatePhoneStep = 1;
        $timeout(function() {
            $scope.loading = true;
        },300);

        postUrl.events('/wap/member/updatePhoneone',{"type":"data"}).success(function(_data) {
        	_data = _data.data;
                $scope.phone= _data.phone; //手机号
        });

        //修改手机号1--点击修改手机按钮
        $scope.editPhone = function(){
            $scope.loading = false;
            $scope.updatePhoneStep = 2;
            $scope.editPhoneTit = "验证旧手机";
            $timeout(function() {
                $scope.loading = true;
            },300);
        }


        //修改手机号2--验证旧手机提交表单
        $scope.phoneForm2 = {}; //提交到后台的数据
        vm.editPhone2 = function(){
            $scope.btnStatus = true;
            var url = "/wap/member/updatephonetwo";
            var data = $scope.phoneForm2;

            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.updatePhoneStep = 3;
                    $scope.editPhoneTit = "验证新手机";
                    $scope.showError('');
                    $scope.smsNotice ='';
                } else {
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback);
        }

        //修改手机号3--验证新手机完成绑定
        $scope.phoneForm3 = {}; //提交到后台的数据
        vm.editPhone3 = function(){
            $scope.btnStatus = true;
            var url = "/wap/member/updatephonethree";
            var data = $scope.phoneForm3;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.data,800,function(){
                       window.location = "/wap/member/myaccountdata";
                   })
                    //$scope.updatePhoneStep = 3;
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback);
        }

        //获取手机验证码
        $scope.getPhoneCode = function(data){
            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var data = data;
            var url = "/wap/system/sendsms";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向手机<span>"+$scope.phone+"</span>发送短信，请输入短信验证码完成绑定。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }

        //同个页面有多个发送验证码，需要另外写一段，否则会互相影响
        $scope.getPhoneCode2 = function(data){
            $scope.isSend2 = true;
            $scope.getCodeVal2 = "发送中...";
            var data = angular.extend(data,{"phone":$scope.phoneForm3.phone});
            var url = "/wap/system/sendsms";
            var callback = function(_data) {
                $scope.showError('');
                if (_data.code == "200") {
                    $scope.newCountdown({
                        "disVar": "isSend2",
                        "getBtn": "getCodeVal2"
                    });
                    $scope.smsNotice = "已向手机<span>" + $scope.phoneForm3.phone + "</span>发送短信，请输入短信验证码完成绑定。"
                } else {
                    $scope.showError(_data.description);
                    $scope.isSend2 = false;
                    $scope.getCodeVal2 = "重新获取";
                }

            }
            $scope._submitFn(url,data,callback);
        }
    });

    //修改邮箱
    dyUser.controller("editEmailCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

        $scope.editEmailTit = "修改邮箱"; //导航栏标题
        $scope.btnStatus = false; //按钮状态
        $scope.getCodeVal = $scope.getCodeVal2 = "获取验证码"; //

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        $scope.loading = false;
        $scope.updateEmailStep = 1;
        $timeout(function() {
            $scope.loading = true;
        },300);

        postUrl.events('/wap/member/updateEmailone').success(function(_data) {
        	_data = _data.data ;
                $scope.email= _data.email; //邮箱号
        });

        //修改邮箱第一步--点击修改邮箱按钮
        $scope.editEmail = function(){
            $scope.loading = false;
            $scope.updateEmailStep = 2;
            $scope.editEmailTit = "验证旧邮箱";
            $timeout(function() {
                $scope.loading = true;
            },300);
        }

        //修改邮箱2--验证旧邮箱提交表单
        $scope.emailForm2 = {}; //提交到后台的数据
        vm.editemail2 = function(){
            $scope.btnStatus = true;
            var url = "/wap/member/updateemailtwo";
            var data = $scope.emailForm2;

            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.updateEmailStep = 3;
                    $scope.smsNotice = '';
                    $scope.showError('');
                    $scope.editEmailTit = "验证新邮箱";
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback);
        }

        //修改邮箱3--输入新邮箱提交表单
        $scope.emailForm3 = {}; //提交到后台的数据
        vm.editemail3 = function(){
            $scope.btnStatus = true;
            var url = "/wap/member/updateemailthree";
            var data = $scope.emailForm3;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.data,800,function(){
                       window.location = "/wap/member/myaccountdata";
                    })
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback);
        }

        //获取邮箱验证码
        $scope.getCode = function(options){
            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var data = angular.extend({},options);
            var url = "/wap/member/sendemail";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向邮箱<span>"+$scope.email+"</span>发送验证码，请输入验证码完成验证。"
                    $scope.showError("");
                }else{
                    $scope.showError(_data.description);
                    $scope.isSend = false;
                    $scope.smsNotice = "" ;
                    $scope.getCodeVal = "重新获取";
                }
            }
            $scope._submitFn(url,data,callback);
        }

        //同个页面有多个发送验证码，需要另外写一段，否则会互相影响
        $scope.getCode2 = function(data){
            $scope.isSend2 = true;
            $scope.getCodeVal2 = "发送中...";
            var data = angular.extend(data,{"email":$scope.emailForm3.email});
            var url = "/wap/member/sendemail";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.newCountdown({"disVar":"isSend2","getBtn":"getCodeVal2"});
                    $scope.smsNotice = "已向邮箱<span>"+$scope.emailForm3.email+"</span>发送短信，请输入短信验证码完成绑定。"
                    $scope.showError("");
                }else{
                    $scope.showError(_data.description);
                    $scope.isSend2 = false;
                    $scope.smsNotice = "" ;
                    $scope.getCodeVal2 = "重新获取";
                }

            }
            $scope._submitFn(url,data,callback);
        }
    });

    //邮箱认证
    dyUser.controller("bindEmailCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.emailForm = {};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };

        vm.bindEmail = function () {
            $scope.btnStatus = true;
            var url = "/wap/member/checkEmail";
            var data = $scope.emailForm;

            var callback = function(_data){
                if(_data.code == "200"){
                    $.cookie('email', $scope.emailForm.email);
                    window.location = "/wap/member/approveemail";
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback)
        };

        $scope.email = $.cookie('email');
        //绑定邮箱2
        vm.bindEmail2 = function () {
            $scope.btnStatus = true;
            var url = "/wap/member/approveemail";
            var data = $scope.emailForm2;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.data,800,function(){
                       window.location = "/wap/member/myaccountdata";
                   });
                }else{
                    $scope.showError(_data.description);
                }
                $scope.btnStatus = false;
            }
            $scope._submitFn(url,data,callback)
        };

        //获取手机验证码
        $scope.getCodeVal = "获取验证码";
        $scope.getPhoneCode = function(data){
            $scope.isSend = true;
            $scope.getCodeVal = "发送中...";
            var data = angular.extend({},data);
            var url = "/wap/member/sendemail";
            var callback = function(_data){
                if(_data.code == "200"){
                    $scope.countDown();
                    $scope.smsNotice = "已向邮箱<span>"+$scope.email+"</span>发送验证码，请输入验证码完成绑定。"
                }else{
                    $scope.showError(_data.description);
                }
            }
            $scope._submitFn(url,data,callback);
        }
    });

    //用户中心，账户详情页
    dyUser.controller("uDetailsCtrl", function($scope, $http, $location, postUrl, $timeout, $injector) {
        //loading加载
        $scope.loading = false;
        postUrl.events('/wap/member/myAccountdata',{"type":"data"}).success(function(_data) {
            _data = _data.data ;
            $scope.account = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });
        $scope.openTrust = function(){
            var $layer = $injector.get('$dylayer');
            $layer.confirm({
                msg:"请先开通资金托管",
                btn:["马上开通","暂不开通"],
                yes:function(){
                	 postUrl.events('/wap/system/isApprove').success(function(_data) {
                     	_data = _data.data;
                         if(_data.is_realname!=1&&_data.realname_status!=-2){
	                         $layer.alert("请先实名认证",1000,function(){
	                             window.location.href = "/wap/member/approverealname";
	                         });
                          }else if(_data.is_email!=1){
                             $layer.alert("请先进行邮箱认证",1000,function(){
                                 window.location.href = "/wap/member/checkEmail";
                             });
                          }else if(_data.is_phone!=1){
                             $layer.alert("请先绑定手机号",1000,function(){
                                 window.location.href = "/wap/member/checkPhone";
                             });
                         }else{
                        	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                 if(_data.status=="200"){
                                     $(_data.description.form).appendTo($("body"));
                                 }else{
                                 	$layer.alert(_data.description,1000);
                                 }
                             });
                         }
                     });
                }
            });
        }
    })

    //用户中心 实名认证
    dyUser.controller("realnameCtrl", function($scope, $http, $location, $injector, $timeout) {

        $scope.loading = false;
        $timeout(function(){
            $scope.loading = true;
        },300);

        //根据参数显示 提示信息
        var notice = isEmpty($location.search()) ? $location.search() : '';
        $scope.isNotice = notice.type == 'bank' ? true : false;


        $(".file").change(function(){
            var url = getObjectURL(this.files[0]) ;
            var pic = $(this).prev();
            if (url) {
                pic.html('<img src="'+url+'">');
            }
        });
        //建立一個可存取到file的url
        function getObjectURL(file) {
            var url = null ;
            if (window.createObjectURL!=undefined) { // basic
                url = window.createObjectURL(file) ;
            } else if (window.URL!=undefined) { // mozilla(firefox)
                url = window.URL.createObjectURL(file) ;
            } else if (window.webkitURL!=undefined) { // webkit or chrome
                url = window.webkitURL.createObjectURL(file) ;
            }
            return url ;
        }

        //提交实名认证表单
        var vm = $scope.vm = {htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };
        $scope.formData = {};
        require("submitForm");
        vm.realForm = function () {
            $("#realForm").ajaxSubmit({
                type:"post",
                url:"/wap/member/approverealname",
                success:function(_data){
                    var $layer = $injector.get('$dylayer');
                    var result = _data;
                    if(result.code == "200"){
                        $layer.alert("数据提交成功，请耐心等待审核..",800,function(){
                            window.location = "/wap/member/myaccountdata";
                        })
                    }else{
                        $layer.alert(result.description);
                        //$scope.showError(_data.description);
                    }
                }
            });
        };
    });

    //用户中心 实名认证
    dyUser.controller("checkrealnameCtrl", function($scope, $http, $location, postUrl, $injector, $timeout) {
        $scope.loading = false;
        postUrl.events('/wap/member/checkRealname',{"type":"data"}).success(function(_data) {
        	_data = _data.data ;
            $scope.user = _data;
            $timeout(function(){
                $scope.loading = true;
            },300);
        });
    });


    //用户中心 银行卡
    dyUser.controller("bankCtrl", function($scope, $http, $location, postUrl, $injector, $timeout) {
        $scope.loading = false;
        var $layer = $injector.get('$dylayer');
        //根据参数显示 提示
        var notice = isEmpty($location.search()) ? $location.search() : '';
        $scope.isNotice = notice.type == 'bank' ? true : false;

        postUrl.events('/wap/bank/index').success(function(_data) {
        	_data = _data.data ;
            $scope.bankCard = _data.bank_info;
            $scope.is_bind = _data.is_bind ;
            $scope.is_add = _data.is_add ;
            $timeout(function(){
                $scope.loading = true;
            },300);
        });

        $scope.addBank = function(){
        	postUrl.events('/wap/member/checkTrustAccount').success(function(_data) {
        		_data = _data.data;
        		if(_data==0){
        			var $layer = $injector.get('$dylayer');
                    $layer.confirm({
                        msg:"请先开通资金托管",
                        btn:["马上开通","暂不开通"],
                        yes:function(){
                        	 postUrl.events('/wap/system/isApprove').success(function(_data) {
                             	_data = _data.data;
                                 if(_data.is_realname!=1&&_data.realname_status!=-2){
        	                         $layer.alert("请先实名认证",1000,function(){
        	                             window.location.href = "/wap/member/approverealname";
        	                         });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else{
                                	 postUrl.events('/wap/trust/register',{"type":"data"}).success(function(_data) {
                                         if(_data.status=="200"){
                                             $(_data.description.form).appendTo($("body"));
                                         }else{
                                         	$layer.alert(_data.description,1000);
                                         }
                                     });
                                 }
                             });
                        }
                    });
        		}else{
        			postUrl.events('/wap/trust/bindBankCard',$scope.formData).success(function(_data) {
                        if(_data.status == "200"){
                        	$(_data.description.form).appendTo($("body"));                      
                        }else{
                        	$layer.alert(_data.description);
                        }
                     });
        		}
        	});
        };

        $scope.editBank = function(){
        	postUrl.events('/wap/trust/unBindBankCard',$scope.formData).success(function(_data) {
                if(_data.status == "200"){
                	$(_data.description.form).appendTo($("body"));
                }else{
                	$layer.alert(_data.description);
                }
            });
        }
        $scope.delBank = function(id){
        	postUrl.events('/trust/trustDirect/unBindBankCard?bankId=' + id + '&operateType=unBindBankCard',$scope.formData).success(function(_data) {
                if(_data.status == "200"){
                    $layer.alert("解绑成功",2000,function(){
                    	window.location.reload();
                    })             	
                }else{
                	$layer.alert(_data.description);
                }
            });
        }
    });

    //用户中心 银行卡添加
    dyUser.controller("bankaddCtrl", function($scope, $http, $location, postUrl, $injector, $timeout) {

        $scope.formData = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {
            blurTrig: true,
            showError : false,
            removeError : false
        };

        //请求数据
        $scope.loading = false;
        postUrl.events('/wap/bank/addBank',{type:'data'}).success(function(_data) {
            $scope.bankInfo = _data;
            $timeout(function(){
                $scope.loading = true;
            },300);
        });

        $scope.loading = true;
        $scope.tenderBtn = {};
        $scope.tenderBtn.text = "确定";
        vm.bankaddForm = function () {
            $scope.playPwdShow();
        };

        $scope.bankSubmit = function(){
            $scope.tenderBtn.text = "投资中...";
            $scope.tenderBtn.status = true;

            //return false;

            //错误提示位置
            $scope.errorPlace = "";
            postUrl.events('/wap/bank/addBankSubmit',$scope.formData).success(function(_data) {
               if(_data.status == "200"){
                  var $layer = $injector.get('$dylayer');
                  $layer.alert("更换成功",800,function(){
                    window.location.href = "/wap/bank/index";
                  });
               }else{
                    $scope.errorPlace = _data.description;
               }
               $scope.tenderBtn.text = "确定";
               $scope.tenderBtn.status = false;
            });
        }


        /******选择省市******/
        $scope.datalist = '';

        //省市列表显示
        $scope.cityList = function(){
            if($scope.datalist){
                $('body').addClass('city-cover');
                return false;
            }
            $scope.loading = false;
            postUrl.events('/wap/public/getProvince').success(function(_data) {
                $scope.datalist = _data;
                $timeout(function(){
                    $scope.loading = true;
                    $('body').addClass('city-cover');
                },300);
            });
        }
        //省市列表隐藏
        $scope.cityListHide = function(){
            $('body').removeClass('city-cover');
        }
        //选择省份
        $scope.selectProvince = function(id, name, index){
            $scope.formData.province = id;
            $scope.formData.provinceText = name;
            $scope.tabid = id;
            if(!$scope.datalist[index].citylist){
                postUrl.events('/wap/public/getCity',{pid:id}).success(function(_data) {
                $scope.datalist[index].citylist = _data;});
            }
            return false;
        }
        //选择城市
        $scope.selectCity = function(id,name){
            $scope.formData.city = id;
            $scope.formData.cityText = name;
            $scope.cityListHide();
            return false;
        }
        //城市的显示与隐藏
        $scope.isSetid = function(tabid) {
            return $scope.tabid === tabid;
        };

        /******选择银行卡******/
        $scope.bankList = function(){
            $('body').addClass('bank-cover');
            if($scope.banklist){
                $('body').addClass('bank-cover');
                return false;
            }
            $scope.loading = false;
            postUrl.events('/wap/bank/allBank').success(function(_data) {
                $scope.banklist = _data;
                $timeout(function(){
                    $scope.loading = true;
                    $('body').addClass('bank-cover');
                },300);
            });
        }
        $scope.bankListHide = function(){
            $('body').removeClass('bank-cover');
        }
        $scope.selectBank = function(bank,bankText){
            $scope.formData.bank = bank;
            $scope.formData.bankText = bankText;
            $scope.bankListHide();
        }
    });

    //用户中心 银行卡更换
    dyUser.controller("bankChangeCtrl", function($scope, $http, $location, postUrl, $injector, $timeout) {

        $scope.formData = {};
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {
            blurTrig: true,
            showError : false,
            removeError : false
        };

        //请求数据
        $scope.loading = false;
        postUrl.events('/wap/bank/editBank',{type:'data'}).success(function(_data) {
            $scope.bankInfo = _data;
            $timeout(function(){
                $scope.loading = true;
            },300);
        });

        $scope.tenderBtn = {};
        $scope.tenderBtn.text = "确定";
        vm.bankaddForm = function () {
            $scope.playPwdShow();
        };

        $scope.bankSubmit = function(){
            $scope.tenderBtn.text = "投资中...";
            $scope.tenderBtn.status = true;

            //错误提示位置
            $scope.errorPlace = "";
            postUrl.events('/wap/bank/editBankSubmit',$scope.formData).success(function(_data) {
               if(_data.status == "200"){
                  var $layer = $injector.get('$dylayer');
                  $layer.alert("更换成功",800,function(){
                    window.location.href = "/wap/bank/index";
                  });
               }else{
                    $scope.errorPlace = _data.description;
               }
               $scope.tenderBtn.text = "确定";
               $scope.tenderBtn.status = false;
            });
        }


        /******选择省市******/
        $scope.datalist = '';

        //省市列表显示
        $scope.cityList = function(){
            // if($scope.datalist){
            //     $('body').addClass('city-cover');
            //     return false;
            // }
            $scope.loading = false;
            postUrl.events('/wap/public/getProvince').success(function(_data) {
                $scope.datalist = _data;
                $timeout(function(){
                    $scope.loading = true;
                    $('body').addClass('city-cover');
                },300);
            });
        }
        //省市列表隐藏
        $scope.cityListHide = function(){
            $('body').removeClass('city-cover');
        }
        //选择省份
        $scope.selectProvince = function(id, name, index){
            $scope.formData.province = id;
            $scope.formData.provinceText = name;
            $scope.tabid = id;
            if(!$scope.datalist[index].citylist){
                postUrl.events('/wap/public/getCity',{pid:id}).success(function(_data) {
                $scope.datalist[index].citylist = _data;});
            }
            return false;
        }
        //选择城市
        $scope.selectCity = function(id,name){
            $scope.formData.city = id;
            $scope.formData.cityText = name;
            $scope.cityListHide();
            return false;
        }
        //城市的显示与隐藏
        $scope.isSetid = function(tabid) {
            return $scope.tabid === tabid;
        };

        /******选择银行卡******/
        $scope.bankList = function(){
            $('body').addClass('bank-cover');
            if($scope.banklist){
                $('body').addClass('bank-cover');
                return false;
            }
            $scope.loading = false;
            postUrl.events('/wap/bank/allBank').success(function(_data) {
                $scope.banklist = _data;
                $timeout(function(){
                    $scope.loading = true;
                    $('body').addClass('bank-cover');
                },300);
            });
        }
        $scope.bankListHide = function(){
            $('body').removeClass('bank-cover');
        }
        $scope.selectBank = function(bank,bankText){
            $scope.formData.bank = bank;
            $scope.formData.bankText = bankText;
            $scope.bankListHide();
        }
    });

    //修改登录密码
    //用户中心设置支付密码
    dyUser.controller("editPwdCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        $scope.pwdForm = {};

        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError : false,
            removeError : false
        };
        $timeout(function(){
            $scope.loading = true;
        },500);
        vm.editPwdForm = function () {
            $scope.setBtn = true;
            var url = "/wap/member/editpwd";
            var data = $scope.pwdForm;

            var callback = function(_data){
                if(_data.code == "200"){
                    var $layer = $injector.get('$dylayer');
                    $layer.alert(_data.data,800,function(){
                       window.location = "/wap/member/myaccountdata";
                   })
                }else{
                    $scope.showError(_data.description);
                }
                $scope.setBtn = false;
            }
            $scope._submitFn(url,data,callback)
        };
    });

    //用户信息
    dyUser.controller("userInfoCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        postUrl.events('/wap/member/center').success(function(_data) {
        	_data = _data.data ;
                $scope.userInfo = _data;
                $scope.avatar = _data.avatar || '/wapassets/default/images/user.png';
        });

        //头像上传
        $(".avatar-file").change(function(){
            require("submitForm");
            $(".avatar-loading").fadeIn();

            $("#avatarForm").ajaxSubmit({
                type:"post",
                url:"/wap/member/avatarSubmit",
                success:function(_data){
                    var result = _data;
                    $('.avatar img').attr({'src': result.data.avater});
                    setTimeout(function(){
                        $(".avatar-loading").fadeOut();
                    },300)
                }
            });

            return false;
        });
    })

    //我的投资 - 投资详情
    dyUser.controller("myloanInfoCtrl", function($scope, $http,$injector, $location,postUrl,$timeout) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/tender/tenderinfodata',$scope.typeId).success(function(_data) {
        	_data = _data.data ;
        	$scope.tender_info = _data.tender_info;
            $scope.recover_info = _data.recover_info;
            $scope.loan_info = _data.loan_info;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        diyou.use('common', function(app) {
            app.tabs();
        });
       //查看协议范本 给app用的h5页面
        $scope.look = function(_sty,id){
        	var url = "/wap/loan/loanAgreement#?id="+id;
        	window.location.href=url;
//            var $layer = $injector.get('$dylayer');
//
//            $layer.page({
//                type:2,
//                //url:"/wap/index/xieyifanben"
//                url: url
//            });
        }
    })

    //我的投资 -借款协议
    dyUser.controller("myloanAgreement", function($scope, $http,$injector, $location,postUrl,$timeout) {
        $scope.typeId = isEmpty($location.search()) ? $location.search() : '';
        $scope.loading = false;
        postUrl.events('/wap/loan/getAgreeInfo',$scope.typeId).success(function(_data) {
            $scope.LoanProtocol = _data.data;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });
    })

    //站内信 列表
    dyUser.controller("messageListCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

        $scope.loading = false;
        postUrl.events('/wap/message/getList').success(function(_data) {
        	_data = _data.data;
            if(_data.items.length < 1){
                $scope.getMoreStatus = false;
            }
            $scope.pageData = _data;

            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            $scope.loader = true;
            postUrl.events('/wap/message/getList',$scope.listData).success(function(_data) {
            	_data = _data.data;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
               }else{
                    $scope.pageData.items = $scope.pageData.items.concat(_data.items);
                }
                $scope.loader = false;
            });
        }
    })

    //站内信 详情
    dyUser.controller("messageDetailCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {


        $scope.loading = false;
        $scope.postData = isEmpty($location.search()) ? $location.search() : '';
        postUrl.events("/wap/message/detail",$scope.postData).success(function(_data) {
            $scope.pageData = _data.data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

    })

    //提现
    dyUser.controller("cashCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        var $layer = $injector.get('$dylayer');
        $scope.loading = false;

        postUrl.events("/wap/bank/withdrawCash",{type:'data'}).success(function(_data) {
        	_data = _data.data ;
            if(_data.is_bind!="1"){
                $layer.confirm({
                    msg:"您还未绑定银行卡，请先绑定银行卡",
                    no:function(){
                        window.history.back(-1);
                    },
                    yes:function(){
                        window.location.href = "/wap/bank/index";
                    }
                });
                /*$layer.alert("请先绑定银行卡",1000,function(){
                    window.location.href = "/wap/bank/addBank";
                });*/
            }else{
                $scope.bankinfo = _data.bank_info[0];
                $timeout(function() {
                  $scope.loading = true;
                },300);
            }

        });


        $scope.formData = {};
        $scope.formData.cost = '';
        $scope.formData.realAmount = '';
        $scope.formData.amount = '';

        //提现费用计算
        var timer;
        $scope.cashFee = function(val){
            if(timer){
                $timeout.cancel(timer);
            }
            timer = $timeout(function() {
                postUrl.events("/wap/bank/getCashFee",{account:val}).success(function(_data) {

                	if(_data.code==100){
                		$scope.disabled = true;
                		$scope.errorWord = _data.description ;
                		return ;
                	}else{
                		$scope.errorWord = '' ;
                	}
                	$scope.disabled = false ;
                    var res = _data.data;
                    $scope.formData.cost = res.fee;
                    $scope.formData.realAmount = res.account_yes ? res.account_yes : 0;
                });
            },300);

            return false;
        }
        //提现提交
        $scope.cashSubmit = function(){
            var val = $scope.formData.amount;
            if(val == ""){
                $layer.alert("请输入提现金额",1000);
                return false;
            }
            if(isNaN(val)){
                $layer.alert("请输入正确的提现金额",1000);
                return false;
            }
       	    $scope.formData.money = val ;

            postUrl.events('/wap/system/isApprove').success(function(_data) {
             	_data = _data.data;
                if(_data.is_realname!=1&&_data.realname_status!=-2){
                     $layer.alert("请先实名认证",1000,function(){
                         window.location.href = "/wap/member/approverealname";
                     });
                  }else if(_data.is_email!=1){
                     $layer.alert("请先进行邮箱认证",1000,function(){
                         window.location.href = "/wap/member/checkEmail";
                     });
                  }else if(_data.is_phone!=1){
                     $layer.alert("请先绑定手机号",1000,function(){
                         window.location.href = "/wap/member/checkPhone";
                     });
                 }else{
                     postUrl.events('/wap/trust/withdraw',$scope.formData).success(function(_data) {
                     if(_data.status == "200"){
                     	$(_data.description.form).appendTo($("body"));
                     }else{
                     	$layer.alert(_data.description);
                     }
                  });
                 }
             });





        }

        $scope.tenderBtn = {};
        $scope.tenderBtn.text = "确定";

        $scope.playSubmit = function(){
            $scope.tenderBtn.text = "提现中...";
            $scope.tenderBtn.status = true;

            //错误提示位置
            $scope.errorPlace = "";
            postUrl.events('/wap/bank/withdraw',$scope.formData).success(function(_data) {

               if(_data.code == "200"){
                  $layer.alert("提现成功",800,function(){
                    window.location.href = "/wap/member/index";
                  });
               }else{
                    $scope.errorPlace = _data.description;
               }
               $scope.tenderBtn.text = "确定";
               $scope.tenderBtn.status = false;
            });
        }

    })

    //提现记录
    dyUser.controller("cashRecordCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        $scope.postData = isEmpty($location.search()) ? $location.search() : '';
        postUrl.events("/wap/bank/withdrawLog",{type:'data'}).success(function(_data) {
        	_data = _data.data;
            $scope.cashRecord = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/bank/withdrawLog',$scope.listData).success(function(_data) {
            	_data = _data.data ;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.cashRecord.items = $scope.cashRecord.items.concat(_data.items);
                }
            });
        }

    })

    //推广管理--我的推广
    dyUser.controller("myspreadCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        var $layer = $injector.get('$dylayer');
        //我的推广信息
        postUrl.events("/wap/spread/mySpreadData").success(function(_data) {
        	_data = _data.data ;
            $scope.countInfo = _data.countInfo;
            $scope.total = _data.total;
            $scope.limit = _data.limit;
            $scope.share_url = _data.share_url ;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //我的推广列表
        postUrl.events("/wap/spread/mySpread").success(function(_data) {
        	_data = _data.data ;
            $scope.mySpread = _data
        });

       //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/spread/mySpread',$scope.listData).success(function(_data) {
            	_data = _data.data;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.mySpread.items = $scope.mySpread.items.concat(_data.items);
                }
            });
        };

        //点击立即结算
        $scope.doAccount = function(){
            postUrl.events('/wap/member/checkTrustAccount').success(function(_data) {
                _data = _data.data;
                if(_data == 0){
                    var $layer = $injector.get('$dylayer');
                    $layer.confirm({
                        msg:"请先开通资金托管",
                        btn:["马上开通","暂不开通"],
                        yes:function(){
                             postUrl.events('/wap/system/isApprove').success(function(_data) {
                                _data = _data.data;
                                 if(_data.is_realname!=1&&_data.realname_status!=-2){
                                     $layer.alert("请先实名认证",1000,function(){
                                         window.location.href = "/wap/member/approverealname";
                                     });
                                  }else if(_data.is_email!=1){
                                     $layer.alert("请先进行邮箱认证",1000,function(){
                                         window.location.href = "/wap/member/checkEmail";
                                     });
                                  }else if(_data.is_phone!=1){
                                     $layer.alert("请先绑定手机号",1000,function(){
                                         window.location.href = "/wap/member/checkPhone";
                                     });
                                 }else{
                                     postUrl.events("/wap/spread/doAccount").success(function(_datas) {
                                        if(_datas.code == "200"){
                                            $layer.alert(_datas.data, 1500,function(){
                                                window.location.reload();
                                            });
                                        }else{
                                            $layer.alert(_datas.description, 1500,function(){
                                                window.location.reload();
                                            });
                                        }
                                    });
                                 }
                             });
                        }
                    });
                }else{
                    window.location = "/wap/member/recharge";
                }
            });
        };
       //推广赚钱
        $scope.onSpread = function(){
            $('body').addClass('spread-cover');
        }
        $scope.onSpreadHide = function(){
            $('body').removeClass('spread-cover');
        }
        $scope.onCopy = function(){
            var txt=$scope.mySpread.url;
            alert("请手动复制")
        }
    })

    //推广管理--推广记录
    dyUser.controller("spreadLogCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        $scope.spreadname = {name:name};
        //我的推广信息
        postUrl.events("/wap/spread/spreadLog",$scope.spreadname).success(function(_data) {
        	_data = _data.data;
            $scope.spreadLog = _data;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/spread/spreadLog',$scope.listData).success(function(_data) {
            	_data = _data.data;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.mytenderList.items = $scope.mytenderList.items.concat(_data.items);
                }
            });
        }
    });

    //推广管理--结算记录
    dyUser.controller("settleLogCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {
        $scope.loading = false;
        //我的推广信息
        postUrl.events("/wap/spread/settleLog").success(function(_data) {
        	_data = _data.data ;
            $scope.settleLog = _data;
            $timeout(function() {
                $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.getMoreList = function(){
            $scope.listData.page++;
            postUrl.events('/wap/spread/settleLog',$scope.listData).success(function(_data) {
            	_data = _data.data ;
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    $scope.mytenderList.items = $scope.mytenderList.items.concat(_data.items);
                }
            });
        }
    });

  //问题记录add 2016年7月8日 15:34:25
    dyUser.controller('riskAnswerCtrl', function($scope, $http, $location, postUrl, $injector) {
	    var $layer = $injector.get('$dylayer');
    	$scope.btnStatus = false;//按钮状态，防止重复提交
    	//$scope.isdialog = true;
    	/*$scope.closeDialog = function(){
    		$scope.isdialog = false;
    	}*/

    	$scope.closeDialog = function(){
    		$("#j-mask").addClass("hid");
			$("#j-prompt-wrap").addClass("hid");
    	}

    	//提示：是否进行重测, 未测试过，直接跳转到风险测评页面
    	postUrl.events("/wap/answer/isecho?id="+$("#id").val()).success(function(_data){
    		if(_data.status == 210){//未测试过
    			$("#j-mask").removeClass("hid");
    			$("#j-prompt-wrap").removeClass("hid");
    		}else if(_data.status == 220){ //有测试过,是否重测
    			$layer.confirm({
    				 msg:"是否重新评测？",
                     btn:["是","否"],
                     yes:function(){
                    	 $("#j-mask").removeClass("hid");
             			 $("#j-prompt-wrap").removeClass("hid");
                     },
                     no:function(){
                    	 dyClose: true;
                         window.location.href = "/wap/member/index";
                     }
    			});
    		}
    	});

    	var type = $("#type").val();
    	$scope.formData = {type:type};

        $scope.url = '/wap/risk/getRiskRecordList';
        postUrl.events($scope.url).success(function(_data) {
            $scope.answers = _data.data.records;
        });
        $scope.submitFn = function(){
        	$scope.btnStatus = true;
        	postUrl.events('/wap/answer/submit',$scope.formData).success(function(_data) {
            	if (_data.status=="200") {
                	$layer.alert(_data.data,800,function(){
                		window.location.href = "/wap/answer/introduce";
                		//window.location.href = "/wap/answer/result";
                	});
                } else if(_data.status=="250") {
                	$layer.alert(_data.description,800,function(){
                		window.location.href = "/wap/member/index";
                	});
                }else if(_data.status=="110") {
                	$layer.alert(_data.description,800);
        		}else{
                	$layer.alert(_data.description,800);
                };
                $scope.btnStatus = false;
            });
        }

       /* $scope.submit = function() {
             $.ajax({
                type: "post",
                url: "/risk/answer/submit",
                data: $scope.answer,
                dataType: "json",
                success: function(data){
                    if (data.status=="200") {
                    	parent.layer.msg(data.description, 2, 1, function() {
                            top.location.reload();
                        });
                    }else{
                    	layer.msg(data.description, 2, 0);
                    };
                }
            });
        }*/;

    });

    dyUser.controller('mobileRiskAnswerCtrl', function($scope, $http, $location, postUrl, $injector) {
    	var $layer = $injector.get('$dylayer');
    	$scope.btnStatus = false;//按钮状态，防止重复提交
    	$scope.closeDialog = function(){
    		$("#j-mask").addClass("hid");
			$("#j-prompt-wrap").addClass("hid");
    	}

    	//提示：是否进行重测, 未测试过，直接跳转到风险测评页面
    	postUrl.events("/wap/mobileAnswer/isecho?id="+$("#id").val()).success(function(_data){
    		if(_data.status == 210){//未测试过
    			$("#j-mask").removeClass("hid");
    			$("#j-prompt-wrap").removeClass("hid");
    		}else if(_data.status == 220){ //有测试过,是否重测
    			$layer.confirm({
    				 msg:"是否重新评测？",
                     btn:["是","否"],
                     yes:function(){
                    	 $("#j-mask").removeClass("hid");
             			 $("#j-prompt-wrap").removeClass("hid");
                     },
                     no:function(){
                    	 window.parent.location.href = '/noecho';
                     }
    			});
    		}
    	});

    	var type = $("#type").val();
    	$scope.formData = {};

    	$scope.url = '/wap/mobileAnswer/getRiskRecordList?id='+$("#id").val()+"&type="+type;
    	postUrl.events($scope.url).success(function(_data) {
    		$scope.answers = _data.data.records;
    	});
    	$scope.submitFn = function(){
    		$scope.btnStatus = true;
    		var subUrl = '/wap/mobileAnswer/submit?id='+$("#id").val();
    		postUrl.events(subUrl, $scope.formData).success(function(_data) {
    			if (_data.status=="200") {
    				$layer.alert(_data.data,800,function(){
    					window.location.href = "/success";
    				});
    			} else if(_data.status=="250") {
                	$layer.alert(_data.description,800,function(){
                		window.location.href = "/wap/member/index";
                	});
                } else if(_data.status=="110") {
                	$layer.alert(_data.description,800);
                }else{
    				$layer.alert(_data.description,800);
    			};
    			$scope.btnStatus = false;
    		});
    	}
    });

    //end

    //易宝托管-我的支付账号
    dyUser.controller("mytrustCtrl", function($scope, $http, $location,postUrl,trustSkip,$timeout,$injector) {
    	// $scope.trust_info = angular.extend({}, trust_info, {
        //      payment_log: payment_log
        // });

        //loading加载
        $scope.loading = false;

        $http.post("/wap/trust/myTrust").success(function(_data) {
            $scope.trust_info = _data.data.trustAccount;
            $scope.isYeepay = _data.data.isYeepay;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });


        $scope.mytrustReset = function() {
            trustSkip.events('/wap/trust/resetPassword');
        };
        $scope.authorize = function() {
            trustSkip.events('/wap/trust/authorize');
        };
        $scope.authorizeAutoTransfer = function() {
            trustSkip.events('/wap/trust/authorizeAutoTransfer');
        };
        $scope.resetMobile = function() {
            trustSkip.events('/wap/trust/resetMobile');
        };
        $scope.trustLogin = function(){
        	trustSkip.events('/wap/trust/trustLogin');
        };
        $scope.cancelauthorizeAutoTransfer = function() {
        	if($scope.trust_info.type == 'chinapnr') {
        		trustSkip.events('/wap/trust/cancelAuthorizeAutoTransfer');
        	} else {
	            $http.post("/wap/trustDirect/cancelAuthorizeAutoTransfer").success(function(_data) {
	                if (_data.status == "200") {
	                    layer.msg(_data.description, 2, 1, function() {
	                        window.location.reload();
	                    });
	                } else {
	                    layer.msg(_data.description, 2, 3);
	                }
	            })
            }
        };
    });

    //易宝托管-开通易宝资金托管
    dyUser.controller("openTrustCtrl", function($scope, $http, $location,postUrl,$timeout,$injector) {

    });

    dyUser.controller('rechargeCtrl', ['$scope', '$http', '$location', '$validator', 'trustSkip', function($scope, $http, $location, $validator, trustSkip) {
        $http.post("/wap/trust/getTrustType").success(function(_data) {
            if (_data.status == "200") {
                $scope.trustLogo = _data.data.logo;
                $scope.trustName = _data.data.name;
                $scope.trustUrl = _data.data.url;
            }

        });
        $scope.trustReg = function(url, type) {
            trustSkip.events(url);
        }
    }]);

    //选择充值
    dyUser.controller("rechargeCtrl", function($scope, $http, $location, postUrl, trustSkip, $injector, $timeout) {
        var vm = $scope.vm = {
            htmlSource: ""
        };
        vm.validateOptions = { //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: true,
            showError: false,
            removeError: false
        };
        $scope.rechargeTp = {};
        postUrl.events('/wap/recharge/getPaymentList').success(function(_data) {
            _data = _data.data;
            $scope.payment_list = _data.items;
            $timeout(function() {
                $scope.loading = true;
            }, 300);
        });
        $scope.rechargeType = function() {
            $('body').addClass('bank-cover');
        };
        $scope.bankListHide = function() {
            $('body').removeClass('bank-cover');
        }
       $scope.selectBank = function(bankType,nid){
            $scope.rechargeTp.paymentType=nid;
            $scope.bankText=bankType;
            $scope.bankListHide();
        }
       $scope.$watchCollection("rechargeTp", function() {
    	   postUrl.events('/wap/member/rechargefee',$scope.rechargeTp).success(function(_data) {
    		   if(_data.code == 200){
    			   _data = _data.data;
                   $scope.getfee = parseFloat(_data.fee).toFixed(2);
                   $scope.getaccount = parseFloat(_data.income).toFixed(2);
    		   }
           });
       });
       vm.rechargeSubmit = function() {
    	   var $layer = $injector.get('$dylayer');
	   if($scope.rechargeTp.amount>1000000000){
    		   $layer.alert("充值金额不能大于1000000000");
    		   return;
    	   }
	       $layer.confirm({
	    	  	msg:"确认是否充值？",
	        	yes:function(){
	        		postUrl.events('/wap/system/isApprove').success(function(_data) {
                     	_data = _data.data;
                        if(_data.is_realname!=1&&_data.realname_status!=-2){
	                         $layer.alert("请先实名认证",1000,function(){
	                             window.location.href = "/wap/member/approverealname";
	                         });
                          }else if(_data.is_email!=1){
                             $layer.alert("请先进行邮箱认证",1000,function(){
                                 window.location.href = "/wap/member/checkEmail";
                             });
                          }else if(_data.is_phone!=1){
                             $layer.alert("请先绑定手机号",1000,function(){
                                 window.location.href = "/wap/member/checkPhone";
                             });
                         }else{
                        	 trustSkip.events("/wap/trust/recharge",$scope.rechargeTp, {
          	         		   dyClose: true
          	                });
                         }
                     });
	        	}
	      	});
       };
    });

     dyUser.controller("mybountyCtrl", function($scope, $http, $location, postUrl, $timeout, $injector) {
        $scope.loading = false;
        postUrl.events('/wap/member/myBountydata').success(function(_data) {
            _data = _data.data;
            if (_data.items.length < 1) {
                $scope.getMoreStatus = false;
            }
            $scope.bountyData = _data;
            $timeout(function() {
                $scope.loading = true;
            }, 300);
        });

        //点击加载更多
        $scope.listData = { page: 1 };
        $scope.getMoreStatus = true;
        $scope.getMoreList = function() {
            $scope.listData.page++;
            $scope.loader = true;
            postUrl.events('/wap/member/myBountydata', $scope.listData).success(function(_data) {
                _data = _data.data;
                if (_data.items.length == 0) {
                    $scope.getMoreStatus = false;
                } else {
                    $scope.bountyData.items = $scope.bountyData.items.concat(_data.items);
                }
                $scope.loader = false;
            });
        }
    });
})
