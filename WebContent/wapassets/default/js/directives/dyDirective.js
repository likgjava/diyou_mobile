/*!
 * Created By:chenli;
 * Created Time:2014-12-19;
 * Updated By:;
 * Updated Time:;
 * http://www.diyou.cn
 */
define(function(require, exports, module) {
    var dyDir = angular.module("dyDir", [])
        .directive("dyCounttime", function() {
            return {
                scope: {},
                require: '?ngModel',
                restrict: 'ACE',
                link: function(scope, element, attrs, ngModel) {
                    scope.$watch(element, function() {
                        var time = element.attr("data-time");
                        counttime(time, element);
                    });
                    function counttime(times, ele) {
                        var endtime = new Date(times);
                        var timer = setInterval(function() {
                            var nowtime = new Date();
                            var sys_second = parseInt((endtime.getTime() - nowtime.getTime()) / 1000);
                            if (sys_second > 1) {
                                sys_second -= 1;
                                var day = Math.floor((sys_second / 3600) / 24);
                                var hour = Math.floor((sys_second / 3600) % 24);
                                var minute = Math.floor((sys_second / 60) % 60);
                                minute = minute < 10 ? "0" + minute : minute;
                                var second = Math.floor(sys_second % 60);
                                second = second < 10 ? "0" + second : second
                                ele.html(day + "天" + hour + "时" + minute + "分" + second + "秒");
                            } else {
                                clearInterval(timer);
                                ele.html("已过期");
                            }
                        }, 1000);
                    }
                }
            }
        })
        //获取验证码
        .directive("getCode", function() { 
            return {
                restrict: 'A',
                scope: {
                    dyOptions: '='
                },
                link: function(scope, element, attrs) {
                    var defaults = {
                        time: 59
                    };
                    var opt = angular.extend({},defaults,scope.dyOptions);
                    var i = opt.time;

                    //element.html("重新获取("+i+")");   
                    element.on("click",function(){

                    })          
                }
            }
        })
        //格式化银行卡
        .directive('formatBank',function(){
            return {
                require: '?ngModel',
                restrict: 'ACE',
                link: function(scope, element, attrs, ngModel) {
                    element.on("keyup",function(e){
                        element.val(element.val().replace(/[^\d]/g,'').replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1 "));

                    })
                }   
            }
        });
});
