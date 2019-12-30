define(function(require, exports, module) {
    var dyApp = angular.module('dyApp', ['w5c.validator','ngDialog','dylayer.m']);
    module.exports = dyApp;
    function isEmpty(obj) {
        for (var name in obj) {
            return true;
        }
        return false;
    };
    //首页数据
    dyApp.controller('indexCtrl', function($scope, $http,$timeout) {
        //loading加载
        $scope.loading = false;
        $http.post('/wap/index/banner').success(function(_data) {
            $scope.banner = _data.banner;
            $timeout(function() {
                diyou.use('common', function(fn) {
                    fn.slide();
                });
            },500);
        });
        $http.post('/wap/loan/loantotal').success(function(_data) {
            $scope.loantotal = _data.data;
        });
        $http.post('/wap/loan/getLoanSumPer').success(function(_data) {
            $scope.tenderFinance = _data.data;
        });
        $http.post('/wap/index/siteConfig').success(function(_data) {
            $scope.siteConfig = _data;
        });
        //标种借款比例
        $http.post('/wap/loan/getLoanSum').success(function(_data) {
            $scope.getLoanSum = _data.data;
            $timeout(function(){
                //显示图表
                require('echarts');
                // 基于准备好的dom，初始化echarts实例
                var myChart = echarts.init(document.getElementById('main'));
                // 指定图表的配置项和数据
                var option = {
                    tooltip: {
                        trigger: 'item',
                        formatter: "{a} <br/>{b}: {c} ({d}%)",
                        position: ['70%','0%']
                    },
                    color:['#317ef3', '#83c4f3','#b2e0ef','#d6f0e0', '#f3d887', '#ff7f2e'] ,
                    // legend: {
                    //     orient: 'vertical',
                    //     x: 'right',
                    //     y:'center',
                    //     itemGap: 20,
                    //     align: 'left',
                    //     data:$scope.getLoanSum.nameList
                    // },
                    series: [
                        {
                            name:'比例',
                            type:'pie',
                            radius: ['50%', '70%'],
                            avoidLabelOverlap: false,
                            label: {
                                normal: {
                                    show: false,
                                    position: 'center'
                                }
                            },
                            labelLine: {
                                normal: {
                                    show: false
                                }
                            },
                            data:$scope.getLoanSum.list
                        }
                    ]
                };
                // 使用刚指定的配置项和数据显示图表。
                myChart.setOption(option);
            },1000);
        });
        $http.post('/wap/index/loanTopThree').success(function(_data) {
            $scope.loanTopThree = _data;
        });
        $http.post('/wap/risk/levelData').success(function(_data) {
            $scope.levelData = _data.data;
        });
        $timeout(function() {
          $scope.loading = true;
        },500);
    });

  //利息计算器
    dyApp.controller("counterCtrl",function($scope,$http,$location,postUrl,$injector){
        var $layer = $injector.get('$dylayer');
        var vm = $scope.vm = { htmlSource: "" };
        vm.validateOptions = {  //每个表单的配置，如果不设置，默认和全局配置相同
            blurTrig: false,
            showError : false,
            removeError : false
        };

        $http.post('/wap/loan/getRepayTypeList').success(function(_data){
            $scope.loanSel = _data.data;
            $scope.counter.repay_type = $scope.loanSel[0].id;
        })

        $scope.counter = {
            account : "", //投资总额
            lilv :"", //借款利率
            period :"",//投资期限
            repay_type :"month" //还款方式
        }

        vm.countBtn=function(){
            postUrl.events('/wap/common/calculator',$scope.counter).success(function(_data) {
            	$scope.showError('');
                if(_data.code == 200){
                    $scope.accountInterest = _data.data.interest_total;
                }else{
                    //$scope.showError(_data.result);
                    $layer.alert(_data.result,800);
                }
            });
        }

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
    });

    //我要投资列表
    dyApp.controller('listTenderCtrl', function($scope, $http,$timeout,postUrl) {

        $scope.loading = false;
        //列表数据
        $scope.borrowList = "";
        //借款列表页，页面初次加载时默认列表
        var url = '/wap/loan/loantenderdata';
        $http.post(url).success(function(_data) {
            $scope.borrowList = _data;
            $scope.total_items = _data.total_items; //总条数
            $scope.epage = _data.epage; //每页显示的条数
            $scope.page = _data.page; //当前页码

            $timeout(function() {
              $scope.loading = true;
            },300);

            diyou.use('common', function(app) {
                app.counter();
            });
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.loader = false;
        $scope.getMoreList = function(){
            $scope.loader = true;
            $scope.listData.page++;
            postUrl.events(url,$scope.listData).success(function(_data) {
                $timeout(function() {
                    if(_data.items.length==0){
                        $scope.getMoreStatus = false;
                        $scope.loader = false;
                    }else{
                        $scope.borrowList.items = $scope.borrowList.items.concat(_data.items);
                        $scope.loader = false;
                    }
                },200);
            });
        }
    });

    //债权转让列表
    dyApp.controller('listTransferCtrl', function($scope, $http,$timeout,postUrl) {

        $scope.loading = false;
        //列表数据
        $scope.borrowList = "";
        //借款列表页，页面初次加载时默认列表
        var url = '/wap/transfer/transferList';
        $http.post(url).success(function(_data) {
            $scope.borrowList = _data;
            $scope.total_items = _data.total_items; //总条数
            $scope.epage = _data.epage; //每页显示的条数
            $scope.page = _data.page; //当前页码

            $timeout(function() {
              $scope.loading = true;
            },300);

            diyou.use('common', function(app) {
                app.counter();
            });
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.getMoreStatus = true;
        $scope.loader = false;
        $scope.getMoreList = function(){
            $scope.loader = true;
            $scope.listData.page++;
            postUrl.events(url,$scope.listData).success(function(_data) {
                $timeout(function() {
                    if(_data.items.length==0){
                        $scope.getMoreStatus = false;
                        $scope.loader = false;
                    }else{
                        $scope.borrowList.items = $scope.borrowList.items.concat(_data.items);
                        $scope.loader = false;
                    }
                },200);
            });
        }
    });

    //公告列表
    dyApp.controller('noticeListCtrl', function($scope, $http,$timeout,postUrl) {
    	$scope.getMoreStatus = true;
        $scope.loading = false;
        $scope.pageData = "";
        postUrl.events("/wap/articles/noticelist").success(function(_data) {
            $scope.pageData = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.loader = false;
        $scope.getMoreList = function(){
            $scope.loader = true;
            $scope.listData.page++;
            postUrl.events('/wap/articles/noticelist',$scope.listData).success(function(_data) {
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    console.log($scope.pageData.items);
                    $scope.pageData.items = $scope.pageData.items.concat(_data.items);
                    console.log($scope.pageData.items);
                }
                $scope.loader = false;
            });
        }
    });
    //公告列表详情
    dyApp.controller('noticeDetailCtrl', function($scope, $http,$timeout,$location,postUrl) {
        $scope.loading = false;
        $scope.postData = isEmpty($location.search()) ? $location.search() : '';
        postUrl.events("/wap/articles/noticeDetail",$scope.postData).success(function(_data) {
            $scope.pageData = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

    });

    //常见问题列表
    dyApp.controller('articlesListCtrl', function($scope, $http,$timeout,postUrl) {
    	$scope.getMoreStatus = true;
        $scope.loading = false;
        $scope.pageData = "";
        postUrl.events("/wap/articles/articleslist").success(function(_data) {
            $scope.pageData = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

        //点击加载更多
        $scope.listData = {page:1};
        $scope.loader = false;
        $scope.getMoreList = function(){
            $scope.loader = true;
            $scope.listData.page++;
            postUrl.events('/wap/articles/articleslist',$scope.listData).success(function(_data) {
                if(_data.items.length==0){
                    $scope.getMoreStatus = false;
                }else{
                    console.log($scope.pageData.items);
                    $scope.pageData.items = $scope.pageData.items.concat(_data.items);
                    console.log($scope.pageData.items);
                }
                $scope.loader = false;
            });
        }
    });
    //常见问题详情
    dyApp.controller('articlesDetailCtrl', function($scope, $http,$timeout,$location,postUrl) {
        $scope.loading = false;
        $scope.postData = isEmpty($location.search()) ? $location.search() : '';
        postUrl.events("/wap/articles/articlesdetail",$scope.postData).success(function(_data) {
            $scope.pageData = _data;
            $timeout(function() {
              $scope.loading = true;
            },300);
        });

    });
})
