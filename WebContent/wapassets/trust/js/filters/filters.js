define(function(require, exports, module) {
    angular.module('filters', [])
        /**
         * units Filter
         * @Param price
         * @Param units, default is "元"
         * @Param units2, default is "万元"
         * @return number
         */
        /**
         * Usage
         * var myText = "100000";
         * {{myText|units}}
         * {{myText|units:"元":"xx"}}
         * {{myText|units:"元":"xx","1000"}}
         * Output
         * "10万元"
         * "10xx"
         * "1000xx"
         */
        .filter('units', function() {
            return function(price, units, units2, min) {
                //数字加上千位分割
                function fmoney(s, n) {
                    n = n > 0 && n <= 20 ? n : 2;
                    s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";
                    var l = s.split(".")[0].split("").reverse(),
                        r = s.split(".")[1];
                    t = "";
                    for (i = 0; i < l.length; i++) {
                        t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
                    }
                    return t.split("").reverse().join("") + "." + r;
                }

                if (isNaN(min))
                    min = 10000;

                if (units === undefined)
                    units = "元";

                if (units2 === undefined)
                    units2 = "万元";

                if (price >= min) {
                    var newPrice = fmoney((price / min), 2);
                    return newPrice + "<em>" + units2 + "</em>";
                } else {
                    return fmoney(price, 2) + "<em>" + units + "</em>";
                }

            };
        })
        /**
         * Truncate Filter
         * @Param text
         * @Param length, default is 10
         * @Param end, default is "..."
         * @return string
         */
        /**
         * Usage
         * var myText = "This is an example.";
         * {{myText|Truncate}}
         * {{myText|Truncate:5}}
         * {{myText|Truncate:25:" ->"}}
         * Output
         * "This is..."
         * "Th..."
         * "This is an e ->"
         */
        .filter('truncate', function() {
            return function(text, leng, end) {
                if (text != undefined) {
                    if (isNaN(leng))
                        leng = 10;
                    if (end === undefined)
                        end = '...';
                    if (text.length <= leng) {
                        return text;
                    } else {
                        return String(text).substring(0, leng) + end;
                    }
                }
            };
        })
        // 过滤undefined时，显示默认值
        .filter('judge', function() {
            return function(t, d) {
                if (d === undefined)
                    d = 0;
                if (t === undefined)
                    t = d;
                return t;
            }
        })
        //转换时间戳指令，将php返回过来的10位的时间戳转换为13位
        .filter("timestamp", function() {
            var filterfun = function(time) {
                return time += '000';
            };

            return filterfun;
        })
        //截取字符串
        .filter('dylimitTo', function() {
            return function(s, n, sg, sgn) {
                n = n || 3;
                sg = sg || '*';
                sgn = sgn || 2;
                if(s.length<n){
                    return s;
                }else{
                    s = s.substring(0, n);
                    var sgs = "";
                    for (var i = 0; i < sgn; i++) {
                        sgs += sg;
                    }
                    return s + sgs;
                }


            }
        })
        //解决标签被转义
        .filter('to_trusted', ['$sce', function($sce) {
            return function(text) {
                return $sce.trustAsHtml(text);
            };
        }])

    //强制转为浮点型
    .filter('numeric', function() {
        return function(number) {
            number = number.replace(',', '');
            number = parseFloat(number);
            return number;
        };
    })
    .filter('repayStatus', function() {
            return function(status) {
                if(status ==1){
                	status = "回收完";
                } if(status ==-1){
                	status = "回收中"
                }
                return status;

            };
        })
       //空或0字符过滤
    .filter('empty', function() {
        return function(val, sign, unit) {
            sign = sign === undefined ? sign : '-';
            unit = unit ? unit : '';
            val = (sign === undefined || val == 0.00) ? sign : val + unit;
            return val;
        };
    })
    //截取小数点后面的数字
    .filter('numdec', function() {
        return function(val) {
            if(typeof val !="undefined"){
                val = val.split(".")[1];
                return val;
            }
        };
    })
    //截取小数点前面的数字
    .filter('numbefore', function() {
        return function(val) {
            if(typeof val !="undefined"){
                val = val.split(".")[0];
                return val;
            }
        };
    })
    //"万元分割"
    .filter('formatMoney', function() {
        return function(money) {
            if(money>=10000){
                var ceil_num = Math.ceil(money/10000);//小数点后两位不为0时，才显示后两位小数
                if(money == (ceil_num)*10000){
                    money = "<b>" + parseInt(money/10000) + "</b>"+"万元";
                }else{
                    money = "<b>" + (money/10000).toFixed(2) + "</b>"+"万元";
                }
            }else{
                money = "<b>" + parseInt(money) + "</b>"+"元"
            }
            return money;
        };
    });
});
