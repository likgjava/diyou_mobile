/*!
 * Created By:james;
 * Created Time:2013-11-11;
 * Updated By:james;
 * Updated Time:2013-11-11;
 * http://www.diyou.cn
 */
define(function(require, exports, module) {
    //tool 利息计算器

    exports.tool = function(form_id) {
        //提交 验证
        require('validate');
        jQuery.validator.addMethod("muit3", function(value, element) {
            if ($("#borrow_style").val() == 'season') {
                var style = value % 3;
            }
            return this.optional(element) || (!style);
        }, "请输入3的倍数");
        $("#" + form_id).validate({
            errorPlacement: function(error, element) {
                if (element.siblings('p').length) {
                    error.appendTo(element.siblings('p'));
                } else {
                    error.insertAfter(element.parent());
                }
            },
            errorElement: 'em',
            rules: {
                account: {
                    required: true,
                    min: true,
                    decimals: true
                },
                lilv: {
                    required: true,
                    min: true,
                    decimals: true
                },
                times: {
                    required: true,
                    min: true,
                    digits: true,
                    muit3: true
                }
            },
            messages: {
                account: {
                    required: "请输入借款金额",
                    min: "请输入正确的金额",
                    decimals: "请输入两位小数以内的值"
                },
                lilv: {
                    required: "请输入年利率",
                    min: "请输入正确的年利率",
                    decimals: "请输入两位小数以内的值"
                },
                times: {
                    required: "请输入借款期限",
                    min: "请输入正确的期限",
                    digits: "请输入整数",
                    muit3: "请输入3的倍数"
                }
            },
            submitHandler: function(form) {
                exports.calcTool();
                return false;
            }
        });
    }

    exports.calcTool = function() {
            var borrowStyle = $("#borrow_style").val();
            switch (borrowStyle) {
                case "month":
                    exports.getsMonth();
                    break;
                case "season":
                    exports.getSeason();
                    break;
                case "end":
                    exports.getEnd();
                    break;
                case "endmonth":
                    exports.getEndMonth();
                    break;
                case "endmonths":
                    exports.getEndMonths();
                    break;
                case "endday":
                    exports.getEndDay();
                    break;
                case "months":
                    exports.getMonths();
                    break;
                default:
                    break;
            }
        }
        //等额本息法
    exports.getsMonth = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
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
        var repayAccount, accountAll, interest, capital;
        repayAccount = accountAll = interest = capital = 0;
        //每个月还款额,还款总额,利息,还款本金
        var jQli = Math.pow((1 + monthApr), times);
        if (jQli > 1) {
            repayAccount = account * (monthApr * jQli) / (jQli - 1);
        } else {
            repayAccount = account;
        }
        var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>'
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
            dateStr = _Year + "-" + _Month + "-" + _Day;
            tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (repayAccount * times - repayAccount * (i + 1)).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
        }
        tableTr += "</table>";
        repayAccount = repayAccount.toFixed(2);
        monthAprShow = monthAprShow.toFixed(2);
        accountAll = (repayAccount * times).toFixed(2);
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }

    //新等额本息
    exports.getMonths = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
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
        var repayAccount, accountAll, interest, capital;
        repayAccount = accountAll = interest = capital = 0;
        //每个月还款额,还款总额,利息,还款本金
        var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
        var repayCapital = account / times;
        repayAccount = (account + account * monthApr * times) / times;

        for (var i = 0; i < times; i++) {
            capital = repayCapital;
            interest = repayAccount - repayCapital;
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
            tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + (repayAccount * times - repayAccount * (i + 1)).toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
        }
        tableTr += "</table>";
        repayAccount = repayAccount.toFixed(2);
        monthAprShow = monthAprShow.toFixed(2);
        accountAll = (repayAccount * times).toFixed(2);
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }

    // 按季等额本息法
    exports.getSeason = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
        var account = parseFloat(accountVal); //借款金额
        var yearApr = parseFloat(lilvVal);
        var monthAprShow = yearApr / 12;
        var monthApr = monthAprShow / 100; //月利率
        var times = parseFloat(timesVal); //借款期限
        var todayDate = new Date(),
            dateStr;
        var Year = parseInt(todayDate.getFullYear()),
            Month = parseInt(todayDate.getMonth()) + 1,
            Day = parseInt(todayDate.getDate()); //还款时间
        var season = times / 3;
        var seasonMoney = account / season; //每季应还的本金
        var yesAccount = 0;
        var repay;
        var repayAccount, accountAll, interest, capital;
        repayAccount = accountAll = interest = capital = 0;
        //每个月还款额,还款总额,利息,还款本金
        var tableTr = '<table class="load-tab"><tr><th width="10%">期数</th><th width="20%">月还款本息</th><th width="20%">月还款本金</th><th width="15%">利息</th><th width="20%">余额</th><th width="15%">还款时间</th></tr>';
        for (var i = 0; i < times; i++) {
            repay = account - yesAccount; //应还的金额    		
            interest = repay * monthApr; //利息等于应还金额乘月利率
            repayAccount = repayAccount + interest; //总还款额+利息
            capital = 0;
            if (i % 3 == 2) {
                capital = seasonMoney; //本金只在第三个月还，本金等于借款金额除季度
                yesAccount = yesAccount + capital;
                repay = account - yesAccount;
                repayAccount = repayAccount + capital; //总还款额+本金
            }
            repayAccount = interest + capital;
            accountAll += repayAccount;
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
            tableTr += "<tr><td>" + parseInt(i + 1) + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥" + repay.toFixed(2) + "</td><td>" + dateStr + "</td></tr>"
        }
        repayAccount = "-";
        monthAprShow = monthAprShow.toFixed(2);
        accountAll = (accountAll).toFixed(2);
        tableTr += "</table>";
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }

    //到期还本还息
    exports.getEnd = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
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
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }

    //到期还本，按月付息
    exports.getEndMonth = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
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
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }


    //到期还本，按天付息
    exports.getEndDay = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
        var account = parseFloat(accountVal); //借款金额
        var yearApr = parseFloat(lilvVal);
        var monthAprShow = yearApr / 12;
        var monthApr = monthAprShow / 100; //月利率
        var dayApr = yearApr / 36000;
        var times = parseFloat(timesVal); //借款期限	
        var todayDate = new Date(),
            dateStr;
        var Year = todayDate.getFullYear(),
            Month = todayDate.getMonth() + 1,
            Day = todayDate.getDate(); //还款时间
        var repayAccount = accountAll = interest = 0;
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
        tableTr += "<tr><td>" + 1 + "</td><td>￥" + repayAccount.toFixed(2) + "</td><td>￥" + capital.toFixed(2) + "</td><td>￥" + interest.toFixed(2) + "</td><td>￥0.00</td><td>" + dateStr + "</td></tr>"
        tableTr += "</table>";
        repayAccount = repayAccount.toFixed(2);
        monthAprShow = monthAprShow.toFixed(2);
        accountAll = (interest + account).toFixed(2);
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }

    //到期还本，按月付息,且当月还息
    exports.getEndMonths = function() {
        var accountVal = $("#account").val();
        var lilvVal = $("#lilv").val();
        var timesVal = $("#times").val();
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
        $('#tableTr').html(tableTr);
        $("#monthAcc").html(repayAccount);
        $("#monthAp").html(monthAprShow);
        $("#monthIn").html(accountAll);
    }
});
